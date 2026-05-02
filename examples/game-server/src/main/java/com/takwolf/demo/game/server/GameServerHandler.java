package com.takwolf.demo.game.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GameServerHandler extends SimpleChannelInboundHandler<String> {
    private static final ChannelGroup allChannels = new DefaultChannelGroup("all", GlobalEventExecutor.INSTANCE);

    @Override
    public void channelActive(ChannelHandlerContext context) {
        allChannels.add(context.channel());
        context.fireChannelActive();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, String message) {
        allChannels.writeAndFlush("Some one say: " + message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {}
}
