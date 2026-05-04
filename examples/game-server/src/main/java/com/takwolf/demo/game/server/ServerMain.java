package com.takwolf.demo.game.server;

import com.takwolf.demo.game.common.GameMessageDecoder;
import com.takwolf.demo.game.common.GameMessageEncoder;
import com.takwolf.netty.vudp.bootstrap.ServerVirtualBootstrap;
import com.takwolf.netty.vudp.channel.ChildVirtualChannel;
import com.takwolf.netty.vudp.router.RemoteAddressChannelRouter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.ByteBufFormat;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ServerMain {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        EventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

        try {
            ServerVirtualBootstrap bootstrap = new ServerVirtualBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioDatagramChannel.class)
                    .handler(new LoggingHandler("UDP Parent", LogLevel.INFO, ByteBufFormat.SIMPLE))
                    .router(RemoteAddressChannelRouter.INSTANCE)
                    .childHandler(new ChannelInitializer<ChildVirtualChannel>() {
                        @Override
                        protected void initChannel(ChildVirtualChannel channel) {
                            channel.pipeline()
                                    .addLast(new LoggingHandler("UDP Child", LogLevel.INFO, ByteBufFormat.SIMPLE))
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
