package com.takwolf.netty.vudp.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

import java.net.SocketAddress;

abstract class AbstractVirtualChannel implements Channel {
    private boolean strValActive;
    private String strVal;

    @Override
    public String toString() {
        boolean active = isActive();
        if (strValActive == active && strVal != null) {
            return strVal;
        }

        ChannelId id = id();
        SocketAddress remoteAddress = remoteAddress();
        SocketAddress localAddress = localAddress();
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
