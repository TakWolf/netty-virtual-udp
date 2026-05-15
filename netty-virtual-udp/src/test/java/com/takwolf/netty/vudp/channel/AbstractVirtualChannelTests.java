package com.takwolf.netty.vudp.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.DefaultChannelPipeline;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AbstractVirtualChannelTests {
    @Test
    public void testCreatePipeline() {
        Channel channel = mock(Channel.class);
        ChannelPipeline pipeline = AbstractVirtualChannel.createPipeline(channel);

        assertInstanceOf(DefaultChannelPipeline.class, pipeline);
        assertSame(channel, pipeline.channel());
    }

    @Test
    public void testToString1() {
        ChannelId channelId = mock(ChannelId.class);
        when(channelId.asShortText()).thenReturn("12345678");

        Channel channel = mock(AbstractVirtualChannel.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        when(channel.id()).thenReturn(channelId);
        when(channel.localAddress()).thenReturn(null);
        when(channel.remoteAddress()).thenReturn(null);
        when(channel.isActive()).thenReturn(false);

        assertEquals("[id: 0x12345678]", channel.toString());
    }

    @Test
    public void testToString2() {
        ChannelId channelId = mock(ChannelId.class);
        when(channelId.asShortText()).thenReturn("12345678");

        Channel channel = mock(AbstractVirtualChannel.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        when(channel.id()).thenReturn(channelId);
        when(channel.localAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 10000));
        when(channel.remoteAddress()).thenReturn(null);
        when(channel.isActive()).thenReturn(false);

        assertEquals("[id: 0x12345678, L:/127.0.0.1:10000]", channel.toString());
    }

    @Test
    public void testToString3() {
        ChannelId channelId = mock(ChannelId.class);
        when(channelId.asShortText()).thenReturn("12345678");

        Channel channel = mock(AbstractVirtualChannel.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        when(channel.id()).thenReturn(channelId);
        when(channel.localAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 10000));
        when(channel.remoteAddress()).thenReturn(new InetSocketAddress("192.168.1.1", 12345));
        when(channel.isActive()).thenReturn(false);

        assertEquals("[id: 0x12345678, L:/127.0.0.1:10000 ! R:/192.168.1.1:12345]", channel.toString());
    }

    @Test
    public void testToString4() {
        ChannelId channelId = mock(ChannelId.class);
        when(channelId.asShortText()).thenReturn("12345678");

        Channel channel = mock(AbstractVirtualChannel.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        when(channel.id()).thenReturn(channelId);
        when(channel.localAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 10000));
        when(channel.remoteAddress()).thenReturn(new InetSocketAddress("192.168.1.1", 12345));
        when(channel.isActive()).thenReturn(true);

        assertEquals("[id: 0x12345678, L:/127.0.0.1:10000 - R:/192.168.1.1:12345]", channel.toString());
    }
}
