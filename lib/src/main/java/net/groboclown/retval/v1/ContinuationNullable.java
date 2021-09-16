// Released under the MIT License. 
package net.groboclown.retval.v1;

import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nonnull;


/**
 * Runs continuations from a value based on a null value contained in the current object.
 *
 * @param <T> the contained value type of this instance.
 */
public interface ContinuationNullable<T> extends ContinuationVoid {
    /**
     * Return a non-null {@link RetVal} value using a function that itself returns a {@link RetVal},
     * taking the current nullable value as an argument.
     * The function is called only if this object has no error.
     *
     * @param func functional object that returns a RetVal and takes the current value as argument.
     * @param <R> type of the returned value.
     * @return the error of the current value, if it is an error, or the object returned by
     *     the supplier.
     */
    @Nonnull
    <R> RetVal<R> then(@Nonnull NonnullReturnFunction<T, RetVal<R>> func);

    @Nonnull
    <R> RetNullable<R> thenNullable(@Nonnull NonnullReturnFunction<T, RetNullable<R>> func);

    @Nonnull
    <R> RetNullable<R> thenNullableValue(@Nonnull Function<T, R> func);

    @Nonnull
    RetVoid thenVoid(@Nonnull Consumer<T> consumer);

    /**
     * Runs the supplier regardless of the current object's error state.  If the supplier
     * returns an error, or if the current object has an error, then the errors are combined.
     * Only if the current object has no error and the supplier returns no error does the
     * returned object have a value.  If the current object has an error and the supplier returns
     * a value, the value is lost.
     *
     * @param func functional object that returns a RetVal.  Always called.
     * @param <R> type of the returned value.
     * @return a RetVal with the combined errors of the current object and the supplier.  In the
     *     case where both objects have no errors, the returned object will contain the value of
     *     the supplier.
     */
    @Nonnull
    <R> RetVal<R> with(@Nonnull NonnullReturnFunction<T, RetVal<R>> func);

    @Nonnull
    <R> RetVal<R> withValue(@Nonnull NonnullReturnFunction<T, R> func);

    @Nonnull
    <R> RetNullable<R> withNullable(@Nonnull NonnullReturnFunction<T, RetNullable<R>> func);

    @Nonnull
    <R> RetNullable<R> withNullableValue(@Nonnull Function<T, R> func);
}
