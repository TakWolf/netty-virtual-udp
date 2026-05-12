package com.takwolf.netty.vudp.router;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RouteResultTests {
    @Test
    public void test() {
        {
            RouteResult<Integer> result = RouteResult.of(1);
            assertTrue(result.ok());
            assertEquals(1, result.get());
        }
        {
            RouteResult<Integer> result = RouteResult.of();
            assertTrue(result.ok());
            assertNull(result.get());
        }
        {
            RouteResult<Integer> result = RouteResult.none();
            assertFalse(result.ok());
            assertNull(result.get());
        }
    }
}
