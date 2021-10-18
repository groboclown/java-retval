// Released under the MIT License. 
package net.groboclown.retval.impl;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.groboclown.retval.Problem;
import net.groboclown.retval.RetNullable;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.RetVoid;
import net.groboclown.retval.env.ReturnTypeFactoryDetection;

import static net.groboclown.retval.impl.CollectionUtil.copyNonNullValues;


/**
 * Central handler for creating Ret* instances.
 */
public class RetGenerator {
    private static ReturnTypeFactory INSTANCE;

    static {
        INSTANCE = ReturnTypeFactoryDetection.discoverReturnTypeFactory();
    }


    private RetGenerator() {
        // No state, so no constructor.
    }


    /**
     * Create a {@link RetNullable} instance with a value.
     *
     * @param value value to store in the return object.
     * @param <T> type of the value.
     * @return the return object.
     */
    @Nonnull
    public static <T> RetNullable<T> nullableOk(@Nullable final T value) {
        return INSTANCE.createNullableOk(value);
    }


    /**
     * Create a {@link RetNullable} instance with one or more problems.
     *
     * @param problems collection of problem values.
     * @param <T> type of the value.
     * @return the return object.
     */
    @Nonnull
    public static <T> RetNullable<T> nullableFromProblem(
            @Nonnull final Collection<Problem> problems
    ) {
        final List<Problem> clonedProblems = copyNonNullValues(problems);
        if (clonedProblems.isEmpty()) {
            throw new IllegalArgumentException(
                    "Problem return objects must have at least 1 problem");
        }
        return INSTANCE.createNullableFromProblems(clonedProblems);
    }


    /**
     * Create a new RetVal instance that has a value and no problems.
     *
     * @param value non-null value.
     * @param <T> type of the value.
     * @return a RetVal containing the value.
     * @throws NullPointerException if the value is null.
     */
    @Nonnull
    public static <T> RetVal<T> valOk(@Nonnull final T value) {
        return INSTANCE.createValOk(Objects.requireNonNull(value, "ok value"));
    }


    /**
     * Create a new RetVal instance that has errors.
     *
     * @param problems list of other problems to include in this value.
     * @param <T> type of the value
     * @return an error RetVal.
     */
    @Nonnull
    public static <T> RetVal<T> valFromProblem(@Nonnull final Collection<Problem> problems) {
        final List<Problem> clonedProblems = copyNonNullValues(problems);
        if (clonedProblems.isEmpty()) {
            throw new IllegalArgumentException(
                    "Problem return objects must have at least 1 problem");
        }
        return INSTANCE.createValFromProblems(clonedProblems);
    }


    /**
     * Return a void object with no problems.
     *
     * @return a no-problem void object.
     */
    @Nonnull
    public static RetVoid voidOk() {
        return INSTANCE.createVoidOk();
    }


    /**
     * Constructs a {@link RetVoid} instance with the collections of problems.
     * This is optimized to reduce the memory load where easy.  If the problem list is
     * empty, then
     *
     * @param problems collection of problems
     * @return a problem version of a void ret.
     */
    @Nonnull
    public static RetVoid voidFromProblem(@Nonnull final Collection<Problem> problems) {
        final List<Problem> clonedProblems = copyNonNullValues(problems);
        if (clonedProblems.isEmpty()) {
            return voidOk();
        }
        return INSTANCE.createVoidFromProblems(clonedProblems);
    }


    /**
     * Allows for querying the active return type factory.  Primarily useful for unit tests or
     * other systems that need runtime replacements of the type.
     *
     * @return the current return type factory instance.
     */
    @Nonnull
    public static ReturnTypeFactory getFactory() {
        return INSTANCE;
    }

    /**
     * Allows for replacing the active return type factory.  Primarily useful for unit tests or
     * other systems that need runtime replacements of the type.
     *
     * @param factory the new factory to use; must be non-null.
     */
    public static void setFactory(@Nonnull final ReturnTypeFactory factory) {
        INSTANCE = Objects.requireNonNull(factory, "factory");
    }
}
