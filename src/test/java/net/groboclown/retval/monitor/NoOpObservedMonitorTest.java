// Released under the MIT License.
package net.groboclown.retval.monitor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoOpObservedMonitorTest {

    @Test
    void isTraceEnabled() {
        assertFalse(NoOpObservedMonitor.getInstance().isTraceEnabled());
    }

    @Test
    void registerInstance() {
        assertSame(
                NoOpObservedMonitor.LISTENER,
                NoOpObservedMonitor.getInstance().registerInstance("x"));
    }

    @Test
    void listenerOnCall() {
        // Ensure nothing happens.
        final ObservedMonitor.Listener listener = NoOpObservedMonitor.LISTENER;
        listener.onObserved();
    }
}
