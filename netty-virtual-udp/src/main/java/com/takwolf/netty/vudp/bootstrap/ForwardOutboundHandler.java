package com.takwolf.netty.vudp.bootstrap;

import com.takwolf.netty.vudp.channel.VirtualChannel;
import io.netty.channel.*;

import java.net.SocketAddress;

final class ForwardOutboundHandler extends ChannelHandlerAdapter implements ChannelOutboundHandler {
    private final VirtualChannel virtualChannel;
    
    ForwardOutboundHandler(VirtualChannel virtualChannel) {
        this.virtualChannel = virtualChannel;
    }

    private ChannelPromise cascadePromise(ChannelPromise promise) {
        return virtualChannel.newPromise().addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                promise.setSuccess();
            } else {
                promise.setFailure(future.cause());
            }
        });
    }

    @Override
    public void bind(ChannelHandlerContext context, SocketAddress localAddress, ChannelPromise promise) {
        virtualChannel.bind(localAddress, cascadePromise(promise));
    }

    @Override
    public void connect(ChannelHandlerContext context, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        virtualChannel.connect(remoteAddress, localAddress, cascadePromise(promise));
    }

    @Override
    public void disconnect(ChannelHandlerContext context, ChannelPromise promise) {
        virtualChannel.disconnect(cascadePromise(promise));
    }

    @Override
    public void close(ChannelHandlerContext context, ChannelPromise promise) {
        virtualChannel.close(cascadePromise(promise));
    }

    @Override
    public void deregister(ChannelHandlerContext context, ChannelPromise promise) {
        virtualChannel.deregister(cascadePromise(promise));
    }

    @Override
    public void read(ChannelHandlerContext context) {
        virtualChannel.read();
    }

    @Override
    public void write(ChannelHandlerContext context, Object message, ChannelPromise promise) {
        virtualChannel.write(message, cascadePromise(promise));
    }

    @Override
    public void flush(ChannelHandlerContext context) {
        virtualChannel.flush();
    }
}
