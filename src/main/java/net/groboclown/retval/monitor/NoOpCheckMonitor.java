// Released under the MIT License. 
package net.groboclown.retval.monitor;

import javax.annotation.Nonnull;
import net.groboclown.retval.ProblemContainer;

/**
 * A monitor that performs no actual monitoring.
 */
public class NoOpCheckMonitor extends CheckMonitor {
    public static final NoOpCloseableListener CLOSEABLE_LISTENER = new NoOpCloseableListener();
    public static final NoOpCheckableListener CHECKABLE_LISTENER = new NoOpCheckableListener();

    @Nonnull
    @Override
    public CloseableListener registerCloseableInstance(@Nonnull final AutoCloseable instance) {
        return CLOSEABLE_LISTENER;
    }

    @Nonnull
    @Override
    public CheckableListener registerErrorInstance(@Nonnull final ProblemContainer instance) {
        return CHECKABLE_LISTENER;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }


    private static class NoOpCloseableListener implements CloseableListener {
        @Override
        public void onClosed() {
            // No op
        }
    }

    private static class NoOpCheckableListener implements CheckableListener {
        @Override
        public void onChecked() {
            // No op
        }
    }
}
