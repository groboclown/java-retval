// Released under the MIT License. 
package net.groboclown.retval.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import net.groboclown.retval.Problem;
import net.groboclown.retval.RetNullable;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.RetVoid;
import net.groboclown.retval.function.NonnullSupplier;
import net.groboclown.retval.monitor.NoOpObservedMonitor;
import net.groboclown.retval.monitor.ObservedMonitor;


/**
 * Monitored version of RetVal with an ok state (no problems).
 */
public class MonitoredRetVoidOk implements RetVoid {
    /**
     * Constant value for a RetVoid with no problems.  This is private to force
     * use of the "ok" function call, which may be replaced to return a new object
     * each time.  While the constant OK is fine for production, it means check tracing
     * can't be performed.
     */
    static MonitoredRetVoidOk OK = new MonitoredRetVoidOk(NoOpObservedMonitor.LISTENER);

    private final ObservedMonitor.Listener listener;

    MonitoredRetVoidOk() {
        this.listener = ObservedMonitor.getCheckedInstance().registerInstance(this);
    }

    private MonitoredRetVoidOk(@Nonnull final ObservedMonitor.Listener listener) {
        this.listener = listener;
    }

    @Override
    public boolean isProblem() {
        return hasProblems();
    }

    @Override
    public boolean hasProblems() {
        this.listener.onObserved();
        return false;
    }

    @Override
    public boolean isOk() {
        this.listener.onObserved();
        return true;
    }

    @Nonnull
    @Override
    public Collection<Problem> anyProblems() {
        this.listener.onObserved();
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public Collection<Problem> validProblems() {
        // This counts as an observation.
        this.listener.onObserved();
        throw new IllegalStateException("contains no problems");
    }

    @Nonnull
    @Override
    public String debugProblems(@Nonnull final String joinedWith) {
        return "";
    }

    @Override
    public void joinProblemsWith(@Nonnull final Collection<Problem> problemList) {
        this.listener.onObserved();
    }

    @Nonnull
    @Override
    public <R> RetVal<R> then(@Nonnull final NonnullSupplier<RetVal<R>> supplier) {
        this.listener.onObserved();
        return supplier.get();
    }

    @Nonnull
    @Override
    public <R> RetVal<R> map(@Nonnull final NonnullSupplier<R> supplier) {
        this.listener.onObserved();
        return RetGenerator.valOk(supplier.get());
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> thenNullable(
            @Nonnull final NonnullSupplier<RetNullable<R>> supplier
    ) {
        this.listener.onObserved();
        return supplier.get();
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> mapNullable(@Nonnull final Supplier<R> supplier) {
        this.listener.onObserved();
        return RetGenerator.nullableOk(supplier.get());
    }

    @Nonnull
    @Override
    public RetVoid thenVoid(@Nonnull final NonnullSupplier<RetVoid> supplier) {
        this.listener.onObserved();
        return supplier.get();
    }

    @Nonnull
    @Override
    public RetVoid thenRun(@Nonnull final Runnable runnable) {
        runnable.run();
        return this;
    }

    @Nonnull
    @Override
    public <V> RetVal<V> forwardProblems() {
        throw new IllegalStateException("contains no problems");
    }

    @Nonnull
    @Override
    public <V> RetNullable<V> forwardNullableProblems() {
        throw new IllegalStateException("contains no problems");
    }

    @Nonnull
    @Override
    public RetVoid forwardVoidProblems() {
        throw new IllegalStateException("contains no problems");
    }

    @Override
    public String toString() {
        return "Ret()";
    }
}
