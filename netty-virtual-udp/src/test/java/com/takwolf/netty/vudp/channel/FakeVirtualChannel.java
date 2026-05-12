package com.takwolf.netty.vudp.channel;

import io.netty.channel.*;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramChannelConfig;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class FakeVirtualChannel extends AbstractVirtualChannel implements VirtualChannel, ChildVirtualChannel {
    private final ChannelId id = DefaultChannelId.newInstance();
    private final Unsafe unsafe = new FakeUnsafe();
    private final ChannelPipeline pipeline = createPipeline(this);

    private volatile InetSocketAddress localAddress;
    private volatile InetSocketAddress remoteAddress;
    private volatile boolean active;

    @Override
    public ChannelId id() {
        return id;
    }

    @Override
    public EventLoop eventLoop() {
        throw new UnsupportedOperationException();
    }

    @Override
    public VirtualChannel parent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DatagramChannel wrappedChannel() {
        return null;
    }

    @Override
    public DatagramChannelConfig config() {
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

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public ChannelMetadata metadata() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InetSocketAddress localAddress() {
        return localAddress;
    }

    public void localAddress(InetSocketAddress localAddress) {
        this.localAddress = localAddress;
    }

    @Override
    public InetSocketAddress remoteAddress() {
        return remoteAddress;
    }

    @Override
    public void remoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
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
