package com.takwolf.netty.vudp.channel;

import io.netty.channel.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.net.SocketAddress;

public class FakeVirtualChannel extends AbstractVirtualChannel {
    private final ChannelId id = DefaultChannelId.newInstance();
    private final Unsafe unsafe = new FakeUnsafe();
    private final ChannelPipeline pipeline = createPipeline(this);

    private volatile SocketAddress localAddress;
    private volatile SocketAddress remoteAddress;
    private volatile boolean active;

    public void setLocalAddress(SocketAddress localAddress) {
        this.localAddress = localAddress;
    }

    public void setRemoteAddress(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public ChannelId id() {
        return id;
    }

    @Override
    public EventLoop eventLoop() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Channel parent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelConfig config() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOpen() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRegistered() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public ChannelMetadata metadata() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SocketAddress localAddress() {
        return localAddress;
    }

    @Override
    public SocketAddress remoteAddress() {
        return remoteAddress;
    }

    @Override
    public ChannelFuture closeFuture() {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> boolean hasAttr(AttributeKey<T> key) {
        throw new UnsupportedOperationException();
    }

    private static final class FakeUnsafe implements Unsafe {
        @Override
        public RecvByteBufAllocator.ExtendedHandle recvBufAllocHandle() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SocketAddress localAddress() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SocketAddress remoteAddress() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void register(EventLoop eventLoop, ChannelPromise promise) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void bind(SocketAddress localAddress, ChannelPromise promise) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void disconnect(ChannelPromise promise) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close(ChannelPromise promise) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void closeForcibly() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deregister(ChannelPromise promise) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void beginRead() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void write(Object msg, ChannelPromise promise) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void flush() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ChannelPromise voidPromise() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ChannelOutboundBuffer outboundBuffer() {
            throw new UnsupportedOperationException();
        }
    }
}
