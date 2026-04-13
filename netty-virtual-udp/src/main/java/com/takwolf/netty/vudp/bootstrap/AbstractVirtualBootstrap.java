package com.takwolf.netty.vudp.bootstrap;

import com.takwolf.netty.vudp.channel.DefaultVirtualChannel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramChannel;

abstract class AbstractVirtualBootstrap {
    protected static ChannelFuture wrapFuture(ChannelFuture oldFuture) {
        ChannelPromise newFuture = DefaultVirtualChannel.instance((DatagramChannel) oldFuture.channel()).newPromise();
        oldFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                newFuture.setSuccess();
            } else {
                newFuture.setFailure(future.cause());
            }
        });
        return newFuture;
    }
}
