package com.takwolf.netty.vudp.channel;

import com.takwolf.netty.vudp.util.ChannelPromiseUtil;
import com.takwolf.netty.vudp.util.EventExecutorUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.ObjectUtil;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class DefaultChildVirtualChannel extends AbstractVirtualChannel implements ChildVirtualChannel {
    private final ChannelId id = DefaultChannelId.newInstance();
    private final VirtualChannel parent;
    private final DefaultUnsafe unsafe = new DefaultUnsafe(this);
    private final ChannelPipeline pipeline = createPipeline(this);
    private final CloseFuture closeFuture = new CloseFuture(this);

    private volatile InetSocketAddress remoteAddress;
    private volatile EventLoop eventLoop;
    private volatile boolean registered;

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
        EventLoop eventLoop = this.eventLoop;
        if (eventLoop == null) {
            throw new IllegalStateException("channel not registered to an event loop");
        }
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
        return registered;
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
    public DefaultUnsafe unsafe() {
        return unsafe;
    }

    @Override
    public ChannelPipeline pipeline() {
        return pipeline;
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

    public static final class DefaultUnsafe implements Unsafe {
        private final DefaultChildVirtualChannel channel;
        private final VoidChannelPromise voidPromise;

        private volatile boolean readTouched;

        DefaultUnsafe(DefaultChildVirtualChannel channel) {
            this.channel = channel;
            voidPromise = new VoidChannelPromise(channel, false);
        }

        public EventLoop tryEventLoop() {
            return channel.eventLoop;
        }

        private EventLoop tryWrappedEventLoop() {
            try {
                return channel.parent.wrappedChannel().eventLoop();
            } catch (Exception e) {
                return null;
            }
        }

        private Unsafe wrappedUnsafe() {
            return channel.parent.wrappedChannel().unsafe();
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
            ObjectUtil.checkNotNull(eventLoop, "eventLoop");

            if (!promise.setUncancellable()) {
                return;
            }

            EventExecutorUtil.executeInEventLoop(eventLoop, () -> {
                if (channel.closeFuture.isDone()) {
                    ChannelPromiseUtil.safeSetFailure(promise, new IllegalStateException("channel is closed already"));
                    return;
                }

                if (channel.registered) {
                    ChannelPromiseUtil.safeSetFailure(promise, new IllegalStateException("channel registered to an event loop already"));
                    return;
                }

                channel.registered = true;
                channel.eventLoop = eventLoop;
                channel.pipeline.fireChannelRegistered();
                channel.pipeline.fireChannelActive();
                ChannelPromiseUtil.safeSetSuccess(promise);
            });
        }

        @Override
        public void bind(SocketAddress localAddress, ChannelPromise promise) {
            ChannelPromiseUtil.safeSetFailure(promise, new UnsupportedOperationException());
        }

        @Override
        public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            ChannelPromiseUtil.safeSetFailure(promise, new UnsupportedOperationException());
        }

        @Override
        public void disconnect(ChannelPromise promise) {
            ChannelPromiseUtil.safeSetFailure(promise, new UnsupportedOperationException());
        }

        @Override
        public void close(ChannelPromise promise) {
            if (!promise.setUncancellable()) {
                return;
            }

            EventLoop eventLoop = tryEventLoop();
            if (eventLoop == null) {
                if (!channel.closeFuture.isDone()) {
                    channel.closeFuture.setClosed();
                }
                ChannelPromiseUtil.safeSetSuccess(promise);
                return;
            }

            EventExecutorUtil.executeInEventLoop(eventLoop, () -> {
                if (channel.closeFuture.isDone()) {
                    ChannelPromiseUtil.safeSetSuccess(promise);
                    return;
                }
                channel.closeFuture.setClosed();

                if (channel.registered) {
                    channel.registered = false;
                    channel.eventLoop = null;
                    if (readTouched) {
                        readTouched = false;
                        channel.pipeline.fireChannelReadComplete();
                    }
                    channel.pipeline.fireChannelInactive();
                    channel.pipeline.fireChannelUnregistered();
                }
                ChannelPromiseUtil.safeSetSuccess(promise);
            });
        }

        @Override
        public void closeForcibly() {
            close(voidPromise());
        }

        @Override
        public void deregister(ChannelPromise promise) {
            if (!promise.setUncancellable()) {
                return;
            }

            if (channel.closeFuture.isDone()) {
                ChannelPromiseUtil.safeSetFailure(promise, new IllegalStateException("channel is closed already"));
                return;
            }

            EventLoop eventLoop = tryEventLoop();
            if (eventLoop == null) {
                ChannelPromiseUtil.safeSetSuccess(promise);
                return;
            }

            EventExecutorUtil.executeInEventLoop(eventLoop, () -> {
                if (channel.registered) {
                    channel.registered = false;
                    channel.eventLoop = null;
                    if (readTouched) {
                        readTouched = false;
                        channel.pipeline.fireChannelReadComplete();
                    }
                    channel.pipeline.fireChannelInactive();
                    channel.pipeline.fireChannelUnregistered();
                }
                ChannelPromiseUtil.safeSetSuccess(promise);
            });
        }

        @Override
        public void beginRead() {}

        public void read(Object message) {
            EventLoop eventLoop = tryEventLoop();
            if (eventLoop == null || !channel.isActive()) {
                ReferenceCountUtil.release(message);
                return;
            }

            EventExecutorUtil.executeInEventLoop(eventLoop, () -> {
                readTouched = true;
                channel.pipeline.fireChannelRead(message);
            });
        }

        public void readComplete() {
            EventLoop eventLoop = tryEventLoop();
            if (eventLoop == null || !channel.isActive()) {
                return;
            }

            EventExecutorUtil.executeInEventLoop(eventLoop, () -> {
                if (readTouched) {
                    readTouched = false;
                    channel.pipeline.fireChannelReadComplete();
                }
            });
        }

        @Override
        public void write(Object message, ChannelPromise promise) {
            if (!channel.isActive()) {
                ReferenceCountUtil.release(message);
                ChannelPromiseUtil.safeSetFailure(promise, new IllegalStateException("channel not registered to an event loop"));
                return;
            }

            EventLoop eventLoop = tryWrappedEventLoop();
            if (eventLoop == null) {
                ReferenceCountUtil.release(message);
                ChannelPromiseUtil.safeSetFailure(promise, new IllegalStateException("channel not registered to an event loop"));
                return;
            }

            if (message instanceof ByteBuf) {
                DatagramPacket packet = new DatagramPacket((ByteBuf) message, remoteAddress());
                EventExecutorUtil.executeInEventLoop(eventLoop, () -> wrappedUnsafe().write(packet, promise));
            } else if (message instanceof DatagramPacket) {
                DatagramPacket packet = (DatagramPacket) message;
                if (Objects.equals(packet.recipient(), remoteAddress())) {
                    EventExecutorUtil.executeInEventLoop(eventLoop, () -> wrappedUnsafe().write(packet, promise));
                } else {
                    ReferenceCountUtil.release(packet);
                    ChannelPromiseUtil.safeSetFailure(promise, new IllegalStateException("cannot send message to non-target remoteAddress"));
                }
            } else {
                ReferenceCountUtil.release(message);
                ChannelPromiseUtil.safeSetFailure(promise, new IllegalStateException("unsupported message type"));
            }
        }

        @Override
        public void flush() {
            if (!channel.isActive()) {
                return;
            }

            EventLoop eventLoop = tryWrappedEventLoop();
            if (eventLoop == null) {
                return;
            }

            EventExecutorUtil.executeInEventLoop(eventLoop, () -> wrappedUnsafe().flush());
        }

        public void writabilityChanged() {
            EventLoop eventLoop = tryEventLoop();
            if (eventLoop == null || !channel.isActive()) {
                return;
            }

            EventExecutorUtil.executeInEventLoop(eventLoop, channel.pipeline::fireChannelWritabilityChanged);
        }

        @Override
        public ChannelPromise voidPromise() {
            return voidPromise;
        }

        @Override
        public RecvByteBufAllocator.ExtendedHandle recvBufAllocHandle() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ChannelOutboundBuffer outboundBuffer() {
            throw new UnsupportedOperationException();
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
