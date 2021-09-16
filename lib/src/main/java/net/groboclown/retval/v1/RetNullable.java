// Released under the MIT License.
package net.groboclown.retval.v1;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class RetNullable<T> implements ProblemContainer, ContinuationNullable<T> {
    private final List<Problem> problems;
    private final CheckMonitor.CheckableListener listener;
    private final T value;

    @Nonnull
    public static <T> RetNullable<T> ok(@Nullable final T value) {
        throw new IllegalStateException();
    }

    @Nonnull
    public static <T> RetNullable<T> error(@Nonnull final Problem... problems) {
        throw new IllegalStateException();
    }

    @SafeVarargs
    @Nonnull
    public static <T> RetNullable<T> error(@Nonnull final Iterable<Problem>... problems) {
        throw new IllegalStateException();
    }

    // package-protected to allow for memory efficient problem passing.
    // Must be non-null, non-empty, and immutable.
    RetNullable(@Nonnull final List<Problem> problems) {
        this.problems = problems;
        this.listener = CheckMonitor.getInstance().registerErrorInstance(this);
        this.value = null;
    }


    @Nullable
    public T getValue() {
        throw new IllegalStateException();
    }

    @Nullable
    public T result() {
        throw new IllegalStateException();
    }

    @Nonnull
    public <V> RetNullable<V> forwardError() {
        throw new IllegalStateException();
    }

    @Nonnull
    public RetVoid forwardVoid() {
        throw new IllegalStateException();
    }

    @Nonnull
    public <V> RetVal<V> forwardNonnull() {
        throw new IllegalStateException();
    }

    @Nonnull
    public RetVal<T> asNonnull() {
        throw new IllegalStateException();
    }

    @Nonnull
    public <V> RetVal<V> then(@Nonnull final NonnullReturnFunction<T, RetVal<V>> func) {
        if (isOk()) {
            return func.apply(result());
        }
        return forwardNonnull();
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> thenNullable(@Nonnull NonnullReturnFunction<T, RetNullable<R>> func) {
        return null;
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> thenNullableValue(@Nonnull Function<T, R> func) {
        return null;
    }

    @Nonnull
    @Override
    public RetVoid thenVoid(@Nonnull Consumer<T> consumer) {
        return null;
    }

    @Nonnull
    @Override
    public <R> RetVal<R> with(@Nonnull NonnullReturnFunction<T, RetVal<R>> func) {
        return null;
    }

    @Nonnull
    @Override
    public <R> RetVal<R> withValue(@Nonnull NonnullReturnFunction<T, R> func) {
        return null;
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> withNullable(@Nonnull NonnullReturnFunction<T, RetNullable<R>> func) {
        return null;
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> withNullableValue(@Nonnull Function<T, R> func) {
        return null;
    }

    @Nonnull
    public <V> RetNullable<V> thenRetNullable(@Nonnull final Function<T, RetNullable<V>> func) {
        if (isOk()) {
            return func.apply(result());
        }
        return forwardError();
    }

    @Nonnull
    public <V> RetNullable<V> thenNullable(@Nonnull final Function<T, V> func) {
        if (isOk()) {
            return RetNullable.ok(func.apply(result()));
        }
        return forwardError();
    }

    @Nonnull
    @Override
    public <R> RetVal<R> then(@Nonnull NonnullSupplier<RetVal<R>> supplier) {
        return null;
    }

    @Nonnull
    @Override
    public <R> RetVal<R> thenValue(@Nonnull NonnullSupplier<R> supplier) {
        return null;
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> thenNullable(@Nonnull NonnullSupplier<RetNullable<R>> supplier) {
        return null;
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> thenNullableValue(@Nonnull Supplier<R> supplier) {
        return null;
    }

    @Nonnull
    @Override
    public RetVoid thenVoid(@Nonnull NonnullSupplier<RetVoid> supplier) {
        return null;
    }

    @Nonnull
    @Override
    public RetVoid thenRun(@Nonnull Runnable runnable) {
        return null;
    }

    @Nonnull
    @Override
    public <R> RetVal<R> with(@Nonnull NonnullSupplier<RetVal<R>> supplier) {
        return null;
    }

    @Nonnull
    @Override
    public <R> RetVal<R> withValue(@Nonnull NonnullSupplier<R> supplier) {
        return null;
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> withNullable(@Nonnull NonnullSupplier<RetNullable<R>> supplier) {
        return null;
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> withNullableValue(@Nonnull Supplier<R> supplier) {
        return null;
    }

    @Nonnull
    @Override
    public RetVoid withVoid(@Nonnull NonnullSupplier<RetVoid> supplier) {
        return null;
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
        return false;
    }

    @Nonnull
    @Override
    public Collection<Problem> anyProblems() {
        return null;
    }

    @Nonnull
    @Override
    public Collection<Problem> validProblems() {
        return null;
    }

    @Nonnull
    @Override
    public String debugProblems(@Nonnull String joinedWith) {
        return null;
    }
}
