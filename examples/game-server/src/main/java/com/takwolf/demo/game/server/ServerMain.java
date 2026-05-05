package com.takwolf.demo.game.server;

import com.takwolf.demo.game.common.handler.GameCryptoEncoder;
import com.takwolf.demo.game.common.handler.GameMessageDecoder;
import com.takwolf.demo.game.common.handler.GameMessageEncoder;
import com.takwolf.demo.game.common.service.GameCrypto;
import com.takwolf.demo.game.common.service.UserService;
import com.takwolf.netty.vudp.bootstrap.ServerVirtualBootstrap;
import com.takwolf.netty.vudp.channel.ChildVirtualChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.ByteBufFormat;
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
                    .handler(new LoggingHandler("UDP Parent", LogLevel.INFO, ByteBufFormat.SIMPLE))
                    .router(new GameServerChannelRouter(userService))
                    .childHandler(new ChannelInitializer<ChildVirtualChannel>() {
                        @Override
                        protected void initChannel(ChildVirtualChannel channel) {
                            GameCrypto gameCrypto = channel.attr(GameCrypto.ATTR).get();

                            channel.pipeline()
                                    .addLast(new LoggingHandler("UDP Child", LogLevel.INFO, ByteBufFormat.SIMPLE))
                                    .addLast(new GameServerRouteDecoder())
                                    .addLast(new GameCryptoEncoder(gameCrypto.getConversationId(), gameCrypto.getServerKey(), GameCrypto.NONCE_PREFIX_SERVER, gameCrypto.getSequenceSeed()))
                                    .addLast(new GameMessageDecoder())
                                    .addLast(new GameMessageEncoder())
                                    .addLast(new GameServerHandler());
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
