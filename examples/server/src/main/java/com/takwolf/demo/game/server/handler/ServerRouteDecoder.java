package com.takwolf.demo.game.server.handler;

import com.takwolf.demo.game.server.domain.RouteMessage;
import com.takwolf.netty.vudp.channel.ChildVirtualChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class ServerRouteDecoder extends MessageToMessageDecoder<RouteMessage> {
    private long acceptedMaxSequence = -1;

    @Override
    protected void decode(ChannelHandlerContext context, RouteMessage message, List<Object> out) {
        if (message.getSequence() > acceptedMaxSequence) {
            acceptedMaxSequence = message.getSequence();
            ChildVirtualChannel channel = (ChildVirtualChannel) context.channel();
            channel.remoteAddress(message.getRemoteAddress());
        }
        out.add(message.content().retain());
    }
}
