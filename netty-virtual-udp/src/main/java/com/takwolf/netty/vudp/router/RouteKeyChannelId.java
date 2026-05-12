package com.takwolf.netty.vudp.router;

import io.netty.channel.ChannelId;
import io.netty.channel.DefaultChannelId;
import org.jspecify.annotations.NonNull;

public final class RouteKeyChannelId implements ChannelId {
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    private static String hexBytes(byte[] value) {
        StringBuilder builder = new StringBuilder(value.length * 2);
        for (byte b : value) {
            builder.append(HEX_CHARS[(b & 0xFF) >>> 4]);
            builder.append(HEX_CHARS[b & 0xF]);
        }
        return builder.toString();
    }

    private static String hexInt(int value) {
        StringBuilder builder = new StringBuilder(8);
        for (int i = 28; i >= 0; i -= 4) {
            builder.append(HEX_CHARS[(value >>> i) & 0xF]);
        }
        return builder.toString();
    }

    private static String hexLong(long value) {
        StringBuilder builder = new StringBuilder(16);
        for (int i = 60; i >= 0; i -= 4) {
            builder.append(HEX_CHARS[(int) ((value >>> i) & 0xF)]);
        }
        return builder.toString();
    }

    private final String shortValue;
    private final String longValue;
    private final int hashCode;

    public RouteKeyChannelId(String key) {
        ChannelId id = DefaultChannelId.newInstance();
        shortValue = key;
        longValue = id.asLongText() + "-" + key;
        hashCode = id.hashCode();
    }

    public RouteKeyChannelId(byte[] key) {
        this(hexBytes(key));
    }

    public RouteKeyChannelId(int key) {
        this(hexInt(key));
    }

    public RouteKeyChannelId(long key) {
        this(hexLong(key));
    }

    @Override
    public String asShortText() {
        return shortValue;
    }

    @Override
    public String asLongText() {
        return longValue;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public int compareTo(@NonNull ChannelId other) {
        if (this == other) {
            return 0;
        }
        return asLongText().compareTo(other.asLongText());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RouteKeyChannelId)) {
            return false;
        }
        RouteKeyChannelId otherId = (RouteKeyChannelId) other;
        return hashCode == otherId.hashCode && longValue.equals(otherId.longValue);
    }

    @Override
    public String toString() {
        return shortValue;
    }
}
