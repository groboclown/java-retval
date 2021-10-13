// Released under the MIT License. 
package net.groboclown.retval.monitor;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A mock check monitor, suitable for testing.  Note that if used in a non-test environment,
 * this has the potential to quickly consume lots of memory.
 */
public abstract class MockObservedMonitor<T> extends ObservedMonitor<T> {
    private final ObservedMonitor<T> previous;
    private final List<Registered<T>> observables = new ArrayList<>();

    // Allow for easily changing the trace enabled behavior.
    public boolean traceEnabled = false;

    protected MockObservedMonitor(final ObservedMonitor<T> previous) {
        this.previous = previous;
    }

    /**
     * Get the previously installed monitor, which this monitor replaced.  Allows for
     * easy restore of the previous state when the system finishes using this mock monitor.
     *
     * @return the previously installed monitor
     */
    @Nonnull
    protected ObservedMonitor<T> getPrevious() {
        return this.previous;
    }

    /**
     * Tear down this monitor to the pre-setup state.
     */
    public abstract void tearDown();

    @Nonnull
    @Override
    public Listener registerInstance(@Nonnull final T instance) {
        final Registered<T> ret = new Registered<>(instance);
        synchronized (this.observables) {
            this.observables.add(ret);
        }
        return ret;
    }

    @Override
    public boolean isTraceEnabled() {
        return this.traceEnabled;
    }


    /**
     * Finds the registered instance for the observed object.  If not returned, then it was never
     * registered.  This uses `==` identity checking rather than {@link #equals(Object)}.
     *
     * <p>This method allows for a deep inspection into how the RetVal classes operate.
     *
     * @param value source object
     * @return registered closeable object, or null if never registered.
     */
    @Nullable
    public Registered<T> findRegistered(@Nonnull final T value) {
        synchronized (this.observables) {
            for (final Registered<T> reg : this.observables) {
                // Note "==" - this is intentional
                if (reg.getObservable() == value) {
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
    public List<T> getNeverObserved() {
        final List<T> ret = new ArrayList<>();
        synchronized (this.observables) {
            for (final Registered<T> reg : this.observables) {
                if (! reg.wasObserved()) {
                    ret.add(reg.getObservable());
                }
            }
        }
        return ret;
    }


    /**
     * Tracks the number of times the closeable is called.
     */
    public static class Registered<T> implements Listener {
        private final T observable;
        private int callCount = 0;

        Registered(final T observable) {
            this.observable = observable;
        }

        /**
         * Get the number of times the {@link #onObserved()} method was called.
         *
         * @return closed invocation count
         */
        public int getCallCount() {
            return this.callCount;
        }

        /**
         * Return if this closeable object had the {@link #onObserved()} ()} method called.
         *
         * @return true if the on close method was called, otherwise false.
         */
        public boolean wasObserved() {
            return this.callCount > 0;
        }

        @Override
        public void onObserved() {
            this.callCount++;
        }

        /**
         * Get the closeable instance.
         *
         * @return closeable instance
         */
        @Nonnull
        public T getObservable() {
            return this.observable;
        }
    }
}
