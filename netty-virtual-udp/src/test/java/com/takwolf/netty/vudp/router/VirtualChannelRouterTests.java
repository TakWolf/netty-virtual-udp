package com.takwolf.netty.vudp.router;

import com.takwolf.netty.vudp.channel.ChildVirtualChannel;
import io.netty.channel.DefaultChannelId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VirtualChannelRouterTests {
    @Test
    public void test() throws Exception {
        abstract class MyRouter implements VirtualChannelRouter<Integer, Void, Void> {}
        MyRouter router = mock(MyRouter.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        ChildVirtualChannel channel = mock(ChildVirtualChannel.class);
        assertInstanceOf(DefaultChannelId.class, router.channelId(1));
        assertTrue(router.newContext(1).ok());
        assertNull(router.existingContext(1, channel));
        router.attachContext(1, null, channel);
    }
}
