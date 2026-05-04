package com.takwolf.netty.vudp.bootstrap;

import com.takwolf.netty.vudp.channel.DefaultVirtualChannel;
import com.takwolf.netty.vudp.channel.VirtualChannel;
import com.takwolf.netty.vudp.router.VirtualChannelRouter;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramChannel;
import io.netty.util.AttributeKey;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ServerVirtualBootstrap extends AbstractVirtualBootstrap implements Cloneable {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ServerVirtualBootstrap.class);

    private final ServerVirtualBootstrapConfig config = new ServerVirtualBootstrapConfig(this);

    private final Bootstrap bootstrap;
    private volatile ChannelHandler handler;
    private volatile VirtualChannelRouter<?, ?, ?> router;
    private volatile EventLoopGroup childGroup;
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
                        .addLast(new RouterInboundHandler<>(virtualChannel, router, childGroup, childHandler, childOptions(), childAttrs()))
                        .addLast(new ForwardOutboundHandler(virtualChannel));
            }
        });
        this.bootstrap = bootstrap;
    }

    private ServerVirtualBootstrap(Bootstrap bootstrap, ServerVirtualBootstrap wrapperBootstrap) {
        this(bootstrap);
        handler = wrapperBootstrap.handler;
        router = wrapperBootstrap.router;
        childGroup = wrapperBootstrap.childGroup;
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
        return group(group, group);
    }

    public ServerVirtualBootstrap group(EventLoopGroup parentGroup, EventLoopGroup childGroup) {
        bootstrap.group(parentGroup);
        if (this.childGroup != null) {
            throw new IllegalStateException("childGroup set already");
        }
        this.childGroup = ObjectUtil.checkNotNull(childGroup, "childGroup");
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

    public ServerVirtualBootstrap router(VirtualChannelRouter<?, ?, ?> router) {
        this.router = ObjectUtil.checkNotNull(router, "router");
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

    VirtualChannelRouter<?, ?, ?> router() {
        return router;
    }

    Map<ChannelOption<?>, Object> options() {
        return bootstrap.config().options();
    }

    Map<AttributeKey<?>, Object> attrs() {
        return bootstrap.config().attrs();
    }

    EventLoopGroup childGroup() {
        return childGroup;
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
        if (router == null) {
            throw new IllegalStateException("router not set");
        }
        if (childGroup == null) {
            logger.warn("childGroup is not set. Using parentGroup instead.");
            childGroup = config.group();
        }
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
}
