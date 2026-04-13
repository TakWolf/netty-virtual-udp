package com.takwolf.demo.game.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class GameMessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out) {
        byte[] data = new byte[in.readableBytes()];
        in.readBytes(data);

        String message = new String(data, StandardCharsets.UTF_8);
        out.add(message);
    }
}
