// Released under the MIT License.
package net.groboclown.retval;

import java.util.Collection;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import net.groboclown.retval.function.NonnullSupplier;
import net.groboclown.retval.impl.RetGenerator;


/**
 * A simple returnable that can have problems, but no value.
 *
 * <p>Because these instances have no value, they allow for more flexibility in their
 * functionality than the other classes.
 */
@Immutable
public interface RetVoid extends ProblemContainer {

    // Developer notes:
    //   1. This class must be carefully constructed to allow one class to represent it and
    //      all the other Ret* interfaces.

    /**
     * Return a void object with no problems.
     *
     * @return a no-problem void object.
     */
    @Nonnull
    static RetVoid ok() {
        return RetGenerator.voidOk();
    }

    /**
     * Constructs a {@link RetVoid} instance with the collections of problems.
     * This is optimized to reduce the memory load where easy.
     *
     * @param problemSet first collection of problems
     * @param problemSets vararg optional problems that should be included in this object.
     * @return a RetVoid with all the given problems.
     */
    @SafeVarargs
    @Nonnull
    static RetVoid fromProblem(
            @Nonnull final Collection<Problem> problemSet,
            @Nonnull final Collection<Problem>... problemSets
    ) {
        return RetGenerator.voidFromProblem(problemSet, problemSets);
    }

    /**
     * Create a RetVoid containing the passed-in problems.  If there was
     * no problem in any of the arguments, then this will return a no-problem
     * value.
     *
     * @param problem initial problem
     * @param problems vararg optional list of problems.
     * @return a new void instance, possibly without problems.
     */
    @Nonnull
    static RetVoid fromProblem(
            @Nullable final Problem problem,
            final Problem... problems) {
        return RetGenerator.voidFromProblem(problem, problems);
    }

    /**
     * Create a RetVoid containing the passed-in problems.  If there was
     * no problem in any of the arguments, then this will return a no-problem
     * value.
     *
     * @param retSet primary collection of problem containers.
     * @param retSets vararg optional problem container collections.
     * @return a new void instance, possibly without problems.
     */
    @SafeVarargs
    @Nonnull
    static RetVoid fromProblems(
            @Nonnull final Collection<ProblemContainer> retSet,
            @Nonnull final Collection<ProblemContainer>... retSets
    ) {
        return RetGenerator.voidFromProblems(retSet, retSets);
    }

    /**
     * Create a RetVoid containing the passed-in problems.  If there was
     * no problem in any of the arguments, then this will return a no-problem
     * value.
     *
     * @param ret a problem container
     * @param rets an optional list of problem containers.
     * @return a new void instance, possibly without problems.
     */
    @Nonnull
    static RetVoid fromProblems(
            @Nullable final ProblemContainer ret,
            @Nonnull final ProblemContainer... rets
    ) {
        return RetGenerator.voidFromProblems(ret, rets);
    }

    /**
     * Return a non-null {@link RetVal} value using a supplier that itself returns a {@link RetVal}.
     * The supplier is called only if this object has no problem.
     *
     * @param supplier functional object that returns a RetVal.
     * @param <R> type of the returned value.
     * @return the problem of the current value, if it is a problem, or the object returned by
     *     the supplier.
     */
    @Nonnull
    <R> RetVal<R> then(@Nonnull final NonnullSupplier<RetVal<R>> supplier);

    /**
     * Return a non-null {@link RetVal} value using a supplier that returns a value.
     * The supplier is called only if this object has no problem.
     *
     * <p>Formally, this doesn't "map" one value to another.  However, for symmetry with the
     * other Ret classes, it is here called map.
     *
     * @param supplier functional object that returns a non-null value.
     * @param <R> return value type
     * @return a RetVal, either a problem if this object has a problem, or the value returned by
     *     the supplier.
     */
    @Nonnull
    <R> RetVal<R> map(@Nonnull final NonnullSupplier<R> supplier);

    /**
     * If there is no problem, run the supplier and return its value.  Otherwise, return
     * this object's problems.
     *
     * @param supplier supplier of the return value; run only if this object has no problems.
     * @param <R> return value type
     * @return the problems in this object, or the supplier's return value.
     */
    @Nonnull
    <R> RetNullable<R> thenNullable(
            @Nonnull final NonnullSupplier<RetNullable<R>> supplier
    );


    /**
     * Return a non-null {@link RetNullable} value using a supplier that returns a value.
     * The supplier is called only if this object has no problem.
     *
     * <p>Formally, this doesn't "map" one value to another.  However, for symmetry with the
     * other Ret classes, it is here called map.
     *
     * @param supplier functional object that returns a non-null value.
     * @param <R> return value type
     * @return a RetVal, either a problem if this object has an problem, or the value returned by
     *     the supplier.
     */
    @Nonnull
    <R> RetNullable<R> mapNullable(@Nonnull final Supplier<R> supplier);


    /**
     * Return the supplier if there is no problem.
     *
     * @param supplier function to run
     * @return this object if there are problems in this object, otherwise the supplier's
     *      return value.
     */
    @Nonnull
    RetVoid thenVoid(@Nonnull final NonnullSupplier<RetVoid> supplier);

    /**
     * Run the runnable if there is no problem.
     *
     * @param runnable function to run
     * @return this object.
     */
    @Nonnull
    RetVoid thenRun(@Nonnull final Runnable runnable);

    /**
     * Forward this object to a typed RetVal instance.  This will only work when the
     * instance has problems.
     *
     * <p>The most common use case is when a value construction requires multiple steps, and
     * an early step requires early exit from the function.  This allows a memory efficient
     * type casting of the problems to the construction function's type.
     *
     * @param <V> altered type.
     * @return the type-altered version
     * @throws IllegalStateException if this instance does not have problems.
     */
    @Nonnull
    <V> RetVal<V> forwardProblems();

    /**
     * Forward this instance as a nullable with a value type, but only if it has
     * problems.  If it does not have problems, then a runtime exception is thrown.
     *
     * <p>The most common use case is when a value construction requires multiple steps, and
     * an early step requires early exit from the function.  This allows a memory efficient
     * type casting of the problems to the construction function's type.
     *
     * @param <V> destination type
     * @return the value, only if this instance has problems.
     */
    @Nonnull
    <V> RetNullable<V> forwardNullableProblems();

    /**
     * Forward this instance as a value-less object, but only if it has
     * problems.  If it does not have problems, then a runtime exception is thrown.
     *
     * <p>The most common use case is when a value construction requires multiple steps, and
     * an early step requires early exit from the function.  This allows a memory efficient
     * type casting of the problems to the construction function's type.
     *
     * @return the value, only if this instance has problems.
     */
    @Nonnull
    RetVoid forwardVoidProblems();

}
