package com.takwolf.netty.vudp.bootstrap;

import com.takwolf.netty.vudp.channel.ChildVirtualChannel;
import com.takwolf.netty.vudp.channel.DefaultChildVirtualChannel;
import com.takwolf.netty.vudp.channel.DefaultVirtualChannel;
import com.takwolf.netty.vudp.channel.VirtualChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.AttributeKey;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ServerVirtualBootstrap extends AbstractVirtualBootstrap implements Cloneable {
    private final ServerVirtualBootstrapConfig config = new ServerVirtualBootstrapConfig(this);

    private final Bootstrap bootstrap;
    private volatile ChannelHandler handler;
    private volatile ChannelHandler childHandler;
    private final Map<ChannelOption<?>, Object> childOptions = new LinkedHashMap<>();
    private final Map<AttributeKey<?>, Object> childAttrs = new ConcurrentHashMap<>();

    private ServerVirtualBootstrap(Bootstrap bootstrap) {
        bootstrap.handler(new ChannelInitializer<DatagramChannel>() {
            @Override
            protected void initChannel(DatagramChannel channel) {
                VirtualChannel virtualChannel = DefaultVirtualChannel.instance(channel);
                virtualChannel.pipeline().addLast(handler);

                channel.pipeline()
                        .addLast(new RouterInboundHandler(virtualChannel, childHandler, childOptions(), childAttrs()))
                        .addLast(new ForwardOutboundHandler(virtualChannel));
            }
        });
        this.bootstrap = bootstrap;
    }

    private ServerVirtualBootstrap(Bootstrap bootstrap, ServerVirtualBootstrap wrapperBootstrap) {
        this(bootstrap);
        handler = wrapperBootstrap.handler;
        childHandler = wrapperBootstrap.childHandler;
        synchronized (wrapperBootstrap.childOptions) {
            childOptions.putAll(wrapperBootstrap.childOptions);
        }
        childAttrs.putAll(wrapperBootstrap.childAttrs);
    }

    public ServerVirtualBootstrap() {
        this(new Bootstrap());
    }

    public ServerVirtualBootstrap localAddress(SocketAddress localAddress) {
        bootstrap.localAddress(localAddress);
        return this;
    }

    public ServerVirtualBootstrap localAddress(InetAddress inetHost, int inetPort) {
        bootstrap.localAddress(inetHost, inetPort);
        return this;
    }

    public ServerVirtualBootstrap localAddress(String inetHost, int inetPort) {
        bootstrap.localAddress(inetHost, inetPort);
        return this;
    }

    public ServerVirtualBootstrap localAddress(int inetPort) {
        bootstrap.localAddress(inetPort);
        return this;
    }

    public ServerVirtualBootstrap group(EventLoopGroup group) {
        bootstrap.group(group);
        return this;
    }

    public ServerVirtualBootstrap channelFactory(ChannelFactory<? extends DatagramChannel> channelFactory) {
        bootstrap.channelFactory(channelFactory);
        return this;
    }

    public ServerVirtualBootstrap channel(Class<? extends DatagramChannel> channelClass) {
        bootstrap.channel(channelClass);
        return this;
    }

    public ServerVirtualBootstrap handler(ChannelHandler handler) {
        this.handler = ObjectUtil.checkNotNull(handler, "handler");
        return this;
    }

    public <T> ServerVirtualBootstrap option(ChannelOption<T> option, T value) {
        bootstrap.option(option, value);
        return this;
    }

    public <T> ServerVirtualBootstrap attr(AttributeKey<T> key, T value) {
        bootstrap.attr(key, value);
        return this;
    }

    public ServerVirtualBootstrap childHandler(ChannelHandler childHandler) {
        this.childHandler = ObjectUtil.checkNotNull(childHandler, "childHandler");
        return this;
    }

    public <T> ServerVirtualBootstrap childOption(ChannelOption<T> childOption, T value) {
        ObjectUtil.checkNotNull(childOption, "childOption");
        synchronized (childOptions) {
            if (value == null) {
                childOptions.remove(childOption);
            } else {
                childOptions.put(childOption, value);
            }
        }
        return this;
    }

    public <T> ServerVirtualBootstrap childAttr(AttributeKey<T> childKey, T value) {
        ObjectUtil.checkNotNull(childKey, "childKey");
        if (value == null) {
            childAttrs.remove(childKey);
        } else {
            childAttrs.put(childKey, value);
        }
        return this;
    }

    public ServerVirtualBootstrapConfig config() {
        return config;
    }

    SocketAddress localAddress() {
        return bootstrap.config().localAddress();
    }

    EventLoopGroup group() {
        return bootstrap.config().group();
    }

    @SuppressWarnings("unchecked")
    ChannelFactory<? extends DatagramChannel> channelFactory() {
        return (ChannelFactory<? extends DatagramChannel>) bootstrap.config().channelFactory();
    }

    ChannelHandler handler() {
        return handler;
    }

    Map<ChannelOption<?>, Object> options() {
        return bootstrap.config().options();
    }

    Map<AttributeKey<?>, Object> attrs() {
        return bootstrap.config().attrs();
    }

    ChannelHandler childHandler() {
        return childHandler;
    }

    Map<ChannelOption<?>, Object> childOptions() {
        synchronized (childOptions) {
            return Collections.unmodifiableMap(childOptions);
        }
    }

    Map<AttributeKey<?>, Object> childAttrs() {
        return Collections.unmodifiableMap(childAttrs);
    }

    private void internalValidate() {
        if (childHandler == null) {
            throw new IllegalStateException("childHandler not set");
        }
    }

    public ServerVirtualBootstrap validate() {
        internalValidate();
        bootstrap.validate();
        return this;
    }

    public ChannelFuture bind(SocketAddress localAddress) {
        internalValidate();
        return wrapFuture(bootstrap.bind(localAddress));
    }

    public ChannelFuture bind(InetAddress inetHost, int inetPort) {
        internalValidate();
        return wrapFuture(bootstrap.bind(inetHost, inetPort));
    }

    public ChannelFuture bind(String inetHost, int inetPort) {
        internalValidate();
        return wrapFuture(bootstrap.bind(inetHost, inetPort));
    }

    public ChannelFuture bind(int inetPort) {
        internalValidate();
        return wrapFuture(bootstrap.bind(inetPort));
    }

    public ChannelFuture bind() {
        internalValidate();
        return wrapFuture(bootstrap.bind());
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public ServerVirtualBootstrap clone() {
        Bootstrap bootstrap = this.bootstrap.clone();
        return new ServerVirtualBootstrap(bootstrap, this);
    }

    public ServerVirtualBootstrap clone(EventLoopGroup group) {
        Bootstrap bootstrap = this.bootstrap.clone(group);
        return new ServerVirtualBootstrap(bootstrap, this);
    }

    @Override
    public String toString() {
        return StringUtil.simpleClassName(this) + '(' + config() + ')';
    }

    private static final class RouterInboundHandler extends ForwardInboundHandler {
        private final ChannelHandler childHandler;
        private final Map<ChannelOption<?>, Object> childOptions;
        private final Map<AttributeKey<?>, Object> childAttrs;

        private final Map<InetSocketAddress, ChildVirtualChannel> router = new ConcurrentHashMap<>();
        private final Set<ChildVirtualChannel> readTouchedChannels = ConcurrentHashMap.newKeySet();

        RouterInboundHandler(
                VirtualChannel virtualChannel,
                ChannelHandler childHandler,
                Map<ChannelOption<?>, Object> childOptions,
                Map<AttributeKey<?>, Object> childAttrs
        ) {
            super(virtualChannel);
            this.childHandler = childHandler;
            this.childOptions = childOptions;
            this.childAttrs = childAttrs;
        }

        @Override
        public void channelInactive(ChannelHandlerContext context) {
            super.channelInactive(context);
            for (ChildVirtualChannel childChannel : router.values()) {
                childChannel.unsafe().close(childChannel.voidPromise());
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void channelRead0(ChannelHandlerContext context, DatagramPacket packet) {
            ChildVirtualChannel childChannel = router.computeIfAbsent(packet.sender(), remoteAddress -> {
                DefaultChildVirtualChannel channel = new DefaultChildVirtualChannel(virtualChannel(), remoteAddress);
                channel.pipeline().addLast(childHandler);
                for (Map.Entry<ChannelOption<?>, Object> entry : childOptions.entrySet()) {
                    channel.config().setOption((ChannelOption<Object>) entry.getKey(), entry.getValue());
                }
                for (Map.Entry<AttributeKey<?>, Object> entry : childAttrs.entrySet()) {
                    channel.attr((AttributeKey<Object>) entry.getKey()).set(entry.getValue());
                }
                channel.closeFuture().addListener((ChannelFutureListener) future -> router.remove(remoteAddress));

                virtualChannel().pipeline()
                        .fireChannelRead(channel)
                        .fireChannelReadComplete();

                channel.unsafe().register(channel.eventLoop(), channel.voidPromise());
                channel.pipeline().fireChannelActive();
                return channel;
            });
            childChannel.pipeline().fireChannelRead(packet.content().retain());
            readTouchedChannels.add(childChannel);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext context) {
            for (ChildVirtualChannel childChannel : readTouchedChannels) {
                childChannel.pipeline().fireChannelReadComplete();
            }
            readTouchedChannels.clear();
        }

        @Override
        public void channelWritabilityChanged(ChannelHandlerContext context) {
            super.channelWritabilityChanged(context);
            for (ChildVirtualChannel childChannel : router.values()) {
                childChannel.pipeline().fireChannelWritabilityChanged();
            }
        }
    }
}
