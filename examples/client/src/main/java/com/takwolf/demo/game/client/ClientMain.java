package com.takwolf.demo.game.client;

import com.takwolf.demo.game.client.handler.ClientHandler;
import com.takwolf.demo.game.common.domain.CryptoInfo;
import com.takwolf.demo.game.common.domain.LoginInfo;
import com.takwolf.demo.game.common.handler.CryptoDecoder;
import com.takwolf.demo.game.common.handler.CryptoEncoder;
import com.takwolf.demo.game.common.handler.MessageDecoder;
import com.takwolf.demo.game.common.handler.MessageEncoder;
import com.takwolf.demo.game.common.service.UserService;
import com.takwolf.netty.vudp.bootstrap.VirtualBootstrap;
import com.takwolf.netty.vudp.channel.VirtualChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ClientMain {
    public static void main(String[] args) throws InterruptedException, IOException {
        UserService userService = new UserService();
        LoginInfo loginInfo = userService.getLoginInfo(1).orElseThrow();
        CryptoInfo cryptoInfo = CryptoInfo.fromLoginInfo(loginInfo, 0);

        EventLoopGroup ioGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());

        try {
            VirtualBootstrap bootstrap = new VirtualBootstrap()
                    .group(ioGroup)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<VirtualChannel>() {
                        @Override
                        protected void initChannel(VirtualChannel channel) {
                            channel.pipeline()
                                    .addLast(new CryptoDecoder(cryptoInfo.getConversationId(), cryptoInfo.getServerKey(), CryptoInfo.NONCE_PREFIX_SERVER))
                                    .addLast(new CryptoEncoder(cryptoInfo.getConversationId(), cryptoInfo.getClientKey(), CryptoInfo.NONCE_PREFIX_CLIENT, cryptoInfo.getSequenceSeed()))
                                    .addLast(new LoggingHandler("UDP", LogLevel.INFO))
                                    .addLast(new MessageDecoder())
                                    .addLast(new MessageEncoder())
                                    .addLast(new ClientHandler());
                        }
                    });

            Channel channel = bootstrap.connect("127.0.0.1", 10000).sync().channel();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
                while (channel.isActive()) {
                    String message = reader.readLine();
                    if (Objects.equals(message, "bye")) {
                        break;
                    } else {
                        channel.writeAndFlush(message);
                    }
                }
            }

            channel.close().sync();
        } finally {
            ioGroup.shutdownGracefully().sync();
        }
    }
}
