// Released under the MIT License.
package net.groboclown.retval.monitor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class StackUtilTest {
    @Test
    void mungedStack_notFound() {
        try {
            StackUtil.mungedStack("Not a class name", "Not a method name");
            fail("Did not throw IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            assertTrue(
                    e.getMessage().contains(" Not a class name.Not a method name()"),
                    e.getMessage()
            );
        }
    }

    @Test
    void mungedStack_notFoundMethod() {
        try {
            StackUtil.mungedStack(getClass().getName(), "Not a method name");
            fail("Did not throw IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
            assertTrue(
                    e.getMessage().contains(" " + getClass().getName() + ".Not a method name()"),
                    e.getMessage()
            );
        }
    }

    @Test
    void mungedStack_directCaller() {
        // Use this method as the caller stack position.
        // The stacks should have the same depth.
        final StackTraceElement[] currentStack = new Exception().fillInStackTrace().getStackTrace();
        assertTrue(currentStack.length > 0, "Current call stack depth is 0");

        final StackTraceElement[] ret = StackUtil.mungedStack(
                getClass().getName(), "mungedStack_directCaller");

        // The returned stack should be everything up to this call, but not including this call.
        // So the returned stack count should be 1 less than the current call stack count.
        assertEquals(currentStack.length, ret.length + 1);
    }
}
