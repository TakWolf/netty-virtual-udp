package com.takwolf.netty.vudp.router;

import com.takwolf.netty.vudp.channel.ChildVirtualChannel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class VirtualChannelRouterTests {
    @Test
    public void test() throws Exception {
        abstract class MyRouter implements VirtualChannelRouter<Integer, Void, Void> {}
        MyRouter router = mock(MyRouter.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        ChildVirtualChannel channel = mock(ChildVirtualChannel.class);
        assertTrue(router.newContext(1).ok());
        assertNull(router.existingContext(1, channel));
        router.attachContext(1, null, channel);
    }
}
