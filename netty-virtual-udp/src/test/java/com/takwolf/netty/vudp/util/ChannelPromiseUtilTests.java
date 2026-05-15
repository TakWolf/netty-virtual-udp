package com.takwolf.netty.vudp.util;

import io.netty.channel.ChannelPromise;
import io.netty.channel.VoidChannelPromise;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class ChannelPromiseUtilTests {
    @Test
    public void shouldNotLogWhenTrySuccessOnVoidPromise() {
        ChannelPromise promise = mock(VoidChannelPromise.class);
        ChannelPromiseUtil.safeSetSuccess(promise);
        verify(promise, never()).trySuccess();
    }

    @Test
    public void shouldNotLogWhenTrySuccessReturnTrue() {
        ChannelPromise promise = mock(ChannelPromise.class);
        when(promise.trySuccess()).thenReturn(true);
        ChannelPromiseUtil.safeSetSuccess(promise);
        verify(promise).trySuccess();
    }

    @Test
    public void shouldLogWarningWhenTrySuccessReturnFalse() {
        ChannelPromise promise = mock(ChannelPromise.class);
        when(promise.trySuccess()).thenReturn(false);
        ChannelPromiseUtil.safeSetSuccess(promise);
    }

    @Test
    public void shouldNotLogWhenTryFailureOnVoidPromise() {
        ChannelPromise promise = mock(VoidChannelPromise.class);
        Throwable cause = new RuntimeException("test");
        ChannelPromiseUtil.safeSetFailure(promise, cause);
        verify(promise, never()).tryFailure(cause);
    }

    @Test
    public void shouldNotLogWhenTryFailureReturnTrue() {
        ChannelPromise promise = mock(ChannelPromise.class);
        Throwable cause = new RuntimeException("test");
        when(promise.tryFailure(cause)).thenReturn(true);
        ChannelPromiseUtil.safeSetFailure(promise, cause);
        verify(promise).tryFailure(cause);
    }

    @Test
    public void shouldLogWarningWhenTryFailureReturnFalse() {
        ChannelPromise promise = mock(ChannelPromise.class);
        Throwable cause = new RuntimeException("test");
        when(promise.tryFailure(cause)).thenReturn(false);
        ChannelPromiseUtil.safeSetFailure(promise, cause);
    }
}
