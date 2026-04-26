package com.takwolf.netty.vudp.channel;

import io.netty.channel.*;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramChannelConfig;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public final class DefaultVirtualChannel extends AbstractVirtualChannel implements VirtualChannel {
    private static final AttributeKey<DefaultVirtualChannel> ATTR_WRAPPED_CHANNEL = AttributeKey.valueOf("wrappedChannel");

    public static synchronized DefaultVirtualChannel instance(DatagramChannel wrappedChannel) {
        Attribute<DefaultVirtualChannel> attr = wrappedChannel.attr(ATTR_WRAPPED_CHANNEL);
        DefaultVirtualChannel channel = attr.get();
        if (channel == null) {
            channel = new DefaultVirtualChannel(wrappedChannel);
            attr.set(channel);
        }
        return channel;
    }

    private final DatagramChannel wrappedChannel;
    private final Unsafe unsafe = new DefaultUnsafe(this);
    private final ChannelPipeline pipeline = createPipeline(this);

    private DefaultVirtualChannel(DatagramChannel wrappedChannel) {
        this.wrappedChannel = wrappedChannel;
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
        return null;
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
        return wrappedChannel.closeFuture();
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
        return unsafe;
    }

    @Override
    public ChannelPipeline pipeline() {
        return pipeline;
    }

    @Override
    public <T> Attribute<T> attr(AttributeKey<T> key) {
        return wrappedChannel.attr(key);
    }

    @Override
    public <T> boolean hasAttr(AttributeKey<T> key) {
        return wrappedChannel.hasAttr(key);
    }

    private static final class DefaultUnsafe implements Unsafe {
        private final VirtualChannel channel;

        DefaultUnsafe(VirtualChannel channel) {
            this.channel = channel;
        }

        private Unsafe wrappedUnsafe() {
            return channel.wrappedChannel().unsafe();
        }

        @SuppressWarnings("deprecation")
        @Override
        public RecvByteBufAllocator.Handle recvBufAllocHandle() {
            return wrappedUnsafe().recvBufAllocHandle();
        }

        @Override
        public SocketAddress localAddress() {
            return wrappedUnsafe().localAddress();
        }

        @Override
        public SocketAddress remoteAddress() {
            return wrappedUnsafe().remoteAddress();
        }

        @Override
        public void register(EventLoop eventLoop, ChannelPromise promise) {
            wrappedUnsafe().register(eventLoop, promise);
        }

        @Override
        public void bind(SocketAddress localAddress, ChannelPromise promise) {
            wrappedUnsafe().bind(localAddress, promise);
        }

        @Override
        public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            wrappedUnsafe().connect(remoteAddress, localAddress, promise);
        }

        @Override
        public void disconnect(ChannelPromise promise) {
            wrappedUnsafe().disconnect(promise);
        }

        @Override
        public void close(ChannelPromise promise) {
            wrappedUnsafe().close(promise);
        }

        @Override
        public void closeForcibly() {
            wrappedUnsafe().closeForcibly();
        }

        @Override
        public void deregister(ChannelPromise promise) {
            wrappedUnsafe().deregister(promise);
        }

        @Override
        public void beginRead() {
            wrappedUnsafe().beginRead();
        }

        @Override
        public void write(Object message, ChannelPromise promise) {
            wrappedUnsafe().write(message, promise);
        }

        @Override
        public void flush() {
            wrappedUnsafe().flush();
        }

        @Override
        public ChannelPromise voidPromise() {
            return wrappedUnsafe().voidPromise();
        }

        @Override
        public ChannelOutboundBuffer outboundBuffer() {
            return wrappedUnsafe().outboundBuffer();
        }
    }
}
