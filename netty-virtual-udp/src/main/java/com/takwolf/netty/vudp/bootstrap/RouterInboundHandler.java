package com.takwolf.netty.vudp.bootstrap;

import com.takwolf.netty.vudp.channel.ChildVirtualChannel;
import com.takwolf.netty.vudp.channel.DefaultChildVirtualChannel;
import com.takwolf.netty.vudp.channel.VirtualChannel;
import com.takwolf.netty.vudp.router.VirtualChannelRouter;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutor;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

final class RouterInboundHandler extends ForwardInboundHandler {
    @SuppressWarnings("rawtypes")
    private final VirtualChannelRouter router;
    private final EventLoopGroup childGroup;
    private final ChannelHandler childHandler;
    private final Map<ChannelOption<?>, Object> childOptions;
    private final Map<AttributeKey<?>, Object> childAttrs;

    private final Map<Object, ChildVirtualChannel> registry = new ConcurrentHashMap<>();
    private final Set<ChildVirtualChannel> readTouched = new HashSet<>();

    private int pendingReadTasks;

    @SuppressWarnings("rawtypes")
    RouterInboundHandler(
            VirtualChannel virtualChannel,
            VirtualChannelRouter router,
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

    private void executeInEventLoop(EventExecutor eventLoop, Runnable runnable) {
        if (eventLoop.inEventLoop()) {
            runnable.run();
        } else {
            eventLoop.execute(runnable);
        }
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
        try {
            //noinspection unchecked
            router.routeKey(packet).ifPresent((Consumer<Object>) key -> channelReadOnKey(context, key, packet));
        } catch (Exception e) {
            virtualChannel().pipeline().fireExceptionCaught(e);
        }
    }

    private void channelReadOnKey(ChannelHandlerContext context, Object key, DatagramPacket packet) {
        EventLoop eventLoop;

        ChildVirtualChannel channel = registry.get(key);
        if (channel != null) {
            eventLoop = channel.eventLoop();
            if (!channel.isActive()) {
                return;
            }
        } else {
            eventLoop = childGroup.next();
        }

        pendingReadTasks += 1;
        packet.retain();
        executeInEventLoop(eventLoop, () -> {
            try {
                //noinspection unchecked
                router.routeMessage(key, packet).ifPresent(message -> channelReadOnMessage(context, eventLoop, packet.sender(), key, message));
            } catch (Exception e) {
                virtualChannel().pipeline().fireExceptionCaught(e);
            } finally {
                packet.release();
                executeInEventLoop(context.executor(), () -> {
                    pendingReadTasks -= 1;
                    checkAndFlushReadComplete();
                });
            }
        });
    }

    private void channelReadOnMessage(ChannelHandlerContext context, EventLoop eventLoop, InetSocketAddress remoteAddress, Object key, Object message) {
        ChildVirtualChannel childChannel = registry.computeIfAbsent(key, routeKey -> {
            DefaultChildVirtualChannel channel = new DefaultChildVirtualChannel(virtualChannel(), remoteAddress);
            channel.pipeline().addLast(childHandler);
            for (Map.Entry<ChannelOption<?>, Object> entry : childOptions.entrySet()) {
                //noinspection unchecked
                channel.config().setOption((ChannelOption<Object>) entry.getKey(), entry.getValue());
            }
            for (Map.Entry<AttributeKey<?>, Object> entry : childAttrs.entrySet()) {
                //noinspection unchecked
                channel.attr((AttributeKey<Object>) entry.getKey()).set(entry.getValue());
            }
            channel.closeFuture().addListener((ChannelFutureListener) future -> registry.remove(routeKey));

            executeInEventLoop(context.executor(), () -> virtualChannel().pipeline()
                    .fireChannelRead(channel)
                    .fireChannelReadComplete());

            channel.unsafe().register(eventLoop, channel.voidPromise());
            channel.pipeline().fireChannelActive();
            return channel;
        });
        executeInEventLoop(context.executor(), () -> {
            childChannel.pipeline().fireChannelRead(message);
            readTouched.add(childChannel);
        });
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
