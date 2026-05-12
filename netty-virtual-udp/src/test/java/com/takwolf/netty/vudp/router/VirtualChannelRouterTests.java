package com.takwolf.netty.vudp.router;

import com.takwolf.netty.vudp.channel.ChildVirtualChannel;
import com.takwolf.netty.vudp.channel.FakeVirtualChannel;
import com.takwolf.netty.vudp.channel.VirtualChannel;
import io.netty.channel.DefaultChannelId;
import io.netty.channel.socket.DatagramPacket;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class VirtualChannelRouterTests {
    @Test
    public void test() throws Exception {
        int key = 1;
        ChildVirtualChannel channel = new FakeVirtualChannel();

        VirtualChannelRouter<Integer, Void, Void> router = new VirtualChannelRouter<>() {
            @Override
            public Optional<Integer> routeKey(DatagramPacket packet) {
                return Optional.of(key);
            }

            @Override
            public RouteResult<Void> routeMessage(Integer key, Void context, DatagramPacket packet) {
                return RouteResult.of();
            }
        };
        assertInstanceOf(DefaultChannelId.class, router.channelId(key));
        assertTrue(router.newContext(key).ok());
        assertNull(router.existingContext(key, channel));
        router.attachContext(key, null, channel);
    }
}
