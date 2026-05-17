package com.takwolf.demo.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext context, String message) {
        log.info("{} Channel Read:\n{}", context.channel(), message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {}
}
