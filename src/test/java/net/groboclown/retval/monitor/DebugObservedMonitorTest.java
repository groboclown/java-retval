// Released under the MIT License.
package net.groboclown.retval.monitor;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DebugObservedMonitorTest {

    @Test
    void registerInstance_notClosed() {
        // Needs to test things going out of scope.
        final TestableNotCompletedListener notCompletedListener =
                new TestableNotCompletedListener();
        final DebugObservedMonitor<Object> monitor = new DebugObservedMonitor<>(
                "o-m", notCompletedListener
        );
        Object value = new Object();
        final String valueName = value.toString();
        final ObservedMonitor.Listener listener = monitor.registerInstance(value);

        // Force the value to go out of scope.  This must be a force such that the phantom
        // reference is triggered.
        value = null;
        forceGc();
        assertEquals(
                1, notCompletedListener.events.size(),
                "Expected not-completed count is not 1; if this is 0, then it is "
                + "possibly from running gc() when it is not guaranteed to do what we want; try "
                + "again, and if it still fails, then the test may be really broken."
        );
        final NotCompleteEvent event = notCompletedListener.events.get(0);
        assertEquals("o-m", event.monitorName);
        assertEquals(valueName, event.instanceName);
        assertTrue(event.creationPoint.length > 0);
        assertEquals(getClass().getName(), event.creationPoint[0].getClassName());
        assertEquals("registerInstance_notClosed", event.creationPoint[0].getMethodName());

        // Ensure this doesn't cause problems.
        listener.onObserved();
    }

    @Test
    void registerInstance_closed() {
        // Needs to test things going out of scope.
        final TestableNotCompletedListener notCompletedListener =
                new TestableNotCompletedListener();
        final DebugObservedMonitor<Object> monitor = new DebugObservedMonitor<>(
                "o-m", notCompletedListener
        );
        Object value = new Object();
        final ObservedMonitor.Listener listener = monitor.registerInstance(value);
        listener.onObserved();

        // Force the value to go out of scope.  This must be a force such that the phantom
        // reference is triggered.
        value = null;
        forceGc();
        assertEquals(0, notCompletedListener.events.size());
    }

    @Test
    void isTraceEnabled() {
        assertTrue(new DebugObservedMonitor<>("", LoggingNotCompletedListener.INSTANCE)
                .isTraceEnabled());
    }

    @BeforeEach
    void beforeEach() {
    }

    @AfterEach
    void afterEach() {
    }


    static class TestableNotCompletedListener implements NotCompletedListener {
        final List<NotCompleteEvent> events = new ArrayList<>();

        @Override
        public synchronized void instanceNotCompleted(
                @Nonnull final String monitorName,
                @Nonnull final String instanceName,
                @Nonnull final StackTraceElement[] creationPoint
        ) {
            this.events.add(new NotCompleteEvent(monitorName, instanceName, creationPoint));
        }
    }

    static class NotCompleteEvent {
        final String monitorName;
        final String instanceName;
        final StackTraceElement[] creationPoint;

        NotCompleteEvent(
                final String monitorName,
                final String instanceName,
                final StackTraceElement[] creationPoint
        ) {
            this.monitorName = monitorName;
            this.instanceName = instanceName;
            this.creationPoint = creationPoint;
        }
    }

    static void forceGc() {
        // This is really flaky.  It isn't 100% guaranteed to get the cleaner to trigger.
        // So call it twice with a small delay.  Even this isn't guaranteed to work.
        innerForceGc();
        try {
            Thread.sleep(10);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
        innerForceGc();
    }

    static void innerForceGc() {
        // Keep pinging the garbage collector until the phantom reference
        // is cleared out.
        final ReferenceQueue<Object> queue = new ReferenceQueue<>();
        Object obj = new Object();
        final PhantomReference<Object> ref = new PhantomReference<>(obj, queue);

        // Force the object to go out of scope, thus ready for cleanup.
        obj = null;

        while (true) {
            System.gc();
            System.runFinalization();
            try {
                queue.remove(10);
                return;
            } catch (final InterruptedException e) {
                // ignore
            }
        }
    }
}
