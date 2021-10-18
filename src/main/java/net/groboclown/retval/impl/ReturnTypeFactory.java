// Released under the MIT License. 
package net.groboclown.retval.impl;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.groboclown.retval.Problem;
import net.groboclown.retval.RetNullable;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.RetVoid;

/**
 * An abstract factory pattern for creating the return type objects.
 *
 * <p>Implementations may assume that all returned values work together as a whole, so
 * the values may take advantage of underlying optimizations.  For example, passing the
 * same unmodifiable list directly between returned values, rather than having to go through
 * the current factory to create new instances.
 */
public interface ReturnTypeFactory {
    /**
     * Create a {@link RetNullable} instance with a value.
     *
     * @param value value to store in the return object.
     * @param <T> type of the value.
     * @return the return object.
     */
    @Nonnull
    <T> RetNullable<T> createNullableOk(@Nullable final T value);


    /**
     * Create a {@link RetNullable} instance with one or more problems.  The
     * problems are checked before calling to have at least 1 value, no values
     * are null, and the collection is unmodifiable.
     *
     * @param problems collection of 1 or more problem values.
     * @param <T> type of the value.
     * @return the return object.
     */
    @Nonnull
    <T> RetNullable<T> createNullableFromProblems(@Nonnull final List<Problem> problems);


    /**
     * Create a new RetVal instance that has a value and no problems.  The passed-in value
     * is checked before calling to ensure it is non-null.
     *
     * @param value non-null value; guaranteed to be non-null.
     * @param <T> type of the value.
     * @return a RetVal containing the value.
     */
    @Nonnull
    <T> RetVal<T> createValOk(@Nonnull final T value);


    /**
     * Create a new RetVal instance that has errors.  The
     * problems are checked before calling to have at least 1 value, no values
     * are null, and the collection is unmodifiable.
     *
     * @param problems list of other problems to include in this value; guaranteed to have at
     *                 least 1 value.
     * @param <T> type of the value
     * @return an error RetVal.
     */
    @Nonnull
    <T> RetVal<T> createValFromProblems(@Nonnull final List<Problem> problems);


    /**
     * Return a void object with no problems.
     *
     * @return a no-problem void object.
     */
    @Nonnull
    RetVoid createVoidOk();


    /**
     * Constructs a {@link RetVoid} instance with the collections of problems.
     * The list of problems is guaranteed by the caller to have at least 1 problem, no values
     * are null, and the collection is unmodifiable.
     *
     * @param problems list of problems that should be included in this object.
     * @return a RetVoid with all the given problems.
     */
    @Nonnull
    RetVoid createVoidFromProblems(@Nonnull final List<Problem> problems);
}
