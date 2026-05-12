package com.takwolf.netty.vudp.bootstrap;

import com.takwolf.netty.vudp.channel.DefaultVirtualChannel;
import com.takwolf.netty.vudp.util.ProxyChannelPromise;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.DatagramChannel;

abstract class AbstractVirtualBootstrap {
    protected static ChannelFuture wrapFuture(ChannelFuture future) {
        DefaultVirtualChannel channel = DefaultVirtualChannel.instance((DatagramChannel) future.channel());
        return new ProxyChannelPromise(channel, future);
    }
}
