package com.takwolf.netty.vudp.bootstrap;

import com.takwolf.netty.vudp.router.VirtualChannelRouter;
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

public final class ServerVirtualBootstrapConfig {
    private final ServerVirtualBootstrap bootstrap;

    ServerVirtualBootstrapConfig(ServerVirtualBootstrap bootstrap) {
        this.bootstrap = ObjectUtil.checkNotNull(bootstrap, "bootstrap");
    }

    public SocketAddress localAddress() {
        return bootstrap.localAddress();
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

    public VirtualChannelRouter<?, ?, ?> router() {
        return bootstrap.router();
    }

    public Map<ChannelOption<?>, Object> options() {
        return bootstrap.options();
    }

    public Map<AttributeKey<?>, Object> attrs() {
        return bootstrap.attrs();
    }

    public EventLoopGroup childGroup() {
        return bootstrap.childGroup();
    }

    public ChannelHandler childHandler() {
        return bootstrap.childHandler();
    }

    public Map<ChannelOption<?>, Object> childOptions() {
        return bootstrap.childOptions();
    }

    public Map<AttributeKey<?>, Object> childAttrs() {
        return bootstrap.childAttrs();
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

        VirtualChannelRouter<?, ?, ?> router = router();
        if (router != null) {
            builder.append("router: ")
                    .append(router)
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

        ChannelHandler childHandler = childHandler();
        if (childHandler != null) {
            builder.append("childHandler: ");
            builder.append(childHandler);
            builder.append(", ");
        }

        Map<ChannelOption<?>, Object> childOptions = childOptions();
        if (!childOptions.isEmpty()) {
            builder.append("childOptions: ");
            builder.append(childOptions);
            builder.append(", ");
        }

        Map<AttributeKey<?>, Object> childAttrs = childAttrs();
        if (!childAttrs.isEmpty()) {
            builder.append("childAttrs: ");
            builder.append(childAttrs);
            builder.append(", ");
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
