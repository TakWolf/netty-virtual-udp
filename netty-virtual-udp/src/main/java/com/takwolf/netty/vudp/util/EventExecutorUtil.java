package com.takwolf.netty.vudp.util;

import io.netty.util.concurrent.EventExecutor;

public final class EventExecutorUtil {
    private EventExecutorUtil() {}

    public static void executeInEventLoop(EventExecutor executor, Runnable runnable) {
        if (executor.inEventLoop()) {
            runnable.run();
        } else {
            executor.execute(runnable);
        }
    }
}
