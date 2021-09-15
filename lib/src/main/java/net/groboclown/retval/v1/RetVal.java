// Released under the MIT License.
package net.groboclown.retval.v1;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class RetVal<T> implements ProblemContainer
{
    @Nonnull
    public static <T> RetVal<T> ok(@Nonnull T value) {
        throw new IllegalStateException();
    }

    @Nonnull
    public static <T> RetVal<T> error(@Nonnull Problem... problems) {
        throw new IllegalStateException();
    }

    @SafeVarargs
    @Nonnull
    public static <T> RetVal<T> error(@Nonnull Iterable<Problem>... problems) {
        throw new IllegalStateException();
    }

    @Nullable
    public T getValue() {
        throw new IllegalStateException();
    }

    @Nonnull
    public T result() {
        throw new IllegalStateException();
    }

    @Nonnull
    public <V> RetVal<V> forwardError() {
        throw new IllegalStateException();
    }

    @Nonnull
    public RetVoid forwardVoid() {
        throw new IllegalStateException();
    }

    @Nonnull
    public <V> RetNullable<V> forwardNullable() {
        throw new IllegalStateException();
    }

    @Nonnull
    public RetNullable<T> asNullable() {
        throw new IllegalStateException();
    }

    @Nonnull
    public <V> RetVal<V> thenValue(@Nonnull final NonnullFunction<T, V> func) {
        if (isOk()) {
            return ok(func.apply(result()));
        }
        return forwardError();
    }

    @Nonnull
    public <V> RetVal<V> then(@Nonnull final NonnullFunction<T, RetVal<V>> func) {
        if (isOk()) {
            return func.apply(result());
        }
        return forwardError();
    }
}
