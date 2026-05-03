package com.takwolf.netty.vudp.router;

import io.netty.channel.socket.DatagramPacket;

import java.util.Optional;

public interface VirtualChannelRouter<Key, Message> {
    Optional<Key> routeKey(DatagramPacket packet) throws Exception;

    Optional<Message> routeMessage(Key key, DatagramPacket packet) throws Exception;
}
