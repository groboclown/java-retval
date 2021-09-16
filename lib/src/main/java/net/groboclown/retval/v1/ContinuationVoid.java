// Released under the MIT License. 
package net.groboclown.retval.v1;

import java.util.function.Supplier;
import javax.annotation.Nonnull;


/**
 * Runs continuations while ignoring the current instance's value, if it even has a value.
 */
public interface ContinuationVoid {
    /**
     * Return a non-null {@link RetVal} value using a supplier that itself returns a {@link RetVal}.
     * The supplier is called only if this object has no error.
     *
     * @param supplier functional object that returns a RetVal.
     * @param <R> type of the returned value.
     * @return the error of the current value, if it is an error, or the object returned by
     *     the supplier.
     */
    @Nonnull
    <R> RetVal<R> then(@Nonnull NonnullSupplier<RetVal<R>> supplier);

    /**
     * Return a non-null {@link RetVal} value using a supplier that returns a value.
     * The supplier is called only if this object has no error.
     *
     * @param supplier functional object that returns a non-null value.
     * @param <R> return value type
     * @return a RetVal, either an error if this object has an error, or the value returned by
     *     the supplier.
     */
    @Nonnull
    <R> RetVal<R> thenValue(@Nonnull NonnullSupplier<R> supplier);

    @Nonnull
    <R> RetNullable<R> thenNullable(@Nonnull NonnullSupplier<RetNullable<R>> supplier);

    @Nonnull
    <R> RetNullable<R> thenNullableValue(@Nonnull Supplier<R> supplier);

    @Nonnull
    RetVoid thenVoid(@Nonnull NonnullSupplier<RetVoid> supplier);

    @Nonnull
    RetVoid thenRun(@Nonnull Runnable runnable);

    /**
     * Runs the supplier regardless of the current object's error state.  If the supplier
     * returns an error, or if the current object has an error, then the errors are combined.
     * Only if the current object has no error and the supplier returns no error does the
     * returned object have a value.  If the current object has an error and the supplier
     * returns a value, the value is lost.
     *
     * @param supplier functional object that returns a RetVal.  Always called.
     * @param <R> type of the returned value.
     * @return a RetVal with the combined errors of the current object and the supplier.  In the
     *     case where both objects have no errors, the returned object will contain the value of
     *     the supplier.
     */
    @Nonnull
    <R> RetVal<R> with(@Nonnull NonnullSupplier<RetVal<R>> supplier);

    @Nonnull
    <R> RetVal<R> withValue(@Nonnull NonnullSupplier<R> supplier);

    @Nonnull
    <R> RetNullable<R> withNullable(@Nonnull NonnullSupplier<RetNullable<R>> supplier);

    @Nonnull
    <R> RetNullable<R> withNullableValue(@Nonnull Supplier<R> supplier);

    @Nonnull
    RetVoid withVoid(@Nonnull NonnullSupplier<RetVoid> supplier);
}
