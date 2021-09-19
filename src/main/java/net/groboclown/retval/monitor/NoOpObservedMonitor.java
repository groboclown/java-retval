// Released under the MIT License. 
package net.groboclown.retval.monitor;

import javax.annotation.Nonnull;

/**
 * A monitor that performs no actual monitoring.
 */
public class NoOpObservedMonitor<T> extends ObservedMonitor<T> {
    public static final NoOpListener LISTENER = new NoOpListener();

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
