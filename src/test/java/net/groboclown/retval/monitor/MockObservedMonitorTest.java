// Released under the MIT License.
package net.groboclown.retval.monitor;

import java.util.List;
import net.groboclown.retval.ProblemContainer;
import net.groboclown.retval.TestableProblemContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class MockObservedMonitorTest {
    private ObservedMonitor<ProblemContainer> original;

    /**
     * Test both setup and tearDown logic, since setup is run with every invocation,
     * the setup is also tested here.
     */
    @Test
    void setup_tearDown() {
        final TestableObservedMonitor<ProblemContainer> replacement =
                new TestableObservedMonitor<>();
        ObservedMonitor.setCheckedInstance(replacement);

        final MockProblemMonitor mock = MockProblemMonitor.setup();
        assertSame(mock, ObservedMonitor.getCheckedInstance());
        assertSame(replacement, mock.getPrevious());
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
    void fullUseCase() {
        final MockProblemMonitor mock = MockProblemMonitor.setup();

        // Note that TestableProblemContainer uses proper equals(), so this exercises
        // that expected behavior.
        final TestableProblemContainer val1 = new TestableProblemContainer();
        final TestableProblemContainer val2 = new TestableProblemContainer();

        final MockObservedMonitor.Registered<?> listener1 =
                (MockObservedMonitor.Registered<?>) mock.registerInstance(val1);
        assertSame(val1, listener1.getObservable());
        assertEquals(0, listener1.getCallCount());

        final MockObservedMonitor.Registered<?> listener2 =
                (MockObservedMonitor.Registered<?>) mock.registerInstance(val2);
        assertSame(val2, listener2.getObservable());
        assertEquals(0, listener1.getCallCount());

        assertSame(
                listener1,
                mock.findRegistered(val1)
        );
        assertSame(
                listener2,
                mock.findRegistered(val2)
        );
        assertNull(mock.findRegistered(new TestableProblemContainer()));

        // Order is unimportant.
        final List<ProblemContainer> neverObserved = mock.getNeverObserved();
        assertEquals(2, neverObserved.size());
        assertTrue(neverObserved.get(0) == val1 || neverObserved.get(1) == val1);
        assertTrue(neverObserved.get(0) == val2 || neverObserved.get(1) == val2);

        listener2.onObserved();
        assertEquals(1, listener2.getCallCount());
        assertEquals(List.of(val1), mock.getNeverObserved());

        listener2.onObserved();
        assertEquals(2, listener2.getCallCount());
        assertEquals(List.of(val1), mock.getNeverObserved());

        listener1.onObserved();
        assertEquals(1, listener1.getCallCount());
        assertEquals(List.of(), mock.getNeverObserved());

        listener1.onObserved();
        assertEquals(2, listener1.getCallCount());
        assertEquals(List.of(), mock.getNeverObserved());
    }


    @BeforeEach
    void beforeEach() {
        this.original = ObservedMonitor.getCheckedInstance();
        ObservedMonitor.setCheckedInstance(new TestableObservedMonitor<>());
    }

    @AfterEach
    void afterEach() {
        if (this.original != null) {
            ObservedMonitor.setCheckedInstance(this.original);
        }
    }


}
