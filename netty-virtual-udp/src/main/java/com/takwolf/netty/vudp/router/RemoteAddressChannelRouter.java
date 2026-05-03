package com.takwolf.netty.vudp.router;

import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;
import java.util.Optional;

public final class RemoteAddressChannelRouter implements VirtualChannelRouter<InetSocketAddress, ByteBuf> {
    public static final RemoteAddressChannelRouter INSTANCE = new RemoteAddressChannelRouter();

    @Override
    public Optional<InetSocketAddress> routeKey(DatagramPacket packet) {
        return Optional.of(packet.sender());
    }

    @Override
    public Optional<ByteBuf> routeMessage(InetSocketAddress key, DatagramPacket packet) {
        return Optional.of(packet.content().retain());
    }
}
