// Released under the MIT License. 
package net.groboclown.retval.monitor;

import javax.annotation.Nonnull;

/**
 * Tracks values that have requires certain calls made.  Instances are loaded into the
 * {@link ObservedMonitorRegistrar} for use.
 *
 * <p>This exists outside the objects that call here, so that they do not need to
 * allocate information.
 */
public interface ObservedMonitor<T> {
    /**
     * A listener into the closeable object to have it tell the monitor
     * when various object lifecycle events take place.
     */
    interface Listener {
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
    Listener registerInstance(@Nonnull T instance);

    /**
     * Is tracing enabled?  If not enabled, then more memory efficient versions of objects may
     * be used.
     *
     * @return true if close tracing is enabled, false otherwise.
     */
    boolean isTraceEnabled();
}
