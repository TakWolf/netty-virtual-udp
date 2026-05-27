package com.takwolf.demo.client;

import com.takwolf.demo.client.handler.ClientHandler;
import com.takwolf.demo.common.domain.CryptoInfo;
import com.takwolf.demo.common.domain.LoginInfo;
import com.takwolf.demo.common.handler.CryptoDecoder;
import com.takwolf.demo.common.handler.CryptoEncoder;
import com.takwolf.demo.common.service.UserService;
import com.takwolf.netty.vudp.bootstrap.VirtualBootstrap;
import com.takwolf.netty.vudp.channel.VirtualChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
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
                                    .addLast(new StringDecoder(StandardCharsets.UTF_8))
                                    .addLast(new StringEncoder(StandardCharsets.UTF_8))
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
