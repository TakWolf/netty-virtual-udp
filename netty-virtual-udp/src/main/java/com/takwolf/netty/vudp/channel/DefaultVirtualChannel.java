package com.takwolf.netty.vudp.channel;

import com.takwolf.netty.vudp.util.ProxyChannelPromise;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramChannelConfig;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;

public final class DefaultVirtualChannel extends AbstractVirtualChannel implements VirtualChannel {
    private static final AttributeKey<DefaultVirtualChannel> ATTR_VIRTUAL_CHANNEL = AttributeKey.valueOf("virtualChannel");

    public static synchronized DefaultVirtualChannel instance(DatagramChannel wrappedChannel) {
        Attribute<DefaultVirtualChannel> attr = wrappedChannel.attr(ATTR_VIRTUAL_CHANNEL);
        DefaultVirtualChannel channel = attr.get();
        if (channel == null) {
            channel = new DefaultVirtualChannel(wrappedChannel);
            attr.set(channel);
        }
        return channel;
    }

    private final DatagramChannel wrappedChannel;
    private final ChannelPipeline pipeline;
    private final ChannelFuture closeFuture;

    private DefaultVirtualChannel(DatagramChannel wrappedChannel) {
        this.wrappedChannel = wrappedChannel;
        pipeline = createPipeline(this);
        closeFuture = new ProxyChannelPromise(this, wrappedChannel.closeFuture());
    }

    @Override
    public DatagramChannel wrappedChannel() {
        return wrappedChannel;
    }

    @Override
    public ChannelId id() {
        return wrappedChannel.id();
    }

    @Override
    public EventLoop eventLoop() {
        return wrappedChannel.eventLoop();
    }

    @Override
    public Channel parent() {
        return wrappedChannel.parent();
    }

    @Override
    public DatagramChannelConfig config() {
        return wrappedChannel.config();
    }

    @Override
    public boolean isOpen() {
        return wrappedChannel.isOpen();
    }

    @Override
    public boolean isRegistered() {
        return wrappedChannel.isRegistered();
    }

    @Override
    public boolean isActive() {
        return wrappedChannel.isActive();
    }

    @Override
    public ChannelMetadata metadata() {
        return wrappedChannel.metadata();
    }

    @Override
    public InetSocketAddress localAddress() {
        return wrappedChannel.localAddress();
    }

    @Override
    public InetSocketAddress remoteAddress() {
        return wrappedChannel.remoteAddress();
    }

    @Override
    public ChannelFuture closeFuture() {
        return closeFuture;
    }

    @Override
    public boolean isWritable() {
        return wrappedChannel.isWritable();
    }

    @Override
    public long bytesBeforeUnwritable() {
        return wrappedChannel.bytesBeforeUnwritable();
    }

    @Override
    public long bytesBeforeWritable() {
        return wrappedChannel.bytesBeforeWritable();
    }

    @Override
    public Unsafe unsafe() {
        return wrappedChannel.unsafe();
    }

    @Override
    public ChannelPipeline pipeline() {
        return pipeline;
    }
}
