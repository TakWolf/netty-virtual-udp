package com.takwolf.demo.game.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

public class GameMessageEncoder extends MessageToByteEncoder<String> {
    @Override
    protected void encode(ChannelHandlerContext context, String message, ByteBuf out) {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        out.writeBytes(data);
    }
}
