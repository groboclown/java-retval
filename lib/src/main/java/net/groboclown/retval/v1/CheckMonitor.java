// Released under the MIT License. 
package net.groboclown.retval.v1;


import net.groboclown.retval.v1.impl.DebugCheckMonitor;
import net.groboclown.retval.v1.impl.NoOpCheckMonitor;

import javax.annotation.Nonnull;
import java.util.Objects;


/**
 * A singleton that monitors the state of closable and return values, to ensure that their
 * values are checked.
 *
 * This exists outside the objects that call here, so that they do not need to allocate information
 */
public abstract class CheckMonitor {
    private static CheckMonitor INSTANCE;

    static {
        INSTANCE = discoverInstance();
    }

    @Nonnull
    public static CheckMonitor getInstance() {
        return INSTANCE;
    }

    /**
     * A listener into the closeable object to have it tell the monitor
     * when various object lifecycle events take place.
     */
    public interface CloseableListener {
        /**
         * Called when the closeable object has its {@link AutoCloseable#close()} method called.
         * This should be called first, before the close call has a chance to throw exceptions.
         */
        void onClosed();
    }

    /**
     * A listener into the closeable object to have it tell the monitor
     * when various object lifecycle events take place.
     */
    public interface CheckableListener {
        /**
         * Called when the checkable object has considered itself as checked.
         */
        void onChecked();
    }

    /**
     * Register a new closable object with the monitor.
     *
     * @return a callback listener for when the close action occurs.
     */
    @Nonnull
    public abstract CloseableListener registerCloseableInstance(@Nonnull AutoCloseable instance);

    /**
     * Register a new object that can potentially contain errors with the monitor.
     * @return a unique ID for the instance.
     */
    @Nonnull
    public abstract CheckableListener registerErrorInstance(@Nonnull ProblemContainer instance);

    /**
     * Is tracing enabled?  If not enabled, then more memory efficient versions of objects may be used.
     *
     * @return true if close tracing is enabled, false otherwise.
     */
    public abstract boolean isTraceEnabled();

    @Nonnull
    private static CheckMonitor discoverInstance() {
        // This is a placeholder for an eventual, possible more robust
        // dynamic implementation.
        final String debugEnv = System.getenv("RETVAL_MONITOR_DEBUG");
        if ("true".equalsIgnoreCase(debugEnv)) {
            return new DebugCheckMonitor();
        }
        return new NoOpCheckMonitor();
    }



    /**
     * Allow for runtime replacement of the singleton.  This is useful for testing or dynamically enabling
     * tracing.
     */
    public static void setInstance(@Nonnull final CheckMonitor monitor) {
        Objects.requireNonNull(monitor, "monitor");
        INSTANCE = monitor;
    }
}
