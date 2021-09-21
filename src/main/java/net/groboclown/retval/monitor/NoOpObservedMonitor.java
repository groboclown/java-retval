// Released under the MIT License. 
package net.groboclown.retval.monitor;

import javax.annotation.Nonnull;

/**
 * A monitor that performs no actual monitoring.
 */
public class NoOpObservedMonitor<T> extends ObservedMonitor<T> {
    public static final NoOpListener LISTENER = new NoOpListener();
    private static final NoOpObservedMonitor<Object> INSTANCE = new NoOpObservedMonitor<>();

    private NoOpObservedMonitor() {
        // prevent direct instantiation.
    }

    /**
     * Type-cast the placeholder No Op implementation to the needed format.
     *
     * @param <T> required type value
     * @return the static instance
     */
    @Nonnull
    public static <T> NoOpObservedMonitor<T> getInstance() {
        @SuppressWarnings("unchecked")
        final NoOpObservedMonitor<T> ret = (NoOpObservedMonitor<T>) INSTANCE;
        return ret;
    }

    @Nonnull
    @Override
    public Listener registerInstance(@Nonnull final T instance) {
        return LISTENER;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }


    private static class NoOpListener implements Listener {
        @Override
        public void onObserved() {
            // No op
        }
    }
}
