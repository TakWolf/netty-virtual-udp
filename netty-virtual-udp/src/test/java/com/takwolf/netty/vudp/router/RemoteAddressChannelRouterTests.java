package com.takwolf.netty.vudp.router;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RemoteAddressChannelRouterTests {
    @Test
    public void test() throws Exception {
        ByteBuf data = Unpooled.wrappedBuffer(new byte[0]);
        InetSocketAddress recipient = new InetSocketAddress("1.1.1.1", 1000);
        InetSocketAddress sender = new InetSocketAddress("2.2.2.2", 2000);
        DatagramPacket packet = new DatagramPacket(data, recipient, sender);

        RemoteAddressChannelRouter router = RemoteAddressChannelRouter.INSTANCE;
        InetSocketAddress key = router.parseKey(packet).orElseThrow();
        assertEquals(sender, key);
        assertTrue(router.newContext(key).ok());
        assertTrue(router.routeMessage(key, null, packet).ok());
    }
}
