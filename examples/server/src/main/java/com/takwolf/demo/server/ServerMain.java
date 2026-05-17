package com.takwolf.demo.server;

import com.takwolf.demo.common.domain.CryptoInfo;
import com.takwolf.demo.common.handler.CryptoEncoder;
import com.takwolf.demo.common.handler.MessageDecoder;
import com.takwolf.demo.common.handler.MessageEncoder;
import com.takwolf.demo.common.service.UserService;
import com.takwolf.demo.server.handler.ServerChannelRouter;
import com.takwolf.demo.server.handler.ServerHandler;
import com.takwolf.demo.server.handler.ServerRouteDecoder;
import com.takwolf.netty.vudp.bootstrap.ServerVirtualBootstrap;
import com.takwolf.netty.vudp.channel.ChildVirtualChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ServerMain {
    public static void main(String[] args) throws InterruptedException {
        UserService userService = new UserService();

        EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        EventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

        try {
            ServerVirtualBootstrap bootstrap = new ServerVirtualBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioDatagramChannel.class)
                    .handler(new LoggingHandler("UDP Parent", LogLevel.INFO))
                    .router(new ServerChannelRouter(userService))
                    .childHandler(new ChannelInitializer<ChildVirtualChannel>() {
                        @Override
                        protected void initChannel(ChildVirtualChannel channel) {
                            CryptoInfo cryptoInfo = channel.attr(CryptoInfo.ATTR).get();

                            channel.pipeline()
                                    .addLast(new ServerRouteDecoder())
                                    .addLast(new CryptoEncoder(cryptoInfo.getConversationId(), cryptoInfo.getServerKey(), CryptoInfo.NONCE_PREFIX_SERVER, cryptoInfo.getSequenceSeed()))
                                    .addLast(new LoggingHandler("UDP Child", LogLevel.INFO))
                                    .addLast(new MessageDecoder())
                                    .addLast(new MessageEncoder())
                                    .addLast(new ServerHandler());
                        }
                    });

            Channel channel = bootstrap.bind(10000).sync().channel();
            channel.closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();
        }
    }
}
