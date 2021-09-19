// Released under the MIT License.
package net.groboclown.retval;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.groboclown.retval.function.NonnullReturnFunction;
import net.groboclown.retval.monitor.ObservedMonitor;

/**
 * A problem container that contains a nullable value or problems, but
 * not both.
 *
 * @param <T> type of the contained value
 */
public class RetNullable<T> implements ProblemContainer {

    // Developer notes:
    // * Much of the logic here is a copy of the RetVal class, but with minor variations
    //   on typing to use nullable types.  The logic that applies to the RetVal class also
    //   applies here.
    // * There could be a valid claim to say that RetVal and RetNullable could share an
    //   underlying class to contain that logic.  Maybe that will be pursued in future versions.
    //   For now, the logic is duplicated for the sake of memory.

    private final List<Problem> problems;
    private final ObservedMonitor.Listener listener;
    private final T value;

    @Nonnull
    public static <T> RetNullable<T> ok(@Nullable final T value) {
        return new RetNullable<>(value);
    }

    @Nonnull
    public static <T> RetNullable<T> fromProblem(
            @Nonnull final Problem problem,
            @Nonnull final Problem... problems
    ) {
        return new RetNullable<>(Ret.joinProblems(problem, problems));
    }

    @SafeVarargs
    @Nonnull
    public static <T> RetNullable<T> fromProblem(@Nonnull final Iterable<Problem>... problems) {
        throw new IllegalStateException();
    }

    @Nonnull
    public static <T> RetNullable<T> fromProblems(
            @Nonnull final ProblemContainer problem,
            @Nonnull final ProblemContainer... problems
    ) {
        return new RetNullable<>(Ret.joinRetProblems(problem, problems));
    }

    // package-protected to allow for memory efficient problem passing.
    // Must be non-null, non-empty, and immutable.
    RetNullable(@Nonnull final List<Problem> problems) {
        if (problems.isEmpty()) {
            throw new IllegalArgumentException("no problems defined");
        }
        this.problems = problems;
        this.listener = ObservedMonitor.getCheckedInstance().registerInstance(this);
        this.value = null;
    }

    private RetNullable(@Nullable final T value) {
        this.problems = Collections.emptyList();
        this.listener = ObservedMonitor.getCheckedInstance().registerInstance(this);
        this.value = value;
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
    public T getValue() {
        // Not considered a check, so the check listener is not called
        return this.value;
    }

    /**
     * Convert the value into an optional typed value.  Note that doing this will lose any
     * problem state, so checking for problems should be done before calling this.
     *
     * <p>This function has limited use, generally after reporting problems, for programs
     * that use Optional typing.
     *
     * @return the value as an optional type.  No checks are made against the problem state.
     */
    @Nonnull
    public Optional<T> asOptional() {
        // This does not indicate a check was made.  An implicit one can be made by the
        // caller, but this method does not mark it as such, because all problem values are lost.
        return Optional.ofNullable(this.value);
    }

    /**
     * Get the result.  If this instance has problems, then a
     * runtime exception is thrown.  Therefore, it's necessary to perform a validity check
     * before calling.
     *
     * @return the stored value, possibly null.
     * @throws IllegalStateException if there are problems
     */
    @Nullable
    public T result() {
        // Not considered a check, so the check listener is not called
        Ret.enforceNoProblems(this.problems);
        return this.value;
    }

    /**
     * Convert the value into an optional typed value only if there are no problems.
     *
     * @return the value as an optional type.
     * @throws IllegalStateException if there are problems
     */
    @Nonnull
    public Optional<T> requireOptional() {
        // Not considered a check, so the check listener is not called
        Ret.enforceNoProblems(this.problems);
        return Optional.ofNullable(this.value);
    }

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
    public <V> RetVal<V> forwardProblems() {
        throw new IllegalStateException();
    }

    /**
     * Forward this instance as a nullable with a different value type, but only if it has
     * errors.  If it does not have errors, then a runtime exception is thrown.
     *
     * <p>The most common use case is when a value construction requires multiple steps, and
     * an early step requires early exit from the function.  This allows a memory efficient
     * type casting of the problems to the construction function's type.
     *
     * @param <V> destination type
     * @return the value, only if this instance has errors.
     */
    @Nonnull
    public <V> RetNullable<V> forwardNullableProblems() {
        throw new IllegalStateException();
    }

    /**
     * Forward this instance as a value-less object, but only if it has
     * errors.  If it does not have errors, then a runtime exception is thrown.
     *
     * <p>The most common use case is when a value construction requires multiple steps, and
     * an early step requires early exit from the function.  This allows a memory efficient
     * type casting of the problems to the construction function's type.
     *
     * @return the value, only if this instance has errors.
     */
    @Nonnull
    public RetVoid forwardVoidProblems() {
        Ret.enforceHasProblems(this.problems);
        // pass the check to the returned value
        this.listener.onObserved();
        // memory efficient access.
        return new RetVoid(this.problems);
    }

    /**
     * Convert this instance into a non-null instance with the same value type.  If this instance
     * has problems, they are returned.  If this value is null and there are no problems, then
     * a value with a problem indicating the null state is returned.
     *
     * <p>A convenience function for situations where a receiver can expect valid uses of a
     * null value when the source is known to never return a null value.
     *
     * @return a nullable version of the same instance.
     */
    @Nonnull
    public RetVal<T> asNonnull() {
        throw new IllegalStateException();
    }

    /**
     * Validate the value in the checker.  The checker can perform any amount of validation
     * against the value as necessary, and returns an optional container of errors.  If no
     * error is found, the checker can return null.
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
    public RetNullable<T> thenValidate(
            @Nonnull final Function<T, ProblemContainer> checker
    ) {
        // This does not count as a validity check, so don't run the listener.
        if (! this.problems.isEmpty()) {
            return this;
        }
        final ProblemContainer discovered = checker.apply(this.value);
        if (discovered != null && discovered.hasProblems()) {
            // Move the checking to the returned value.
            this.listener.onObserved();
            return RetNullable.fromProblems(this, discovered);
        }
        return this;
    }

    /**
     * Return a non-null {@link RetVal} value using a function that itself returns a {@link RetVal},
     * taking the current non-nullable value as an argument.
     * The function is called only if this object has no error.  If it has an error, then
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
    public <R> RetVal<R> then(@Nonnull final NonnullReturnFunction<T, RetVal<R>> func) {
        if (isOk()) {
            return func.apply(result());
        }
        return forwardProblems();
    }

    /**
     * Return a non-null {@link RetVal} value, using a function that returns a non-null value,
     * taking the current non-null value as an argument.
     * The function is called only if this object has no problem.  If it has an error, then
     * the current list of problems is returned as the new type.
     *
     * <p>This is similar to {@link #then(NonnullReturnFunction)}, but logically
     * different.  This method implies the functional argument performs a transformation of
     * the data type into another one without validation checks.
     *
     * @param func functional object that takes the current value as argument, and
     *             returns a transformed value.
     * @param <R> type of the returned value.
     * @return the error of the current value, if it is an error, or the object returned by
     *     the function.
     */
    @Nonnull
    public <R> RetVal<R> map(@Nonnull final NonnullReturnFunction<T, R> func) {
        if (isOk()) {
            return RetVal.ok(func.apply(result()));
        }
        return forwardProblems();
    }

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
     * @return the error of the current value, if it is an error, or the object returned by
     *     the supplier.
     */
    @Nonnull
    public <R> RetNullable<R> thenNullable(
            @Nonnull final NonnullReturnFunction<T, RetNullable<R>> func
    ) {
        throw new IllegalStateException();
    }

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
    public <R> RetNullable<R> mapNullable(@Nonnull final Function<T, R> func) {
        throw new IllegalStateException();
    }

    /**
     * Run the parameter, only if this instance has no problems.
     *
     * @param runner the runnable to execute if no problems exist
     * @return this instance
     */
    @Nonnull
    public RetNullable<T> thenRunNullable(@Nonnull final Runnable runner) {
        throw new IllegalStateException();
    }

    /**
     * Run the consumer with the current value, only if this instance
     * has no problems.
     *
     * @param consumer the consumer of this value to run if no problems exist
     * @return this instance.
     */
    @Nonnull
    public RetNullable<T> thenRunNullable(@Nonnull final Consumer<T> consumer) {
        // thenWrapped performs a "onChecked" call, and this method
        // returns "this", so this cannot use the wrapped helper.
        // By returning "this", it means that the "checked" call can only be done
        // if this function is considered performing a check, which this isn't.
        if (this.problems.isEmpty()) {
            consumer.accept(this.value);
        }
        return this;
    }

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
    public RetVoid thenVoid(@Nonnull final Consumer<T> consumer) {
        throw new IllegalStateException();
    }

    @Override
    public boolean isProblem() {
        this.listener.onObserved();
        return ! this.problems.isEmpty();
    }

    @Override
    public boolean hasProblems() {
        this.listener.onObserved();
        return ! this.problems.isEmpty();
    }

    @Override
    public boolean isOk() {
        this.listener.onObserved();
        return this.problems.isEmpty();
    }

    @Nonnull
    @Override
    public Collection<Problem> anyProblems() {
        // Consider this as checking for problems only if there are problems.
        // Generally, this combines the problems
        // in this instance with a larger collection, which can itself be used to check if any
        // of the values had problems.
        if (! this.problems.isEmpty()) {
            this.listener.onObserved();
        }
        return this.problems;
    }

    @Nonnull
    @Override
    public Collection<Problem> validProblems() {
        // Mark as checked before ensuring it has problems, so that the
        // developer isn't bombarded with double errors.
        this.listener.onObserved();
        return Ret.enforceHasProblems(this.problems);
    }

    @Nonnull
    @Override
    public String debugProblems(@Nonnull final String joinedWith) {
        return Ret.joinProblemMessages(joinedWith, this.problems);
    }

    @Override
    public void joinProblemsWith(@Nonnull final Collection<Problem> problemList) {
        // This acts as closing off this value and passing the problem state to the
        // list.
        this.listener.onObserved();
        problemList.addAll(this.problems);
    }

    @Override
    public String toString() {
        return "RetNullable("
                + (this.problems.isEmpty()
                        ? ("value: " + this.value)
                        : (this.problems.size() + " problems: " + debugProblems("; "))
                ) + ")";
    }
}
