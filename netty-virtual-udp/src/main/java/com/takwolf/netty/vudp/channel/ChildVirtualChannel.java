package com.takwolf.netty.vudp.channel;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

public interface ChildVirtualChannel extends Channel {
    @Override
    VirtualChannel parent();

    Object routeKey();

    @Override
    InetSocketAddress localAddress();

    @Override
    InetSocketAddress remoteAddress();

    void remoteAddress(InetSocketAddress remoteAddress);
}
