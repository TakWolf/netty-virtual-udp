package com.takwolf.netty.vudp.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.DefaultChannelPipeline;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketAddress;
import java.util.Objects;

abstract class AbstractVirtualChannel implements Channel {
    protected static ChannelPipeline createPipeline(Channel channel) {
        try {
            Constructor<DefaultChannelPipeline> constructor = DefaultChannelPipeline.class.getDeclaredConstructor(Channel.class);
            constructor.setAccessible(true);
            return constructor.newInstance(channel);
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private SocketAddress strValLocalAddress;
    private SocketAddress strValRemoteAddress;
    private boolean strValActive;
    private String strVal;

    @Override
    public String toString() {
        SocketAddress localAddress = localAddress();
        SocketAddress remoteAddress = remoteAddress();
        boolean active = isActive();
        if (Objects.equals(strValLocalAddress, localAddress) && Objects.equals(strValRemoteAddress, remoteAddress) && strValActive == active && strVal != null) {
            return strVal;
        }

        ChannelId id = id();
        if (remoteAddress != null) {
            StringBuilder builder = new StringBuilder(96)
                    .append("[id: 0x")
                    .append(id.asShortText())
                    .append(", L:")
                    .append(localAddress)
                    .append(active? " - " : " ! ")
                    .append("R:")
                    .append(remoteAddress)
                    .append(']');
            strVal = builder.toString();
        } else if (localAddress != null) {
            StringBuilder builder = new StringBuilder(64)
                    .append("[id: 0x")
                    .append(id.asShortText())
                    .append(", L:")
                    .append(localAddress)
                    .append(']');
            strVal = builder.toString();
        } else {
            StringBuilder builder = new StringBuilder(16)
                    .append("[id: 0x")
                    .append(id.asShortText())
                    .append(']');
            strVal = builder.toString();
        }

        strValLocalAddress = localAddress;
        strValRemoteAddress = remoteAddress;
        strValActive = active;
        return strVal;
    }

    @Override
    public int compareTo(Channel other) {
        if (this == other) {
            return 0;
        }
        return id().compareTo(other.id());
    }
}
