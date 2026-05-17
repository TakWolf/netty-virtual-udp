package com.takwolf.demo.server.domain;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;
import lombok.Getter;

import java.net.InetSocketAddress;

@Getter
public final class RouteMessage extends DefaultByteBufHolder {
    private final InetSocketAddress remoteAddress;
    private final long sequence;

    public RouteMessage(InetSocketAddress remoteAddress, long sequence, ByteBuf data) {
        super(data);
        this.remoteAddress = remoteAddress;
        this.sequence = sequence;
    }
}
