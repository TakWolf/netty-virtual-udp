package com.takwolf.netty.vudp.bootstrap;

import com.takwolf.netty.vudp.channel.DefaultVirtualChannel;
import com.takwolf.netty.vudp.channel.VirtualChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramChannel;
import io.netty.util.AttributeKey;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Map;

public final class VirtualBootstrap extends AbstractVirtualBootstrap implements Cloneable {
    private final VirtualBootstrapConfig config = new VirtualBootstrapConfig(this);

    private final Bootstrap bootstrap;
    private volatile ChannelHandler handler;

    private VirtualBootstrap(Bootstrap bootstrap) {
        bootstrap.handler(new ChannelInitializer<DatagramChannel>() {
            @Override
            protected void initChannel(DatagramChannel channel) {
                VirtualChannel virtualChannel = DefaultVirtualChannel.instance(channel);
                virtualChannel.pipeline().addLast(handler);

                channel.pipeline()
                        .addLast(new ForwardInboundHandler(virtualChannel))
                        .addLast(new ForwardOutboundHandler(virtualChannel));
            }
        });
        this.bootstrap = bootstrap;
    }

    private VirtualBootstrap(Bootstrap bootstrap, VirtualBootstrap wrapperBootstrap) {
        this(bootstrap);
        handler = wrapperBootstrap.handler;
    }

    public VirtualBootstrap() {
        this(new Bootstrap());
    }

    public VirtualBootstrap localAddress(SocketAddress localAddress) {
        bootstrap.localAddress(localAddress);
        return this;
    }

    public VirtualBootstrap localAddress(InetAddress inetHost, int inetPort) {
        bootstrap.localAddress(inetHost, inetPort);
        return this;
    }

    public VirtualBootstrap localAddress(String inetHost, int inetPort) {
        bootstrap.localAddress(inetHost, inetPort);
        return this;
    }

    public VirtualBootstrap localAddress(int inetPort) {
        bootstrap.localAddress(inetPort);
        return this;
    }

    public VirtualBootstrap remoteAddress(SocketAddress remoteAddress) {
        bootstrap.remoteAddress(remoteAddress);
        return this;
    }

    public VirtualBootstrap remoteAddress(InetAddress inetHost, int inetPort) {
        bootstrap.remoteAddress(inetHost, inetPort);
        return this;
    }

    public VirtualBootstrap remoteAddress(String inetHost, int inetPort) {
        bootstrap.remoteAddress(inetHost, inetPort);
        return this;
    }

    public VirtualBootstrap group(EventLoopGroup group) {
        bootstrap.group(group);
        return this;
    }

    public VirtualBootstrap channelFactory(ChannelFactory<? extends DatagramChannel> channelFactory) {
        bootstrap.channelFactory(channelFactory);
        return this;
    }

    public VirtualBootstrap channel(Class<? extends DatagramChannel> channelClass) {
        bootstrap.channel(channelClass);
        return this;
    }

    public VirtualBootstrap handler(ChannelHandler handler) {
        this.handler = ObjectUtil.checkNotNull(handler, "handler");
        return this;
    }

    public <T> VirtualBootstrap option(ChannelOption<T> option, T value) {
        bootstrap.option(option, value);
        return this;
    }

    public <T> VirtualBootstrap attr(AttributeKey<T> key, T value) {
        bootstrap.attr(key, value);
        return this;
    }

    public VirtualBootstrapConfig config() {
        return config;
    }

    SocketAddress localAddress() {
        return bootstrap.config().localAddress();
    }

    SocketAddress remoteAddress() {
        return bootstrap.config().remoteAddress();
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

    private void internalValidate() {
        if (handler == null) {
            throw new IllegalStateException("handler not set");
        }
    }

    public VirtualBootstrap validate() {
        internalValidate();
        bootstrap.validate();
        return this;
    }

    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
        internalValidate();
        return wrapFuture(bootstrap.connect(remoteAddress, localAddress));
    }

    public ChannelFuture connect(SocketAddress remoteAddress) {
        internalValidate();
        return wrapFuture(bootstrap.connect(remoteAddress));
    }

    public ChannelFuture connect(InetAddress inetHost, int inetPort) {
        internalValidate();
        return wrapFuture(bootstrap.connect(inetHost, inetPort));
    }

    public ChannelFuture connect(String inetHost, int inetPort) {
        internalValidate();
        return wrapFuture(bootstrap.connect(inetHost, inetPort));
    }

    public ChannelFuture connect() {
        internalValidate();
        return wrapFuture(bootstrap.connect());
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public VirtualBootstrap clone() {
        Bootstrap bootstrap = this.bootstrap.clone();
        return new VirtualBootstrap(bootstrap, this);
    }

    public VirtualBootstrap clone(EventLoopGroup group) {
        Bootstrap bootstrap = this.bootstrap.clone(group);
        return new VirtualBootstrap(bootstrap, this);
    }

    @Override
    public String toString() {
        return StringUtil.simpleClassName(this) + '(' + config() + ')';
    }
}
