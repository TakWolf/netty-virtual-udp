package com.takwolf.netty.vudp.bootstrap;

import com.takwolf.netty.vudp.channel.VirtualChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

class ForwardInboundHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private final VirtualChannel virtualChannel;

    ForwardInboundHandler(VirtualChannel virtualChannel) {
        this.virtualChannel = virtualChannel;
    }

    protected VirtualChannel virtualChannel() {
        return virtualChannel;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext context) {
        virtualChannel.pipeline().fireChannelRegistered();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext context) {
        virtualChannel.pipeline().fireChannelUnregistered();
    }

    @Override
    public void channelActive(ChannelHandlerContext context) {
        virtualChannel.pipeline().fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) {
        virtualChannel.pipeline().fireChannelInactive();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, DatagramPacket packet) {
        virtualChannel.pipeline().fireChannelRead(packet.content().retain());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext context) {
        virtualChannel.pipeline().fireChannelReadComplete();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext context, Object event) {
        virtualChannel.pipeline().fireUserEventTriggered(event);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext context) {
        virtualChannel.pipeline().fireChannelWritabilityChanged();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        virtualChannel.pipeline().fireExceptionCaught(cause);
    }
}
