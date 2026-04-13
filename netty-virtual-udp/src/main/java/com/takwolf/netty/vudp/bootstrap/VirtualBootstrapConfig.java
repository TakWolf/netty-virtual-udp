package com.takwolf.netty.vudp.bootstrap;

import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.util.AttributeKey;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.net.SocketAddress;
import java.util.Map;

public final class VirtualBootstrapConfig {
    private final VirtualBootstrap bootstrap;

    VirtualBootstrapConfig(VirtualBootstrap bootstrap) {
        this.bootstrap = ObjectUtil.checkNotNull(bootstrap, "bootstrap");
    }

    public SocketAddress localAddress() {
        return bootstrap.localAddress();
    }

    public SocketAddress remoteAddress() {
        return bootstrap.remoteAddress();
    }

    public EventLoopGroup group() {
        return bootstrap.group();
    }

    public ChannelFactory<? extends DatagramChannel> channelFactory() {
        return bootstrap.channelFactory();
    }

    public ChannelHandler handler() {
        return bootstrap.handler();
    }

    public Map<ChannelOption<?>, Object> options() {
        return bootstrap.options();
    }

    public Map<AttributeKey<?>, Object> attrs() {
        return bootstrap.attrs();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append(StringUtil.simpleClassName(this))
                .append('(');

        SocketAddress localAddress = localAddress();
        if (localAddress != null) {
            builder.append("localAddress: ")
                    .append(localAddress)
                    .append(", ");
        }

        SocketAddress remoteAddress = remoteAddress();
        if (remoteAddress != null) {
            builder.append("remoteAddress: ")
                    .append(remoteAddress)
                    .append(", ");
        }

        EventLoopGroup group = group();
        if (group != null) {
            builder.append("group: ")
                    .append(StringUtil.simpleClassName(group))
                    .append(", ");
        }

        ChannelFactory<? extends DatagramChannel> channelFactory = channelFactory();
        if (channelFactory != null) {
            builder.append("channelFactory: ")
                    .append(channelFactory)
                    .append(", ");
        }

        ChannelHandler handler = handler();
        if (handler != null) {
            builder.append("handler: ")
                    .append(handler)
                    .append(", ");
        }

        Map<ChannelOption<?>, Object> options = options();
        if (!options.isEmpty()) {
            builder.append("options: ")
                    .append(options)
                    .append(", ");
        }

        Map<AttributeKey<?>, Object> attrs = attrs();
        if (!attrs.isEmpty()) {
            builder.append("attrs: ")
                    .append(attrs)
                    .append(", ");
        }

        if (builder.charAt(builder.length() - 1) == '(') {
            builder.append(')');
        } else {
            builder.setCharAt(builder.length() - 2, ')');
            builder.setLength(builder.length() - 1);
        }
        return builder.toString();
    }
}
