package com.takwolf.demo.game.server;

import com.takwolf.netty.vudp.channel.ChildVirtualChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class GameServerRouteDecoder extends MessageToMessageDecoder<DatagramPacket> {
    @Override
    protected void decode(ChannelHandlerContext context, DatagramPacket packet, List<Object> out) {
        ChildVirtualChannel channel = (ChildVirtualChannel) context.channel();
        channel.remoteAddress(packet.sender());
        out.add(packet.content().retain());
    }
}
