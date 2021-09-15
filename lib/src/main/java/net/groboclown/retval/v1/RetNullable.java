// Released under the MIT License.
package net.groboclown.retval.v1;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public abstract class RetNullable<T> implements ProblemContainer {
    @Nonnull
    public static <T> RetNullable<T> ok(@Nonnull T value) {
        throw new IllegalStateException();
    }

    @Nonnull
    public static <T> RetNullable<T> error(@Nonnull Problem... problems) {
        throw new IllegalStateException();
    }

    @SafeVarargs
    @Nonnull
    public static <T> RetNullable<T> error(@Nonnull Iterable<Problem>... problems) {
        throw new IllegalStateException();
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
}
