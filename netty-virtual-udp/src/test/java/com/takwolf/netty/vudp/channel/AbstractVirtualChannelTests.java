package com.takwolf.netty.vudp.channel;

import io.netty.channel.DefaultChannelPipeline;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class AbstractVirtualChannelTests {
    @Test
    public void testCreatePipeline() {
        FakeVirtualChannel channel = new FakeVirtualChannel();
        assertInstanceOf(DefaultChannelPipeline.class, channel.pipeline());
    }

    @Test
    public void testToString() {
        FakeVirtualChannel channel = new FakeVirtualChannel();
        assertEquals("[id: 0x" + channel.id().asShortText() + "]", channel.toString());
        channel.setLocalAddress(new InetSocketAddress("127.0.0.1", 10000));
        assertEquals("[id: 0x" + channel.id().asShortText() + "]", channel.toString());
        channel.setActive(true);
        assertEquals("[id: 0x" + channel.id().asShortText() + ", L:/127.0.0.1:10000]", channel.toString());
        channel.setRemoteAddress(new InetSocketAddress("192.168.1.1", 12345));
        channel.setActive(false);
        assertEquals("[id: 0x" + channel.id().asShortText() + ", L:/127.0.0.1:10000 ! R:/192.168.1.1:12345]", channel.toString());
        channel.setActive(true);
        assertEquals("[id: 0x" + channel.id().asShortText() + ", L:/127.0.0.1:10000 - R:/192.168.1.1:12345]", channel.toString());
    }
}
