package com.takwolf.netty.vudp.util;

import io.netty.channel.ChannelPromise;
import io.netty.channel.VoidChannelPromise;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public final class ChannelPromiseUtil {
    private ChannelPromiseUtil() {}

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ChannelPromiseUtil.class);

    public static void safeSetSuccess(ChannelPromise promise) {
        if (!(promise instanceof VoidChannelPromise) && !promise.trySuccess()) {
            logger.warn("Failed to mark a promise as success because it is done already: {}", promise);
        }
    }

    public static void safeSetFailure(ChannelPromise promise, Throwable cause) {
        if (!(promise instanceof VoidChannelPromise) && !promise.tryFailure(cause)) {
            logger.warn("Failed to mark a promise as failure because it's done already: {}", promise, cause);
        }
    }
}
