// Released under the MIT License. 
package net.groboclown.retval.monitor;

import java.util.Objects;
import javax.annotation.Nonnull;
import net.groboclown.retval.ProblemContainer;

/**
 * A singleton that tracks values that have requires certain calls made.
 *
 * <p>This exists outside the objects that call here, so that they do not need to
 * allocate information
 */
public abstract class ObservedMonitor<T> {
    private static ObservedMonitor<ProblemContainer> CHECKED_INSTANCE;

    static {
        CHECKED_INSTANCE = discoverCheckedInstance();
    }

    @Nonnull
    public static ObservedMonitor<ProblemContainer> getCheckedInstance() {
        return CHECKED_INSTANCE;
    }

    /**
     * A listener into the closeable object to have it tell the monitor
     * when various object lifecycle events take place.
     */
    public interface Listener {
        /**
         * Called when the observable object has its specific action called.
         */
        void onObserved();
    }

    /**
     * Register a new observable object with the monitor.
     * The returned value must be immutable in all but unit test environments.
     *
     * @param instance instance to register
     * @return a callback listener for when the close action occurs.
     */
    @Nonnull
    public abstract Listener registerInstance(@Nonnull T instance);

    /**
     * Is tracing enabled?  If not enabled, then more memory efficient versions of objects may
     * be used.
     *
     * @return true if close tracing is enabled, false otherwise.
     */
    public abstract boolean isTraceEnabled();

    // package-protected for testing purposes.
    @Nonnull
    static ObservedMonitor<ProblemContainer> discoverCheckedInstance() {
        // This is a placeholder for an eventual, possible more robust
        // dynamic implementation.
        if (SystemEnvUtil.isValueEqual("RETVAL_MONITOR_DEBUG", "true")) {
            return new DebugObservedMonitor<>(
                    "problem state", LoggingNotCompletedListener.INSTANCE);
        }
        return NoOpObservedMonitor.getInstance();
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
        Objects.requireNonNull(monitor, "monitor");
        CHECKED_INSTANCE = monitor;
    }
}
