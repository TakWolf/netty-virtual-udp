package com.takwolf.demo.game.server;

import com.takwolf.netty.vudp.channel.ChildVirtualChannel;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class GameServerRouteDecoder extends MessageToMessageDecoder<GameServerChannelRouter.RouteMessage> {
    private long acceptedMaxSequence = -1;

    @Override
    protected void decode(ChannelHandlerContext context, GameServerChannelRouter.RouteMessage message, List<Object> out) {
        if (message.getSequence() > acceptedMaxSequence) {
            acceptedMaxSequence = message.getSequence();
            ChildVirtualChannel channel = (ChildVirtualChannel) context.channel();
            channel.remoteAddress(message.getRemoteAddress());
        }
        out.add(Unpooled.wrappedBuffer(message.getData()));
    }
}
