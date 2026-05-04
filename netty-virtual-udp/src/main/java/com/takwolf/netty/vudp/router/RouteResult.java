package com.takwolf.netty.vudp.router;

public final class RouteResult<T> {
    private static final RouteResult<?> EMPTY = new RouteResult<>(true, null);
    private static final RouteResult<?> NONE = new RouteResult<>(false, null);

    public static <T> RouteResult<T> of(T value) {
        return new RouteResult<>(true, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> RouteResult<T> of() {
        return (RouteResult<T>) EMPTY;
    }

    @SuppressWarnings("unchecked")
    public static <T> RouteResult<T> none() {
        return (RouteResult<T>) NONE;
    }

    private final boolean ok;
    private final T value;

    private RouteResult(boolean ok, T value) {
        this.ok = ok;
        this.value = value;
    }

    public boolean ok() {
        return ok;
    }

    public T get() {
        return value;
    }
}
