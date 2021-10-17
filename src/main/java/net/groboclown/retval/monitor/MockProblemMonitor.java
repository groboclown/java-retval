// Released under the MIT License. 
package net.groboclown.retval.monitor;

import javax.annotation.Nonnull;
import net.groboclown.retval.ProblemContainer;


/**
 * A mock check monitor, suitable for testing.  Note that if used in a non-test environment,
 * this has the potential to quickly consume lots of memory.
 */
public class MockProblemMonitor extends MockObservedMonitor<ProblemContainer> {
    private final Throwable created;

    /**
     * Create a new mock monitor, and register it.
     *
     * @return the new mock instance.
     */
    public static MockProblemMonitor setup() {
        final ObservedMonitor<ProblemContainer> previous =
                ObservedMonitorRegistrar.getCheckedInstance();
        if (previous instanceof MockProblemMonitor) {
            throw new IllegalStateException(
                    "Already have a " + MockProblemMonitor.class.getSimpleName()
                    + " registered", ((MockProblemMonitor) previous).created);
        }
        final MockProblemMonitor ret = new MockProblemMonitor(previous);
        ObservedMonitorRegistrar.setCheckedInstance(ret);
        return ret;
    }

    private MockProblemMonitor(@Nonnull final ObservedMonitor<ProblemContainer> previous) {
        super(previous);
        this.created = new Throwable();
        this.created.fillInStackTrace();
    }

    /**
     * De-register this monitor.
     */
    public void tearDown() {
        if (ObservedMonitorRegistrar.getCheckedInstance() == this) {
            ObservedMonitorRegistrar.setCheckedInstance(getPrevious());
        }
    }
}
