// Released under the MIT License. 
package net.groboclown.retval.monitor;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.groboclown.retval.ProblemContainer;

/**
 * A mock check monitor, suitable for testing.  Note that if used in a non-test environment,
 * this has the potential to quickly consume lots of memory.
 */
public class MockCheckMonitor extends CheckMonitor {
    private final CheckMonitor previous;
    private final List<RegisteredCloseable> closeables = new ArrayList<>();
    private final List<RegisteredCheckable> checkables = new ArrayList<>();

    // Allow for easily changing the trace enabled behavior.
    public boolean traceEnabled = false;

    /**
     * Create a new mock monitor, and register it.
     *
     * @return the new mock instance.
     */
    public static MockCheckMonitor setup() {
        final CheckMonitor previous = CheckMonitor.getInstance();
        if (previous instanceof MockCheckMonitor) {
            throw new IllegalStateException(
                    "Already have a " + MockCheckMonitor.class.getSimpleName()
                    + " registered");
        }
        final MockCheckMonitor ret = new MockCheckMonitor(previous);
        CheckMonitor.setInstance(ret);
        return ret;
    }

    private MockCheckMonitor(final CheckMonitor previous) {
        this.previous = previous;
    }

    /**
     * De-register this monitor.
     */
    public void tearDown() {
        if (CheckMonitor.getInstance() == this) {
            CheckMonitor.setInstance(this.previous);
        }
    }

    @Nonnull
    @Override
    public CloseableListener registerCloseableInstance(@Nonnull final AutoCloseable instance) {
        final RegisteredCloseable ret = new RegisteredCloseable(instance);
        synchronized (this.closeables) {
            this.closeables.add(ret);
        }
        return ret;
    }

    @Nonnull
    @Override
    public CheckableListener registerErrorInstance(@Nonnull final ProblemContainer instance) {
        final RegisteredCheckable ret = new RegisteredCheckable(instance);
        synchronized (this.checkables) {
            this.checkables.add(ret);
        }
        return ret;
    }

    @Override
    public boolean isTraceEnabled() {
        return this.traceEnabled;
    }


    /**
     * Finds the registered closeable instance for the object.  If not returned, then it was never
     * registered.
     *
     * @param closeable source object
     * @return registered closeable object, or null if never registered.
     */
    @Nullable
    public RegisteredCloseable findRegistered(@Nonnull final AutoCloseable closeable) {
        synchronized (this.closeables) {
            for (final RegisteredCloseable reg : this.closeables) {
                // Note "==" - this is intentional
                if (reg.getCloseable() == closeable) {
                    return reg;
                }
            }
        }
        return null;
    }


    /**
     * Finds the registered checkable instance for the object.  If not returned, then it was never
     * registered.
     *
     * @param closeable source object
     * @return registered checkable object, or null if never registered.
     */
    @Nullable
    public RegisteredCheckable findRegistered(@Nonnull final ProblemContainer closeable) {
        synchronized (this.checkables) {
            for (final RegisteredCheckable reg : this.checkables) {
                // Note "==" - this is intentional
                if (reg.getCheckable() == closeable) {
                    return reg;
                }
            }
        }
        return null;
    }


    /**
     * Return all registered closeable objects that never had the close called on the listener.
     *
     * @return all never closed, registered objects.
     */
    @Nonnull
    public List<AutoCloseable> getNeverClosed() {
        final List<AutoCloseable> ret = new ArrayList<>();
        synchronized (this.closeables) {
            for (final RegisteredCloseable reg : this.closeables) {
                if (! reg.wasClosed()) {
                    ret.add(reg.getCloseable());
                }
            }
        }
        return ret;
    }


    /**
     * Return all registered closeable objects that never had the close called on the listener.
     *
     * @return all never closed, registered objects.
     */
    @Nonnull
    public List<ProblemContainer> getNeverChecked() {
        final List<ProblemContainer> ret = new ArrayList<>();
        synchronized (this.checkables) {
            for (final RegisteredCheckable reg : this.checkables) {
                if (! reg.wasChecked()) {
                    ret.add(reg.getCheckable());
                }
            }
        }
        return ret;
    }


    /**
     * Tracks the number of times the closeable is called.
     */
    public static class RegisteredCloseable implements CloseableListener {
        private final AutoCloseable closeable;
        private int callCount = 0;

        RegisteredCloseable(final AutoCloseable closeable) {
            this.closeable = closeable;
        }

        /**
         * Get the number of times the {@link #onClosed()} method was called.
         *
         * @return closed invocation count
         */
        public int getCallCount() {
            return this.callCount;
        }

        /**
         * Return if this closeable object had the {@link #onClosed()} ()} method called.
         *
         * @return true if the on close method was called, otherwise false.
         */
        public boolean wasClosed() {
            return this.callCount > 0;
        }

        @Override
        public void onClosed() {
            this.callCount++;
        }

        /**
         * Get the closeable instance.
         *
         * @return closeable instance
         */
        @Nonnull
        public AutoCloseable getCloseable() {
            return this.closeable;
        }
    }

    /**
     * Checkable listener class that's suitable for testing.
     */
    public static class RegisteredCheckable implements CheckableListener {
        private final ProblemContainer checkable;
        private int callCount;

        RegisteredCheckable(@Nonnull final ProblemContainer checkable) {
            this.checkable = checkable;
        }

        /**
         * Get the number of times the {@link #onChecked()} method was called.
         *
         * @return the checked invocation count.
         */
        public int getCallCount() {
            return this.callCount;
        }

        /**
         * Return if this checkable object has the {@link #onChecked()} method called.
         *
         * @return true if the on check method was called, otherwise false.
         */
        public boolean wasChecked() {
            return this.callCount > 0;
        }

        @Override
        public void onChecked() {
            this.callCount++;
        }

        /**
         * Get the source checkable object.
         *
         * @return the corresponding checkable value.
         */
        @Nonnull
        public ProblemContainer getCheckable() {
            return this.checkable;
        }
    }
}
