// Released under the MIT License. 
package net.groboclown.retval.monitor;

import javax.annotation.Nonnull;
import net.groboclown.retval.ProblemContainer;


/**
 * A mock check monitor, suitable for testing.  Note that if used in a non-test environment,
 * this has the potential to quickly consume lots of memory.
 */
public class MockProblemMonitor extends MockObservedMonitor<ProblemContainer> {

    /**
     * Create a new mock monitor, and register it.
     *
     * @return the new mock instance.
     */
    public static MockProblemMonitor setup() {
        final ObservedMonitor<ProblemContainer> previous = ObservedMonitor.getCheckedInstance();
        if (previous instanceof MockProblemMonitor) {
            throw new IllegalStateException(
                    "Already have a " + MockProblemMonitor.class.getSimpleName()
                    + " registered");
        }
        final MockProblemMonitor ret = new MockProblemMonitor(previous);
        ObservedMonitor.setCheckedInstance(ret);
        return ret;
    }

    private MockProblemMonitor(@Nonnull final ObservedMonitor<ProblemContainer> previous) {
        super(previous);
    }

    /**
     * De-register this monitor.
     */
    public void tearDown() {
        if (ObservedMonitor.getCheckedInstance() == this) {
            ObservedMonitor.setCheckedInstance(getPrevious());
        }
    }
}
