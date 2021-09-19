// Released under the MIT License.
package net.groboclown.retval.monitor;

import net.groboclown.retval.ProblemContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MockCheckMonitorTest {
    private ObservedMonitor<ProblemContainer> original;

    /**
     * Test both setup and tearDown logic, since setup is run with every invocation,
     * the setup is also tested here.
     */
    @Test
    void setup_tearDown() {
        final NoOpObservedMonitor<ProblemContainer> replacement = new NoOpObservedMonitor<>();
        ObservedMonitor.setCheckedInstance(replacement);

        final MockProblemMonitor mock = MockProblemMonitor.setup();
        assertSame(mock, ObservedMonitor.getCheckedInstance());
        try {
            MockProblemMonitor.setup();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
        // Ensure the registered instance didn't change.
        assertSame(mock, ObservedMonitor.getCheckedInstance());

        // Tear down.
        mock.tearDown();
        assertSame(replacement, ObservedMonitor.getCheckedInstance());

        // Should only work if the mock is current.
        mock.tearDown();
        assertSame(replacement, ObservedMonitor.getCheckedInstance());

        ObservedMonitor.setCheckedInstance(this.original);
        mock.tearDown();
        assertSame(this.original, ObservedMonitor.getCheckedInstance());

        // If we run it again and it's active, it will restore itself again.
        ObservedMonitor.setCheckedInstance(mock);
        mock.tearDown();
        assertSame(replacement, ObservedMonitor.getCheckedInstance());
    }

    @Test
    void registerCloseableInstance() {
    }

    @Test
    void registerErrorInstance() {
    }

    @Test
    void isTraceEnabled() {
    }

    @Test
    void findRegistered() {
    }

    @Test
    void testFindRegistered() {
    }

    @Test
    void getNeverClosed() {
    }

    @Test
    void getNeverChecked() {
    }

    @BeforeEach
    void beforeEach() {
        this.original = ObservedMonitor.getCheckedInstance();
        ObservedMonitor.setCheckedInstance(new NoOpObservedMonitor<>());
    }

    @AfterEach
    void afterEach() {
        if (this.original != null) {
            ObservedMonitor.setCheckedInstance(this.original);
        }
    }
}
