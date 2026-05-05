package com.takwolf.netty.vudp.bootstrap;

import com.takwolf.netty.vudp.channel.ChildVirtualChannel;
import com.takwolf.netty.vudp.channel.DefaultChildVirtualChannel;
import com.takwolf.netty.vudp.channel.VirtualChannel;
import com.takwolf.netty.vudp.router.RouteResult;
import com.takwolf.netty.vudp.router.VirtualChannelRouter;
import com.takwolf.netty.vudp.util.EventExecutorUtil;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.AttributeKey;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class RouterInboundHandler<Key, RouteContext, Out> extends ForwardInboundHandler {
    private final VirtualChannelRouter<Key, RouteContext, Out> router;
    private final EventLoopGroup childGroup;
    private final ChannelHandler childHandler;
    private final Map<ChannelOption<?>, Object> childOptions;
    private final Map<AttributeKey<?>, Object> childAttrs;

    private final Map<Key, ChildVirtualChannel> registry = new ConcurrentHashMap<>();
    private final Set<ChildVirtualChannel> readTouched = new HashSet<>();

    private int pendingReadTasks;

    RouterInboundHandler(
            VirtualChannel virtualChannel,
            VirtualChannelRouter<Key, RouteContext, Out> router,
            EventLoopGroup childGroup,
            ChannelHandler childHandler,
            Map<ChannelOption<?>, Object> childOptions,
            Map<AttributeKey<?>, Object> childAttrs
    ) {
        super(virtualChannel);
        this.router = router;
        this.childGroup = childGroup;
        this.childHandler = childHandler;
        this.childOptions = childOptions;
        this.childAttrs = childAttrs;
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) {
        super.channelInactive(context);
        for (ChildVirtualChannel childChannel : registry.values()) {
            childChannel.unsafe().close(childChannel.voidPromise());
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, DatagramPacket packet) {
        Key key;
        try {
            key = router.parseKey(packet).orElse(null);
            if (key == null) {
                return;
            }
        } catch (Exception e) {
            virtualChannel().pipeline().fireExceptionCaught(e);
            return;
        }

        EventLoop eventLoop;
        ChildVirtualChannel childChannel = registry.get(key);
        if (childChannel == null) {
            eventLoop = childGroup.next();
        } else {
            eventLoop = childChannel.eventLoop();
            if (!childChannel.isActive()) {
                // Channel is being closed and cannot receive any messages, so ignore.
                return;
            }
        }

        pendingReadTasks += 1;
        packet.retain();
        EventExecutorUtil.executeInEventLoop(eventLoop, () -> {
            if (childChannel == null) {
                routeNewChannel(context, eventLoop, key, packet);
            } else {
                routeExistingChannel(context, childChannel, key, packet);
            }
        });
    }

    private void finishReadTask(ChannelHandlerContext context, DatagramPacket packet) {
        packet.release();
        EventExecutorUtil.executeInEventLoop(context.executor(), () -> {
            pendingReadTasks -= 1;
            checkAndFlushReadComplete();
        });
    }

    private void routeNewChannel(ChannelHandlerContext context, EventLoop eventLoop, Key key, DatagramPacket packet) {
        boolean[] created = { false };

        ChildVirtualChannel childChannel = registry.computeIfAbsent(key, routeKey -> {
            created[0] = true;

            RouteContext routeContext;
            Out message;
            try {
                RouteResult<RouteContext> routeContextResult = router.newContext(routeKey);
                if (!routeContextResult.ok()) {
                    finishReadTask(context, packet);
                    return null;
                }
                routeContext = routeContextResult.get();

                RouteResult<Out> messageResult = router.routeMessage(key, routeContext, packet);
                if (!messageResult.ok()) {
                    finishReadTask(context, packet);
                    return null;
                }
                message = messageResult.get();
            } catch (Exception e) {
                finishReadTask(context, packet);
                virtualChannel().pipeline().fireExceptionCaught(e);
                return null;
            }

            DefaultChildVirtualChannel channel = new DefaultChildVirtualChannel(virtualChannel(), packet.sender());
            try {
                channel.pipeline().addLast(childHandler);
                for (Map.Entry<ChannelOption<?>, Object> entry : childOptions.entrySet()) {
                    // noinspection unchecked
                    channel.config().setOption((ChannelOption<Object>) entry.getKey(), entry.getValue());
                }
                for (Map.Entry<AttributeKey<?>, Object> entry : childAttrs.entrySet()) {
                    // noinspection unchecked
                    channel.attr((AttributeKey<Object>) entry.getKey()).set(entry.getValue());
                }
                channel.closeFuture().addListener((ChannelFutureListener) future -> registry.remove(routeKey));

                EventExecutorUtil.executeInEventLoop(context.executor(), () -> virtualChannel().pipeline()
                        .fireChannelRead(channel)
                        .fireChannelReadComplete());

                channel.unsafe().register(eventLoop, channel.voidPromise());
                channel.pipeline().fireChannelActive();
            } catch (Exception e) {
                finishReadTask(context, packet);
                channel.unsafe().close(channel.voidPromise());
                virtualChannel().pipeline().fireExceptionCaught(e);
                return null;
            }

            try {
                router.attachContext(key, routeContext, channel, true);
            } catch (Exception e) {
                channel.pipeline().fireExceptionCaught(e);
            }

            EventExecutorUtil.executeInEventLoop(context.executor(), () -> {
                channel.pipeline().fireChannelRead(message);
                readTouched.add(channel);
            });
            finishReadTask(context, packet);

            return channel;
        });

        if (created[0]) {
            return;
        }

        EventLoop newEventLoop = childChannel.eventLoop();
        if (!childChannel.isActive()) {
            // Channel is being closed and cannot receive any messages, so ignore.
            finishReadTask(context, packet);
            return;
        }

        EventExecutorUtil.executeInEventLoop(newEventLoop, () -> routeExistingChannel(context, childChannel, key, packet));
    }

    private void routeExistingChannel(ChannelHandlerContext context, ChildVirtualChannel childChannel, Key key, DatagramPacket packet) {
        if (!childChannel.isActive()) {
            // Channel is being closed and cannot receive any messages, so ignore.
            finishReadTask(context, packet);
            return;
        }

        RouteContext routeContext;
        Out message;
        try {
            RouteResult<RouteContext> routeContextResult = router.existingContext(key, childChannel);
            if (!routeContextResult.ok()) {
                finishReadTask(context, packet);
                return;
            }
            routeContext = routeContextResult.get();

            RouteResult<Out> messageResult = router.routeMessage(key, routeContext, packet);
            if (!messageResult.ok()) {
                finishReadTask(context, packet);
                return;
            }
            message = messageResult.get();
        } catch (Exception e) {
            finishReadTask(context, packet);
            virtualChannel().pipeline().fireExceptionCaught(e);
            return;
        }

        try {
            router.attachContext(key, routeContext, childChannel, false);
        } catch (Exception e) {
            childChannel.pipeline().fireExceptionCaught(e);
        }

        EventExecutorUtil.executeInEventLoop(context.executor(), () -> {
            childChannel.pipeline().fireChannelRead(message);
            readTouched.add(childChannel);
        });
        finishReadTask(context, packet);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext context) {
        checkAndFlushReadComplete();
    }

    private void checkAndFlushReadComplete() {
        if (pendingReadTasks > 0) {
            return;
        }
        for (ChildVirtualChannel childChannel : readTouched) {
            childChannel.pipeline().fireChannelReadComplete();
        }
        readTouched.clear();
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext context) {
        super.channelWritabilityChanged(context);
        for (ChildVirtualChannel childChannel : registry.values()) {
            childChannel.pipeline().fireChannelWritabilityChanged();
        }
    }
}
