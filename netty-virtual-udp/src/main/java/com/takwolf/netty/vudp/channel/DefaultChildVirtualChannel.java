package com.takwolf.netty.vudp.channel;

import com.takwolf.netty.vudp.util.EventExecutorUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.*;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class DefaultChildVirtualChannel extends AbstractVirtualChannel implements ChildVirtualChannel {
    private final ChannelId id = DefaultChannelId.newInstance();

    private final VirtualChannel parent;
    private final AttributeMap attrs = new DefaultAttributeMap();
    private final Unsafe unsafe = new DefaultUnsafe(this);
    private final CloseFuture closeFuture = new CloseFuture(this);
    private final ChannelPipeline pipeline = createPipeline(this);

    private volatile InetSocketAddress remoteAddress;
    private volatile EventLoop eventLoop;

    public DefaultChildVirtualChannel(VirtualChannel parent, InetSocketAddress remoteAddress) {
        this.parent = parent;
        this.remoteAddress = remoteAddress;
    }

    @Override
    public ChannelId id() {
        return id;
    }

    @Override
    public EventLoop eventLoop() {
        return eventLoop;
    }

    @Override
    public VirtualChannel parent() {
        return parent;
    }

    @Override
    public ChannelConfig config() {
        return NoOperationChannelConfig.INSTANCE;
    }

    @Override
    public boolean isOpen() {
        return !closeFuture.isDone();
    }

    @Override
    public boolean isRegistered() {
        return eventLoop != null;
    }

    @Override
    public boolean isActive() {
        return isOpen() && isRegistered();
    }

    @Override
    public ChannelMetadata metadata() {
        return parent.metadata();
    }

    @Override
    public InetSocketAddress localAddress() {
        return parent.localAddress();
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
        return closeFuture;
    }

    @Override
    public boolean isWritable() {
        return parent.isWritable();
    }

    @Override
    public long bytesBeforeUnwritable() {
        return parent.bytesBeforeUnwritable();
    }

    @Override
    public long bytesBeforeWritable() {
        return parent.bytesBeforeWritable();
    }

    @Override
    public Unsafe unsafe() {
        return unsafe;
    }

    @Override
    public ChannelPipeline pipeline() {
        return pipeline;
    }

    void doRegister(EventLoop eventLoop, ChannelPromise promise) {
        if (this.eventLoop == null) {
            this.eventLoop = eventLoop;
            pipeline.fireChannelRegistered();
            promise.setSuccess();
        } else {
            promise.setFailure(new IllegalStateException("eventLoop already registered"));
        }
    }

    void doClose(ChannelPromise promise) {
        if (!closeFuture.isDone()) {
            closeFuture.setClosed();
            pipeline.fireChannelInactive();
        }
        if (eventLoop != null) {
            eventLoop = null;
            pipeline.fireChannelUnregistered();
        }
        promise.setSuccess();
    }

    void doDeregister(ChannelPromise promise) {
        if (eventLoop != null) {
            eventLoop = null;
            pipeline.fireChannelUnregistered();
        }
        promise.setSuccess();
    }

    @Override
    public <T> Attribute<T> attr(AttributeKey<T> key) {
        return attrs.attr(key);
    }

    @Override
    public <T> boolean hasAttr(AttributeKey<T> key) {
        return attrs.hasAttr(key);
    }

    private static final class NoOperationChannelConfig implements ChannelConfig {
        static final NoOperationChannelConfig INSTANCE = new NoOperationChannelConfig();

        private NoOperationChannelConfig() {}

        @Override
        public Map<ChannelOption<?>, Object> getOptions() {
            return Collections.emptyMap();
        }

        @Override
        public boolean setOptions(Map<ChannelOption<?>, ?> options) {
            return false;
        }

        @Override
        public <T> T getOption(ChannelOption<T> option) {
            return null;
        }

        @Override
        public <T> boolean setOption(ChannelOption<T> option, T value) {
            return false;
        }

        @Override
        public int getConnectTimeoutMillis() {
            return 0;
        }

        @Override
        public ChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
            return this;
        }

        @Override
        public int getMaxMessagesPerRead() {
            return 0;
        }

        @Override
        public ChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
            return this;
        }

        @Override
        public int getWriteSpinCount() {
            return 0;
        }

        @Override
        public ChannelConfig setWriteSpinCount(int writeSpinCount) {
            return this;
        }

        @Override
        public ByteBufAllocator getAllocator() {
            return ByteBufAllocator.DEFAULT;
        }

        @Override
        public ChannelConfig setAllocator(ByteBufAllocator allocator) {
            return this;
        }

        @Override
        public <T extends RecvByteBufAllocator> T getRecvByteBufAllocator() {
            return null;
        }

        @Override
        public ChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
            return this;
        }

        @Override
        public boolean isAutoRead() {
            return true;
        }

        @Override
        public ChannelConfig setAutoRead(boolean autoRead) {
            return this;
        }

        @Override
        public boolean isAutoClose() {
            return true;
        }

        @Override
        public ChannelConfig setAutoClose(boolean autoClose) {
            return this;
        }

        @Override
        public int getWriteBufferHighWaterMark() {
            return 0;
        }

        @Override
        public ChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
            return this;
        }

        @Override
        public int getWriteBufferLowWaterMark() {
            return 0;
        }

        @Override
        public ChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
            return this;
        }

        @Override
        public MessageSizeEstimator getMessageSizeEstimator() {
            return DefaultMessageSizeEstimator.DEFAULT;
        }

        @Override
        public ChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
            return this;
        }

        @Override
        public WriteBufferWaterMark getWriteBufferWaterMark() {
            return WriteBufferWaterMark.DEFAULT;
        }

        @Override
        public ChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
            return this;
        }
    }

    private static final class DefaultUnsafe implements Unsafe {
        private final DefaultChildVirtualChannel channel;
        private final VoidChannelPromise voidPromise;

        DefaultUnsafe(DefaultChildVirtualChannel channel) {
            this.channel = channel;
            voidPromise = new VoidChannelPromise(channel, false);
        }

        private Unsafe wrappedUnsafe() {
            return channel.parent().wrappedChannel().unsafe();
        }

        private EventLoop wrappedEventLoop() {
            return channel.parent().wrappedChannel().eventLoop();
        }

        @Override
        public RecvByteBufAllocator.ExtendedHandle recvBufAllocHandle() {
            throw new UnsupportedOperationException();
        }

        @Override
        public InetSocketAddress localAddress() {
            return channel.localAddress();
        }

        @Override
        public InetSocketAddress remoteAddress() {
            return channel.remoteAddress();
        }

        @Override
        public void register(EventLoop eventLoop, ChannelPromise promise) {
            EventExecutorUtil.executeInEventLoop(eventLoop, () -> channel.doRegister(eventLoop, promise));
        }

        @Override
        public void bind(SocketAddress localAddress, ChannelPromise promise) {
            promise.setSuccess();
        }

        @Override
        public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            promise.setSuccess();
        }

        @Override
        public void disconnect(ChannelPromise promise) {
            close(promise);
        }

        @Override
        public void close(ChannelPromise promise) {
            EventLoop eventLoop = channel.eventLoop();
            if (eventLoop == null) {
                promise.setSuccess();
            } else {
                EventExecutorUtil.executeInEventLoop(eventLoop, () -> channel.doClose(promise));
            }
        }

        @Override
        public void closeForcibly() {
            close(channel.voidPromise());
        }

        @Override
        public void deregister(ChannelPromise promise) {
            EventLoop eventLoop = channel.eventLoop();
            if (eventLoop == null) {
                promise.setSuccess();
            } else {
                EventExecutorUtil.executeInEventLoop(eventLoop, () -> channel.doDeregister(promise));
            }
        }

        @Override
        public void beginRead() {}

        @Override
        public void write(Object message, ChannelPromise promise) {
            if (message instanceof ByteBuf) {
                DatagramPacket packet = new DatagramPacket((ByteBuf) message, remoteAddress());
                EventExecutorUtil.executeInEventLoop(wrappedEventLoop(), () -> wrappedUnsafe().write(packet, promise));
            } else if (message instanceof DatagramPacket) {
                DatagramPacket packet = (DatagramPacket) message;
                if (Objects.equals(packet.recipient(), remoteAddress())) {
                    EventExecutorUtil.executeInEventLoop(wrappedEventLoop(), () -> wrappedUnsafe().write(packet, promise));
                } else {
                    ReferenceCountUtil.release(packet);
                    promise.setFailure(new IllegalStateException("cannot send message to non-target remoteAddress"));
                }
            } else {
                ReferenceCountUtil.release(message);
                promise.setFailure(new IllegalStateException("unsupported message type"));
            }
        }

        @Override
        public void flush() {
            EventExecutorUtil.executeInEventLoop(wrappedEventLoop(), () -> wrappedUnsafe().flush());
        }

        @Override
        public ChannelPromise voidPromise() {
            return voidPromise;
        }

        @Override
        public ChannelOutboundBuffer outboundBuffer() {
            return null;
        }
    }

    private static final class CloseFuture extends DefaultChannelPromise {
        CloseFuture(Channel channel) {
            super(channel);
        }

        @Override
        public ChannelPromise setSuccess() {
            throw new IllegalStateException();
        }

        @Override
        public ChannelPromise setFailure(Throwable cause) {
            throw new IllegalStateException();
        }

        @Override
        public boolean trySuccess() {
            throw new IllegalStateException();
        }

        @Override
        public boolean tryFailure(Throwable cause) {
            throw new IllegalStateException();
        }

        @SuppressWarnings("UnusedReturnValue")
        boolean setClosed() {
            return super.trySuccess();
        }
    }
}
