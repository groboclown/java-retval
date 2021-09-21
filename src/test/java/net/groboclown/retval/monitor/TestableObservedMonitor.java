// Released under the MIT License. 
package net.groboclown.retval.monitor;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

class TestableObservedMonitor<T> extends ObservedMonitor<T> {
    private final List<TestableListener<?>> listeners = new ArrayList<>();

    @Nonnull
    @Override
    public Listener registerInstance(@Nonnull final T instance) {
        final TestableListener<T> ret = new TestableListener<>(instance);
        this.listeners.add(ret);
        return ret;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Nonnull
    public List<TestableListener<?>> getListeners() {
        return this.listeners;
    }


    public static class TestableListener<T> implements Listener {
        private final T value;
        private int callCount = 0;

        private TestableListener(@Nonnull final T value) {
            this.value = value;
        }

        @Nonnull
        public T getValue() {
            return this.value;
        }

        @Nonnegative
        public int getCallCount() {
            return this.callCount;
        }

        @Override
        public void onObserved() {
            this.callCount++;
        }
    }
}
