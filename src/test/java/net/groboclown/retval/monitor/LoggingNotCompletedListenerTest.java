// Released under the MIT License.
package net.groboclown.retval.monitor;

import org.junit.jupiter.api.Test;

class LoggingNotCompletedListenerTest {
    @Test
    void instanceNotCompleted() {
        // This calls into the java.util.logging, which is neigh impossible to
        // make testable.  So we'll do the best that we can.
        // By default, logging is sent to system.err, but we can't guarantee that
        // the test environment is setup that way.
        LoggingNotCompletedListener.INSTANCE.instanceNotCompleted(
                "m-o",
                "instance-x",
                new StackTraceElement[] {
                    new StackTraceElement("loader", "mx", "fs", 1)
                }
        );
    }
}
