// Released under the MIT License. 
package net.groboclown.retval.monitor;

import java.util.Objects;
import javax.annotation.Nonnull;
import net.groboclown.retval.ProblemContainer;
import net.groboclown.retval.env.ObservedMonitorDetection;

/**
 * A singleton that tracks values that have requirements around execution paths.
 *
 * <p>This exists outside the objects that call here, so that they do not need to
 * allocate information.
 */
public class ObservedMonitorRegistrar {
    private static ObservedMonitor<ProblemContainer> CHECKED_INSTANCE;

    static {
        CHECKED_INSTANCE = Objects.requireNonNull(
                ObservedMonitorDetection.discoverCheckedInstance(),
                "Discovered checked instance");
    }

    private ObservedMonitorRegistrar() {
        // Utility class
    }

    @Nonnull
    public static ObservedMonitor<ProblemContainer> getCheckedInstance() {
        return Objects.requireNonNullElse(
                CHECKED_INSTANCE, NoOpObservedMonitor.getInstance());
    }

    /**
     * Register a new observable object with the monitor.
     * The returned value must be immutable in all but unit test environments.
     *
     * @param instance instance to register
     * @return a callback listener for when the close action occurs.
     */
    @Nonnull
    public static ObservedMonitor.Listener registerCheckedInstance(
            @Nonnull final ProblemContainer instance) {
        return CHECKED_INSTANCE.registerInstance(instance);
    }

    /**
     * Is tracing enabled?  If not enabled, then more memory efficient versions of objects may
     * be used.
     *
     * @return true if close tracing is enabled, false otherwise.
     */
    public static boolean isCheckedTraceEnabled() {
        return CHECKED_INSTANCE.isTraceEnabled();
    }

    /**
     * Allow for runtime replacement of the singleton.  This is useful for testing or
     * dynamically enabling tracing.
     *
     * @param monitor new monitor to use as the singleton.
     */
    public static void setCheckedInstance(
            @Nonnull final ObservedMonitor<ProblemContainer> monitor
    ) {
        CHECKED_INSTANCE = Objects.requireNonNull(monitor, "monitor");
    }
}
