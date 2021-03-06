// Released under the MIT License.
package net.groboclown.retval;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.groboclown.retval.function.NonnullConsumer;
import net.groboclown.retval.function.NonnullFunction;
import net.groboclown.retval.function.NonnullParamFunction;
import net.groboclown.retval.function.NonnullReturnFunction;
import net.groboclown.retval.impl.RetGenerator;

/**
 * A problem container that contains a nullable value or problems, but
 * not both.
 *
 * @param <T> type of the contained value
 */
public interface RetNullable<T> extends ValuedProblemContainer<T>, ProblemContainer {

    // Developer notes:
    //   1. This class must be carefully constructed to allow one class to represent it and
    //      all the other Ret* interfaces.

    /**
     * Create a return object with a possibly null value and no problems.
     *
     * @param value value to set in the return object.
     * @param <T> type of the return object.
     * @return the return object, containing the value passed as argument and no problems.
     */
    @Nonnull
    static <T> RetNullable<T> ok(@Nullable final T value) {
        return RetGenerator.nullableOk(value);
    }

    /**
     * Create a nullable return object with at least one problem.
     *
     * <p>By taking the argument form of {@literal (Problem, Problem...)}, this method allows for
     * taking 1 or more problem values as variable length arguments (e.g.
     * {@literal RetNullable.fromProblem(a, b, c)}), while forcing the list of problems to be
     * non-empty.
     *
     * @param problem the first problem to add to the return object.
     * @param problems optional list of additional problems
     * @param <T> type of the return object; not directly used, because the object will have no
     *           value associated with it.
     * @return the return object with problems.
     */
    @Nonnull
    static <T> RetNullable<T> fromProblem(
            @Nonnull final Problem problem,
            @Nonnull final Problem... problems
    ) {
        return RetGenerator.nullableFromProblem(Ret.joinProblems(problem, problems));
    }


    /**
     * Create a nullable return object from collections of problems.
     *
     * @param problem the first problem collection to add to the return object.
     * @param problems optional list of additional problem collections.
     * @param <T> type of the return object; not directly used, because the object will have no
     *           value associated with it.
     * @return the return object with problems.
     * @throws IllegalArgumentException if the collection arguments contain no problems.
     */
    @SafeVarargs
    @Nonnull
    static <T> RetNullable<T> fromProblem(
            @Nonnull final Collection<Problem> problem,
            @Nonnull final Collection<Problem>... problems
    ) {
        return RetGenerator.nullableFromProblem(Ret.joinProblemSets(problem, problems));
    }

    @Nonnull
    static <T> RetNullable<T> fromProblems(
            @Nonnull final ProblemContainer problem,
            @Nonnull final ProblemContainer... problems
    ) {
        return RetGenerator.nullableFromProblem(Ret.joinRetProblems(problem, problems));
    }

    @SafeVarargs
    @Nonnull
    static <T> RetNullable<T> fromProblems(
            @Nonnull final Collection<ProblemContainer> problem,
            @Nonnull final Collection<ProblemContainer>... problems
    ) {
        return RetGenerator.nullableFromProblem(Ret.joinRetProblemSets(problem, problems));
    }


    /**
     * Get the value stored in this object, even if it has problems.  Note that this will
     * return null if the value is null or if it has problems.
     *
     * <p>This is usually helpful for log messages where a correctness check would impose
     * extra runtime overhead that logging doesn't care about.  This can also be used to return
     * a simple value to other parts of a program that use null to indicate an invalid setup but
     * that are separate from user notifications.
     *
     * @return the stored value
     */
    @Nullable
    T getValue();

    /**
     * Convert the value into an optional typed value.  Note that doing this will lose any
     * problem state, so checking for problems should be done before calling this.
     *
     * <p>This function has limited use.  It's provided here to allow support for systems
     * that use {@link Optional} values.
     *
     * @return the value as an optional type.  No checks are made against the problem state.
     */
    @Nonnull
    Optional<T> asOptional();

    /**
     * Get the result.  If this instance has problems, then a
     * runtime exception is thrown.  Therefore, it's necessary to perform a validity check
     * before calling.
     *
     * @return the stored value, possibly null.
     * @throws IllegalStateException if there are problems
     */
    @Nullable
    T result();

    /**
     * Get the stored value, or, if the stored value is null, return the non-null argument.  If
     * this value is a problem, then an exception is raised.
     *
     * @param defaultValue value to return if the stored value is null.
     * @return the stored value, or the defaultValue if the stored value is null.
     * @throws IllegalStateException if there are problems
     */
    @Nonnull
    default T result(@Nonnull T defaultValue) {
        T ret = result();
        return ret == null ? defaultValue : ret;
    }

    /**
     * Convert the value into an optional typed value only if there are no problems.
     *
     * @return the value as an optional type.
     * @throws IllegalStateException if there are problems
     */
    @Nonnull
    Optional<T> requireOptional();

    /**
     * Forward this object to RetVal instance, possibly as a different value type.  This will
     * only work when the instance has problems.
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
     * Forward this instance as a nullable with a different value type, but only if it has
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

    /**
     * Validate the value in the checker.  The checker can perform any amount of validation
     * against the value as necessary, and returns an optional container of problems.  If no
     * problem is found, the checker can return null.
     *
     * <p>Be careful with over-using this method.  {@link ValueBuilder} and {@link ProblemCollector}
     * provide better solutions for constructing validation problems within a method.  This
     * particular method finds practical use when a builder method returns an initially valid
     * value, then another method performs cross-value validation.
     *
     * @param checker function that checks the validity of the value.
     * @return a retval with additional problems, or this value if no problem is found.
     */
    @Nonnull
    RetNullable<T> thenValidate(
            @Nonnull final Function<T, ProblemContainer> checker
    );

    /**
     * Return a non-null {@link RetVal} value using a function that itself returns a {@link RetVal},
     * taking the current non-nullable value as an argument.
     * The function is called only if this object has no problem.  If it has a problem, then
     * the current list of problems is returned as the new type.
     *
     * <p>This is similar to {@link #map(NonnullReturnFunction)}, but logically
     * different.  This method implies the functional argument will use the value and perform
     * new processing to create a different value.
     *
     * @param func functional object that returns a RetVal and takes the current value as argument.
     * @param <R> type of the returned value.
     * @return the problems of the current value, or the object returned by
     *     the function if there are no problems (but the returned value may have its own
     *     set of problems).
     */
    @Nonnull
    <R> RetVal<R> then(@Nonnull final NonnullReturnFunction<T, RetVal<R>> func);

    /**
     * Return a non-null {@link RetVal} value, using a function that returns a non-null value,
     * taking the current non-null value as an argument.
     * The function is called only if this object has no problem.  If it has a problem, then
     * the current list of problems is returned as the new type.
     *
     * <p>This is similar to {@link #then(NonnullReturnFunction)}, but logically
     * different.  This method implies the functional argument performs a transformation of
     * the data type into another one without validation checks.
     *
     * @param func functional object that takes the current value as argument, and
     *             returns a transformed value.
     * @param <R> type of the returned value.
     * @return the problem of the current value, if it is a problem, or the object returned by
     *     the function.
     */
    @Nonnull
    <R> RetVal<R> map(@Nonnull final NonnullReturnFunction<T, R> func);

    /**
     * Return a non-null {@link RetNullable} value using a function that itself returns a
     * {@link RetNullable}, taking the current non-null value as an argument.
     * The function is called only if this object has no problem.
     *
     * <p>This is similar to {@link #mapNullable(Function)}, but logically
     * different.  This method implies the functional argument will use the value and perform
     * new processing to create a different value.
     *
     * @param func functional object that returns a RetNullable and takes the current
     *            value as argument.
     * @param <R> type of the returned value.
     * @return the problem of the current value, if it is a problem, or the object returned by
     *     the supplier.
     */
    @Nonnull
    <R> RetNullable<R> thenNullable(
            @Nonnull final NonnullReturnFunction<T, RetNullable<R>> func
    );

    /**
     * If this instance has no problems, then it runs the parameter with the
     * current value.  The returned value, which may be null, is wrapped in a
     * nullable value.  If this instance has problems, then the problem list is returned
     * and the parameter is not run.
     *
     * <p>This is similar to {@link #thenNullable(NonnullReturnFunction)}, but logically
     * different.  This method implies the functional argument performs a transformation of
     * the data type into another one without validation checks.
     *
     * @param func the function to run.
     * @param <R> return value type
     * @return a transformed version of this object.
     */
    @Nonnull
    <R> RetNullable<R> mapNullable(@Nonnull final Function<T, R> func);

    /**
     * Run the parameter, only if this instance has no problems.
     *
     * @param runner the runnable to execute if no problems exist
     * @return this instance
     */
    @Nonnull
    RetNullable<T> thenRunNullable(@Nonnull final Runnable runner);

    /**
     * Run the consumer with the current value, only if this instance
     * has no problems.
     *
     * @param consumer the consumer of this value to run if no problems exist
     * @return this instance.
     */
    @Nonnull
    RetNullable<T> thenRunNullable(@Nonnull final Consumer<T> consumer);

    /**
     * Pass the value of this instance to the consumer, only if there are no problems.  Return
     * a void version of this instance.
     *
     * <p>A note about usage: if the argument is a lambda that ignores the argument, then
     * the compiler will fail due to an ambiguous call.  There exist some use cases where the
     * argument value is no longer needed and can be safely ignored; for those scenarios, use
     * {@link #consume(Consumer)}.
     *
     * <p>This call will lose the contained value on return, so it's used to pass on the value
     * to another object.
     *
     * @param consumer consumer of this value.
     * @return a response that contains the problem state of the current value.
     */
    @Nonnull
    RetVoid thenVoid(@Nonnull final Consumer<T> consumer);

    /**
     * Pass the value of this instance to the consumer, only if there are no problems.  Return
     * the function's value.
     *
     * <p>A note about usage: if the argument is a lambda that ignores the argument, then
     * the compiler will fail due to an ambiguous call.  There exist some use cases where the
     * argument value is no longer needed and can be safely ignored; for those scenarios, use
     * {@link #produceVoid(NonnullReturnFunction)}.
     *
     * <p>This call will lose the contained value on return, so it's used to pass on the value
     * to another object.
     *
     * @param func consumer of this value.
     * @return a response that contains the problem state of the current value.
     */
    @Nonnull
    RetVoid thenVoid(@Nonnull final NonnullReturnFunction<T, RetVoid> func);

    /**
     * Pass the value of this instance to the consumer, only if there are no problems.  Return
     * a void version of this instance.
     *
     * <p>This call will lose the contained value on return, so it's used to pass on the value
     * to another object.
     *
     * @param consumer consumer of this value.
     * @return a response that contains the problem state of the current value.
     */
    @Nonnull
    RetVoid consume(@Nonnull final Consumer<T> consumer);

    /**
     * Pass the value of this instance to the consumer, only if there are no problems.  Return
     * the function's value.
     *
     * <p>This call will lose the contained value on return, so it's used to pass on the value
     * to another object.
     *
     * @param func consumer of this value.
     * @return a response that contains the problem state of the current value.
     */
    @Nonnull
    RetVoid produceVoid(@Nonnull final NonnullReturnFunction<T, RetVoid> func);

    /**
     * Require that this value contains a non-null value, otherwise produce a
     * value with the problem arguments.  If this value already has a problem,
     * the arguments are ignored.
     *
     * @param problem  problem to report if the value is null.
     * @param problems optional other problems to report if the value is null.
     * @return if non-null, return this value, otherwise the problem arguments.
     * @since 2.2.0
     */
    @Nonnull
    default RetVal<T> requireNonNull(
            @Nonnull final Problem problem,
            @Nonnull final Problem... problems) {
        if (hasProblems()) {
            return forwardProblems();
        }
        final T value = getValue();
        return value == null
                ? RetVal.fromProblem(problem, problems)
                : RetVal.ok(value);
    }

    /**
     * Returns this object as a {@link RetVal} if it is not null, otherwise a value with the
     * returned value.  If this value has a problem, that is still returned.
     *
     * @param defaultValue value to use if this object contains a null value.
     * @return the value with a non-null result, using the parameter if this object contains a
     *      null value.
     * @since 2.2.0
     */
    @Nonnull
    default RetVal<T> defaultAs(@Nonnull T defaultValue) {
        if (hasProblems()) {
            return forwardProblems();
        }
        final T value = getValue();
        return value == null
                       ? RetVal.ok(defaultValue)
                       : RetVal.ok(value);
    }

    /**
     * If this value has a problem, then it is returned as a {@link RetVoid}.  If it
     * contains a null value, then the equivalent of {@link RetVoid#ok()} is returned.
     * Otherwise, the consumer is called with the value, and {@link RetVoid#ok()} is returned.
     *
     * @param consumer consumer called with the value if there are no problems and the value is
     *                non-null.
     * @return the problems or a problem-free void value.
     * @since 2.2.0
     */
    @Nonnull
    default RetVoid consumeIfNonnull(@Nonnull final NonnullConsumer<T> consumer) {
        if (hasProblems()) {
            return forwardVoidProblems();
        }
        final T value = getValue();
        if (value != null) {
            consumer.accept(value);
        }
        return RetVoid.ok();
    }

    /**
     * If this value has a problem, then it is returned as a {@link RetVoid}.  If it
     * contains a null value, then the equivalent of {@link RetVoid#ok()} is returned.
     * Otherwise, the consumer is called with the value, and {@link RetVoid#ok()} is returned.
     *
     * @param func called with the value if there are no problems and the value is
     *                non-null, and the return value is returned by this call.
     * @return the problems or a problem-free void value.
     * @since 2.3.0
     */
    @Nonnull
    default RetVoid produceVoidIfNonnull(@Nonnull final NonnullFunction<T, RetVoid> func) {
        if (hasProblems()) {
            return forwardVoidProblems();
        }
        final T value = getValue();
        if (value != null) {
            return func.apply(value);
        }
        return RetVoid.ok();
    }

    /**
     * If this value has a problem, then it is returned.  If the stored value is null,
     * then {@literal defaultValue} is returned.  If the stored value is non-null, then
     * it is passed to the function and returned.
     *
     * @param <R> mapped-to value type
     * @param defaultValue value to return if the stored value is null.
     * @param func function to process the stored value if it is non-null.
     * @return this value's problems, or the mapped-to value, or the default value.
     * @since 2.2.0
     */
    @Nonnull
    default <R> RetVal<R> defaultOrMap(
            @Nonnull final R defaultValue,
            @Nonnull final NonnullFunction<T, R> func) {
        if (hasProblems()) {
            return forwardProblems();
        }
        final T value = getValue();
        if (value != null) {
            return RetVal.ok(func.apply(value));
        }
        return RetVal.ok(defaultValue);
    }


    /**
     * If this value has a problem, then it is returned.  If the stored value is null,
     * then {@literal defaultValue} is returned.  If the stored value is non-null, then
     * it is passed to the function and returned.
     *
     * @param <R> mapped-to value type
     * @param defaultValue value to return if the stored value is null.
     * @param func function to process the stored value if it is non-null.
     * @return this value's problems, or the mapped-to value, or the default value.
     * @since 2.2.0
     */
    @Nonnull
    default <R> RetVal<R> defaultOrThen(
            @Nonnull final R defaultValue,
            @Nonnull final NonnullFunction<T, RetVal<R>> func) {
        if (hasProblems()) {
            return forwardProblems();
        }
        final T value = getValue();
        if (value != null) {
            return func.apply(value);
        }
        return RetVal.ok(defaultValue);
    }

    /**
     * If this value has a problem, then it is returned.  If the stored value is null,
     * then {@literal null} is returned.  If the stored value is non-null, then
     * it is passed to the function and returned.
     *
     * @param <R> mapped-to value type
     * @param func function to process the stored value if it is non-null.
     * @return this value's problems, or the mapped-to value, or the default value.
     * @since 2.2.0
     */
    @Nonnull
    default <R> RetNullable<R> nullOrMap(
            @Nonnull final NonnullParamFunction<T, R> func) {
        if (hasProblems()) {
            return forwardNullableProblems();
        }
        final T value = getValue();
        if (value != null) {
            return RetNullable.ok(func.apply(value));
        }
        return RetNullable.ok(null);
    }


    /**
     * If this value has a problem, then it is returned.  If the stored value is null,
     * then {@literal null} is returned.  If the stored value is non-null, then
     * it is passed to the function and returned.
     *
     * @param <R> mapped-to value type
     * @param func function to process the stored value if it is non-null.
     * @return this value's problems, or the mapped-to value, or the default value.
     * @since 2.2.0
     */
    @Nonnull
    default <R> RetNullable<R> nullOrThenNullable(
            @Nonnull final NonnullFunction<T, RetNullable<R>> func) {
        if (hasProblems()) {
            return forwardNullableProblems();
        }
        final T value = getValue();
        if (value != null) {
            return func.apply(value);
        }
        return RetNullable.ok(null);
    }
}
