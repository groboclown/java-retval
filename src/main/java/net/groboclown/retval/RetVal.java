// Released under the MIT License.
package net.groboclown.retval;

import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.groboclown.retval.function.NonnullConsumer;
import net.groboclown.retval.function.NonnullFunction;
import net.groboclown.retval.function.NonnullParamFunction;
import net.groboclown.retval.impl.RetGenerator;

/**
 * A non-null value holder version of a problem container.
 *
 * <p>Instances are intended to provide to other <code>Ret*</code> classes a value or problem, or
 * to provide to the using class a value or problems.  These instances represent a correct or
 * wrong environment that went into an expected value.  To that end, all methods that chain to
 * this one will not throw away the value; it will either be returned or offered to a
 * parameter function.
 *
 * <p>For simple cases, this is useful for return a validated value:
 * <pre>
 *     RetVal&lt;String&gt; loadEmail() {
 *         String email = readEmail();
 *         if (! isValidEmailFormat(email)) {
 *             return RetVal.fromProblem(LocalProblem.from("not valid email format: " + email));
 *         }
 *         return RetVal.ok(email);
 *     }
 * </pre>
 *
 * <p>In many cases, several validation checks may be necessary to build up a complex value.  In
 * this case, the values can be chained together:
 * <pre>
 *     RetVal&lt;String&gt; loadEmailForUser(String userName) {
 *         return
 *                 findEmailForUser(userName)
 *             .then((email) -&gt;
 *                 isValidEmailFormat(email)
 *                     ? RetVal.ok(email)
 *                     : RetVal.fromProblem(LocalProblem.from("not valid email format: " + email))
*              );
 *     }
 * </pre>
 *
 * <p>If the value is an intermediary value, used for constructing other values, then you may
 * need to return the value early, but that may require casting it to another type:
 * <pre>
 *     RetVal&lt;User&gt; loadUser(String userName) {
 *         RetVal&lt;String&gt; emailRes = loadEmail(userName);
 *         if (emailRes.hasProblems()) {
 *             return emailRes.forwardProblems();
 *         }
 *         // ... more data loading and validation
 *     }
 * </pre>
 *
 * <p>For use cases where the value may be partially constructed while including problems,
 * see the {@link WarningVal} class.
 *
 * @param <T> type of the contained value.
 */
public interface RetVal<T> extends ProblemContainer {

    // Developer notes:
    //   1. This class must be carefully constructed to allow one class to represent it and
    //      all the other Ret* interfaces.

    /**
     * Create a new RetVal instance that has a value and no problems.
     *
     * @param value non-null value.
     * @param <T> type of the value.
     * @return a RetVal containing the value.
     * @throws NullPointerException if the value is null.
     */
    @Nonnull
    static <T> RetVal<T> ok(@Nonnull final T value) {
        return RetGenerator.valOk(value);
    }

    /**
     * Create a new RetVal instance that has problems.
     *
     * @param problem the first problem.
     * @param problems optional list of other problems to include in this value.
     * @param <T> type of the value
     * @return a problem RetVal.
     */
    @Nonnull
    static <T> RetVal<T> fromProblem(
            @Nonnull final Problem problem,
            @Nonnull final Problem... problems
    ) {
        return RetGenerator.valFromProblem(Ret.joinProblems(problem, problems));
    }

    /**
     * Create a new RetVal instance that has problems stored in collections of
     * problems.  The arguments must contain at least one problem.
     *
     * @param problem the first problem.
     * @param problems optional list of other problems to include in this value.
     * @param <T> type of the value
     * @return a problem RetVal.
     * @throws IllegalArgumentException if no problems exist within the arguments.
     */
    @SafeVarargs
    @Nonnull
    static <T> RetVal<T> fromProblem(
            @Nonnull final Collection<Problem> problem,
            @Nonnull final Collection<Problem>... problems
    ) {
        return RetGenerator.valFromProblem(Ret.joinProblemSets(problem, problems));
    }

    /**
     * Create a new RetVal instance that has problems.  This is only valid if at least one
     * problem exists within all the arguments.
     *
     * <p>Normally, you would use this in situations where you collect several validations
     * together, when you know at least one of them has a problem, if not more.
     *
     * @param problem the first problem container.
     * @param problems optional list of other problem containers to include in this value.
     * @param <T> type of the value
     * @return a problem RetVal.
     * @throws IllegalArgumentException if no problems exist within the arguments.
     */
    @Nonnull
    static <T> RetVal<T> fromProblems(
            @Nonnull final ProblemContainer problem, @Nonnull final ProblemContainer... problems
    ) {
        // ProblemContainer instances include Ret* objects, which can contain values without
        // problems.  However, this form of the constructor requires at least one problem, and
        // the standard use case is for returning a known bad state, so any values are
        // considered to be okay to lose.
        return RetGenerator.valFromProblem(Ret.joinRetProblems(problem, problems));
    }

    /**
     * Create a new RetVal instance that has problems.  This is only valid if at least one
     * problem exists within all the arguments.
     *
     * <p>Normally, you would use this in situations where you collect several validations
     * together, when you know at least one of them has a problem, if not more.
     *
     * @param problem the first problem container.
     * @param problems optional list of other problem containers to include in this value.
     * @param <T> type of the value
     * @return a problem RetVal.
     * @throws IllegalArgumentException if no problems exist within the arguments.
     */
    @SafeVarargs
    @Nonnull
    static <T> RetVal<T> fromProblems(
            @Nonnull final Collection<ProblemContainer> problem,
            @Nonnull final Collection<ProblemContainer>... problems
    ) {
        return RetGenerator.valFromProblem(Ret.joinRetProblemSets(problem, problems));
    }

    /**
     * Get the value contained in this instance.  If this is an problem state, then the value will
     * be null.
     *
     * <p>This is usually helpful for log messages where a correctness check would impose
     * extra runtime overhead that logging doesn't care about.  This can also be used to return
     * a simple value to other parts of a program that use null to indicate an invalid setup but
     * that are separate from user notifications.
     *
     * @return the value, which will be null if there are problems.
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
     * Convert the value into an optional typed value only if there are no problems.
     *
     * @return the value as an optional type.  No checks are made against the problem state.
     */
    @Nonnull
    Optional<T> requireOptional();

    /**
     * Get the result, which is always non-null.  If this instance has problems, then a
     * runtime exception is thrown.  Therefore, it's necessary to perform a validity check
     * before calling.
     *
     * @return the non-null value, only if this instance is ok.
     * @throws IllegalStateException if this instance has problems.
     */
    @Nonnull
    T result();

    /**
     * Forward this object to a different typed RetVal instance.  This will only work when the
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
     * Convert this instance into a nullable instance with the same value type.  If this instance
     * has problems, they are returned.  This differs from {@link #forwardNullableProblems()}
     * by 1. keeping the same type, and 2. allowing for a non-problem state in the returned value.
     *
     * <p>A convenience function for situations where a receiver can expect valid uses of a
     * null value when the source is known to never return a null value.
     *
     * @return a nullable version of the same instance.
     */
    @Nonnull
    RetNullable<T> asNullable();

    /**
     * Validate the value in the checker.  The checker can perform any amount of validation
     * against the value as necessary, and returns an optional container of problems.  If no
     * problem is found, the checker can return null.  The checker is only invoked if the
     * value has no current problems.
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
    RetVal<T> thenValidate(
            @Nonnull final NonnullParamFunction<T, ProblemContainer> checker
    );

    /**
     * Return a non-null {@link RetVal} value using a function that itself returns a {@link RetVal},
     * taking the current non-nullable value as an argument.
     * The function is called only if this object has no problem.  If it has a problem, then
     * the current list of problems is returned as the new type.
     *
     * <p>This is similar to {@link #map(NonnullFunction)}, but logically
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
    <R> RetVal<R> then(@Nonnull final NonnullFunction<T, RetVal<R>> func);

    /**
     * Return a non-null {@link RetVal} value, using a function that returns a non-null value,
     * taking the current non-null value as an argument.
     * The function is called only if this object has no problem.  If it has a problem, then
     * the current list of problems is returned as the new type.
     *
     * <p>This is similar to {@link #then(NonnullFunction)}, but logically
     * different.  This method implies the functional argument performs a transformation of
     * the data type into another one without validation checks.
     *
     * @param func functional object that takes the current value as argument, and
     *             returns a transformed value.
     * @param <R> type of the returned value.
     * @return the problem of the current value, if it is an problem, or the object returned by
     *     the function.
     */
    @Nonnull
    <R> RetVal<R> map(@Nonnull final NonnullFunction<T, R> func);

    /**
     * Return a non-null {@link RetNullable} value using a function that itself returns a
     * {@link RetNullable}, taking the current non-null value as an argument.
     * The function is called only if this object has no problem.
     *
     * <p>This is similar to {@link #mapNullable(NonnullParamFunction)}, but logically
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
            @Nonnull final NonnullFunction<T, RetNullable<R>> func
    );

    /**
     * If this instance has no problems, then it runs the parameter with the
     * current value.  The returned value, which may be null, is wrapped in a
     * nullable value.  If this instance has problems, then the problem list is returned
     * and the parameter is not run.
     *
     * <p>This is similar to {@link #thenNullable(NonnullFunction)}, but logically
     * different.  This method implies the functional argument performs a transformation of
     * the data type into another one without validation checks.
     *
     * @param func the function to run.
     * @param <R> return value type
     * @return a transformed version of this object.
     */
    @Nonnull
    <R> RetNullable<R> mapNullable(@Nonnull final NonnullParamFunction<T, R> func);

    /**
     * Run the parameter, only if this instance has no problems.
     *
     * @param runner the runnable to execute if no problems exist
     * @return this instance
     */
    @Nonnull
    RetVal<T> thenRun(@Nonnull final Runnable runner);

    /**
     * Run the consumer with the current value, only if this instance
     * has no problems.
     *
     * @param consumer the consumer of this value to run if no problems exist
     * @return this instance.
     */
    @Nonnull
    RetVal<T> thenRun(@Nonnull final NonnullConsumer<T> consumer);

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
    RetVoid thenVoid(@Nonnull final NonnullConsumer<T> consumer);

    /**
     * Pass the value of this instance to the function, only if there are no problems.  Return
     * a void version of this instance if there are problems with it, or the return value from
     * the function.
     *
     * <p>This call will lose the contained value on return, so it's used to pass on the value
     * to another object.
     *
     * @param func consumer of this value.
     * @return if this instance has problems, then the problems are returned, otherwise the
     *      return value of the function is returned.
     */
    @Nonnull
    RetVoid thenVoid(@Nonnull final NonnullFunction<T, RetVoid> func);
}
