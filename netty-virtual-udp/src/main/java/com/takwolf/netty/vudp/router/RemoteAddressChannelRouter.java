package com.takwolf.netty.vudp.router;

import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;
import java.util.Optional;

public final class RemoteAddressChannelRouter implements VirtualChannelRouter<InetSocketAddress, Void, ByteBuf> {
    public static final RemoteAddressChannelRouter INSTANCE = new RemoteAddressChannelRouter();

    @Override
    public Optional<InetSocketAddress> routeKey(DatagramPacket packet) {
        return Optional.of(packet.sender());
    }

    @Override
    public RouteResult<ByteBuf> routeMessage(InetSocketAddress key, Void context, DatagramPacket packet) {
        return RouteResult.of(packet.content().retain());
    }
}
