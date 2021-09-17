// Released under the MIT License.
package net.groboclown.retval.monitor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MockCheckMonitorTest {
    private CheckMonitor original;

    /**
     * Test both setup and tearDown logic, since setup is run with every invocation,
     * the setup is also tested here.
     */
    @Test
    void setup_tearDown() {
        final NoOpCheckMonitor replacement = new NoOpCheckMonitor();
        CheckMonitor.setInstance(replacement);

        final MockCheckMonitor mock = MockCheckMonitor.setup();
        assertSame(mock, CheckMonitor.getInstance());
        try {
            MockCheckMonitor.setup();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
        // Ensure the registered instance didn't change.
        assertSame(mock, CheckMonitor.getInstance());

        // Tear down.
        mock.tearDown();
        assertSame(replacement, CheckMonitor.getInstance());

        // Should only work if the mock is current.
        mock.tearDown();
        assertSame(replacement, CheckMonitor.getInstance());

        CheckMonitor.setInstance(this.original);
        mock.tearDown();
        assertSame(this.original, CheckMonitor.getInstance());

        // If we run it again and it's active, it will restore itself again.
        CheckMonitor.setInstance(mock);
        mock.tearDown();
        assertSame(replacement, CheckMonitor.getInstance());
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
        this.original = CheckMonitor.getInstance();
        CheckMonitor.setInstance(new NoOpCheckMonitor());
    }

    @AfterEach
    void afterEach() {
        if (this.original != null) {
            CheckMonitor.setInstance(this.original);
        }
    }
}