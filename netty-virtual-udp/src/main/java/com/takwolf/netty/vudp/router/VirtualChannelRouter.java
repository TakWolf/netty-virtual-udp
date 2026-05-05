package com.takwolf.netty.vudp.router;

import com.takwolf.netty.vudp.channel.ChildVirtualChannel;
import io.netty.channel.socket.DatagramPacket;

import java.util.Optional;

public interface VirtualChannelRouter<Key, RouteContext, Out> {
    Optional<Key> parseKey(DatagramPacket packet) throws Exception;

    default RouteResult<RouteContext> newContext(Key key) throws Exception {
        return RouteResult.of();
    }

    default RouteContext existingContext(Key key, ChildVirtualChannel channel) throws Exception {
        return null;
    }

    default void attachContext(Key key, RouteContext context, ChildVirtualChannel channel) throws Exception {}

    RouteResult<Out> routeMessage(Key key, RouteContext context, DatagramPacket packet) throws Exception;
}
