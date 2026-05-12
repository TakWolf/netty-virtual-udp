package com.takwolf.netty.vudp.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.jspecify.annotations.NonNull;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

public final class ProxyChannelPromise implements ChannelPromise {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ProxyChannelPromise.class);

    private final Channel channel;
    private final ChannelFuture rawFuture;

    private final Map<GenericFutureListener<? extends Future<? super Void>>, GenericFutureListener<? extends Future<? super Void>>> listeners = new IdentityHashMap<>();

    public ProxyChannelPromise(Channel channel, ChannelFuture rawFuture) {
        this.channel = checkNotNull(channel, "channel");
        this.rawFuture = checkNotNull(rawFuture, "rawFuture");
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public ChannelPromise setSuccess(Void result) {
        if (rawFuture instanceof ChannelPromise) {
            ((ChannelPromise) rawFuture).setSuccess(result);
            return this;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public boolean trySuccess(Void result) {
        if (rawFuture instanceof ChannelPromise) {
            return ((ChannelPromise) rawFuture).trySuccess(result);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public ChannelPromise setSuccess() {
        if (rawFuture instanceof ChannelPromise) {
            ((ChannelPromise) rawFuture).setSuccess();
            return this;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public boolean trySuccess() {
        if (rawFuture instanceof ChannelPromise) {
            return ((ChannelPromise) rawFuture).trySuccess();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public ChannelPromise setFailure(Throwable cause) {
        if (rawFuture instanceof ChannelPromise) {
            ((ChannelPromise) rawFuture).setFailure(cause);
            return this;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public boolean tryFailure(Throwable cause) {
        if (rawFuture instanceof ChannelPromise) {
            return ((ChannelPromise) rawFuture).tryFailure(cause);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public boolean setUncancellable() {
        if (rawFuture instanceof ChannelPromise) {
            return ((ChannelPromise) rawFuture).setUncancellable();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public boolean isSuccess() {
        return rawFuture.isSuccess();
    }

    @Override
    public boolean isCancellable() {
        return rawFuture.isCancellable();
    }

    @Override
    public Throwable cause() {
        return rawFuture.cause();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void notifyListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        try {
            ((GenericFutureListener) listener).operationComplete(this);
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("An exception was thrown by " + listener.getClass().getName() + ".operationComplete()", t);
            }
        }
    }

    private void addListener0(GenericFutureListener<? extends Future<? super Void>> listener) {
        if (rawFuture.isDone()) {
            notifyListener(listener);
            return;
        }
        synchronized (listeners) {
            if (listeners.containsKey(listener)) {
                return;
            }
            GenericFutureListener<? extends Future<? super Void>> proxyListener = (GenericFutureListener<Future<? super Void>>) future -> {
                synchronized (listeners) {
                    listeners.remove(listener);
                }
                notifyListener(listener);
            };
            listeners.put(listener, proxyListener);
            rawFuture.addListener(proxyListener);
        }
    }

    private void removeListener0(GenericFutureListener<? extends Future<? super Void>> listener) {
        synchronized (listeners) {
            GenericFutureListener<? extends Future<? super Void>> proxyListener = listeners.remove(listener);
            if (proxyListener != null) {
                rawFuture.removeListener(proxyListener);
            }
        }
    }

    @Override
    public ChannelPromise addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        checkNotNull(listener, "listener");
        addListener0(listener);
        return this;
    }

    @SafeVarargs
    @Override
    public final ChannelPromise addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
        checkNotNull(listeners, "listeners");
        for (GenericFutureListener<? extends Future<? super Void>> listener : listeners) {
            if (listener == null) {
                break;
            }
            addListener0(listener);
        }
        return this;
    }

    @Override
    public ChannelPromise removeListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        checkNotNull(listener, "listener");
        removeListener0(listener);
        return this;
    }

    @SafeVarargs
    @Override
    public final ChannelPromise removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
        checkNotNull(listeners, "listeners");
        for (GenericFutureListener<? extends Future<? super Void>> listener : listeners) {
            if (listener == null) {
                break;
            }
            removeListener0(listener);
        }
        return this;
    }

    @Override
    public ChannelPromise sync() throws InterruptedException {
        rawFuture.sync();
        return this;
    }

    @Override
    public ChannelPromise syncUninterruptibly() {
        rawFuture.syncUninterruptibly();
        return this;
    }

    @Override
    public ChannelPromise await() throws InterruptedException {
        rawFuture.await();
        return this;
    }

    @Override
    public ChannelPromise awaitUninterruptibly() {
        rawFuture.awaitUninterruptibly();
        return this;
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return rawFuture.await(timeout, unit);
    }

    @Override
    public boolean await(long timeoutMillis) throws InterruptedException {
        return rawFuture.await(timeoutMillis);
    }

    @Override
    public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
        return rawFuture.awaitUninterruptibly(timeout, unit);
    }

    @Override
    public boolean awaitUninterruptibly(long timeoutMillis) {
        return rawFuture.awaitUninterruptibly(timeoutMillis);
    }

    @Override
    public Void getNow() {
        return rawFuture.getNow();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return rawFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return rawFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return rawFuture.isDone();
    }

    @Override
    public Void get() throws InterruptedException, ExecutionException {
        return rawFuture.get();
    }

    @Override
    public Void get(long timeout, @NonNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return rawFuture.get(timeout, unit);
    }

    @Override
    public boolean isVoid() {
        return rawFuture.isVoid();
    }

    @Override
    public ChannelPromise unvoid() {
        if (rawFuture instanceof ChannelPromise) {
            ((ChannelPromise) rawFuture).unvoid();
            return this;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String toString() {
        return rawFuture.toString();
    }
}
