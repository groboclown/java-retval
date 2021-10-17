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
 * Unmonitored version of RetVoid with an OK state.
 */
public class SimpleRetVoidOk implements RetVoid {
    public static final SimpleRetVoidOk OK = new SimpleRetVoidOk();

    private SimpleRetVoidOk() {
        // It must be shared.
    }

    @Override
    public boolean isProblem() {
        return false;
    }

    @Override
    public boolean hasProblems() {
        return false;
    }

    @Override
    public boolean isOk() {
        return true;
    }

    @Nonnull
    @Override
    public Collection<Problem> anyProblems() {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public Collection<Problem> validProblems() {
        throw new IllegalStateException("contains no problems");
    }

    @Nonnull
    @Override
    public String debugProblems(@Nonnull final String joinedWith) {
        return "";
    }

    @Override
    public void joinProblemsWith(@Nonnull final Collection<Problem> problemList) {
        // No-op
    }

    @Nonnull
    @Override
    public <R> RetVal<R> then(@Nonnull final NonnullSupplier<RetVal<R>> supplier) {
        return supplier.get();
    }

    @Nonnull
    @Override
    public <R> RetVal<R> map(@Nonnull final NonnullSupplier<R> supplier) {
        return RetGenerator.valOk(supplier.get());
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> thenNullable(
            @Nonnull final NonnullSupplier<RetNullable<R>> supplier
    ) {
        return supplier.get();
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> mapNullable(@Nonnull final Supplier<R> supplier) {
        return RetGenerator.nullableOk(supplier.get());
    }

    @Nonnull
    @Override
    public RetVoid thenVoid(@Nonnull final NonnullSupplier<RetVoid> supplier) {
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
        return "Ret(ok)";
    }
}
