package com.takwolf.netty.vudp.channel;

import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramChannelConfig;

import java.net.InetSocketAddress;

public interface VirtualChannel extends Channel {
    DatagramChannel wrappedChannel();

    @Override
    DatagramChannelConfig config();

    @Override
    InetSocketAddress localAddress();

    @Override
    InetSocketAddress remoteAddress();
}
