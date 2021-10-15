// Released under the MIT License.
package net.groboclown.retval.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.groboclown.retval.Problem;
import net.groboclown.retval.ProblemCollector;
import net.groboclown.retval.ProblemContainer;
import net.groboclown.retval.Ret;
import net.groboclown.retval.RetNullable;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.RetVoid;
import net.groboclown.retval.ValueBuilder;
import net.groboclown.retval.function.NonnullConsumer;
import net.groboclown.retval.function.NonnullFunction;
import net.groboclown.retval.function.NonnullParamFunction;
import net.groboclown.retval.function.NonnullReturnFunction;
import net.groboclown.retval.function.NonnullSupplier;
import net.groboclown.retval.monitor.ObservedMonitor;

/**
 * A shared implementation of the Ret* interfaces, which only contains problems.
 *
 * @param <T> type of the referenced value.
 */
public class MonitoredReturnProblem<T> implements RetVal<T>, RetNullable<T>, RetVoid {
    private final List<Problem> problems;
    private final ObservedMonitor.Listener listener;

    // Implementation notes for developers:
    // * The listener must only be called when the use case ensures that the call scenario
    //   indicates a "check" operation.  If the call requires a validation that would generate
    //   an exception, then the listener call must happen AFTER the exception check, because
    //   raising an exception means the check was not made, and the programmer will need to
    //   be made aware of the problems that lead up to the exception.
    // * Similarly, a problem state enforcement call does not mean that the call is a check.
    //   this can cause scenarios where, in testing, one problem state always happens, and,
    //   if marked as a check, it can hide places where, if problem states swap, the code
    //   would throw exceptions.
    // * All methods must be written to not lose data.  Calls that generate a transformation
    //   must either return the problems, return the value, or allow the value to be processed
    //   through an argument function.  Methods such as "RetVoid then() {}" can end up losing
    //   the value if there are no problems, so it is not allowed in this implementation.
    // * Large numbers of these objects can be created in a short period of time, but their
    //   expected lifetime is very short.  They also tend to be used in places where the program
    //   is already I/O bound.  Where possible, memory efficiency should be utilized over speed,
    //   but never at the risk of exposing the programmer to allowed and unchecked incorrect usage
    //   patterns.
    // * Even though this is immutable, care must be taken when considering the monitor's listener
    //   state, to make sure it's correctly maintained.  This is directly related to the use
    //   case of the class.

    // made package-protected to allow other classes in this package to pass in known
    // non-null, non-empty, immutable problem lists.
    MonitoredReturnProblem(@Nonnull final List<Problem> problems) {
        // an empty problem list is the marker for an error, so it can't accept a
        // problem state with no problems.
        if (problems.isEmpty()) {
            throw new IllegalArgumentException("no problems defined");
        }
        this.problems = problems;
        // The observable listeners are not passed to constructors.  This allows the developer
        // to know which specific place caused the value to be lost, not where it originated from.
        this.listener = ObservedMonitor.getCheckedInstance().registerInstance(this);
    }

    /**
     * Get the value contained in this instance.  If this is an error state, then the value will
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
    public T getValue() {
        // This does not indicate that a check was made.  An implicit one can be made by the
        // caller, but this method does not mark it as such, because all problem values are lost.
        return null;
    }

    /**
     * Convert the value into an optional typed value.  Note that doing this will lose any
     * problem state, so checking for problems should be done before calling this.
     *
     * <p>This function has limited use, generally after reporting problems, for programs
     * that use Optional typing, including using a null value to mark a problem state.
     *
     * @return the value as an optional type.  No checks are made against the problem state.
     */
    @Nonnull
    public Optional<T> asOptional() {
        // This does not indicate a check was made.  An implicit one can be made by the
        // caller, but this method does not mark it as such, because all problem values are lost.
        return Optional.empty();
    }

    /**
     * Get the result, which is always non-null.  If this instance has problems, then a
     * runtime exception is thrown.  Therefore, it's necessary to perform a validity check
     * before calling.
     *
     * @return the non-null value, only if this instance is ok.
     * @throws IllegalStateException if this instance has problems.
     */
    @Nonnull
    public T result() {
        // This does not indicate a check.  If this is marked as a check and the enforcement
        // passes, then that can disguise scenarios where testing doesn't encounter an error,
        // then in production, when an error is found, this fails.  The developer should be
        // notified during development about the incorrect usage.

        throw new IllegalStateException("contains problems");
    }

    @Nonnull
    @Override
    public Optional<T> requireOptional() {
        throw new IllegalStateException("contains problems");
    }

    @Nonnull
    public <V> MonitoredReturnProblem<V> forwardProblems() {
        @SuppressWarnings("unchecked")
        final MonitoredReturnProblem<V> t = (MonitoredReturnProblem<V>) this;
        return t;
    }

    @Nonnull
    public <V> RetNullable<V> forwardNullableProblems() {
        @SuppressWarnings("unchecked") final RetNullable<V> ret = (RetNullable<V>) this;
        return ret;
    }

    @Nonnull
    public RetVoid forwardVoidProblems() {
        return this;
    }

    @Nonnull
    @Override
    public RetNullable<T> thenValidate(@Nonnull final Function<T, ProblemContainer> checker) {
        return this;
    }

    @Nonnull
    @Override
    public <R> RetVal<R> then(@Nonnull final NonnullReturnFunction<T, RetVal<R>> func) {
        @SuppressWarnings("unchecked") final MonitoredReturnProblem<R> ret = (MonitoredReturnProblem<R>) this;
        return ret;
    }

    @Nonnull
    @Override
    public <R> RetVal<R> map(@Nonnull final NonnullReturnFunction<T, R> func) {
        @SuppressWarnings("unchecked") final MonitoredReturnProblem<R> ret = (MonitoredReturnProblem<R>) this;
        return ret;
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> thenNullable(
            @Nonnull final NonnullReturnFunction<T, RetNullable<R>> func
    ) {
        @SuppressWarnings("unchecked") final MonitoredReturnProblem<R> ret = (MonitoredReturnProblem<R>) this;
        return ret;
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> mapNullable(@Nonnull final Function<T, R> func) {
        @SuppressWarnings("unchecked") final MonitoredReturnProblem<R> ret = (MonitoredReturnProblem<R>) this;
        return ret;
    }

    @Nonnull
    @Override
    public RetNullable<T> thenRunNullable(@Nonnull final Runnable runner) {
        return this;
    }

    @Nonnull
    @Override
    public RetNullable<T> thenRunNullable(@Nonnull final Consumer<T> consumer) {
        return this;
    }

    @Nonnull
    @Override
    public RetVoid thenVoid(@Nonnull final Consumer<T> consumer) {
        return this;
    }

    @Nonnull
    @Override
    public RetVoid thenVoid(@Nonnull final NonnullReturnFunction<T, RetVoid> func) {
        return this;
    }

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
    public RetNullable<T> asNullable() {
        return this;
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
    public MonitoredReturnProblem<T> thenValidate(
            @Nonnull final NonnullParamFunction<T, ProblemContainer> checker
    ) {
        // This does not count as a validity check, so don't run the listener.
        return this;
    }

    @Nonnull
    @Override
    public <R> RetVal<R> then(@Nonnull final NonnullFunction<T, RetVal<R>> func) {
        @SuppressWarnings("unchecked") final RetVal<R> ret = (RetVal<R>) this;
        return ret;
    }

    @Override
    @Nonnull
    public <R> RetVal<R> map(@Nonnull final NonnullFunction<T, R> func) {
        @SuppressWarnings("unchecked") final RetVal<R> ret = (RetVal<R>) this;
        return ret;
    }

    @Override
    @Nonnull
    public <R> RetNullable<R> thenNullable(
            @Nonnull final NonnullFunction<T, RetNullable<R>> func
    ) {
        @SuppressWarnings("unchecked") final RetNullable<R> ret = (RetNullable<R>) this;
        return ret;
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> mapNullable(@Nonnull final NonnullParamFunction<T, R> func) {
        @SuppressWarnings("unchecked") final RetNullable<R> ret = (RetNullable<R>) this;
        return ret;
    }

    @Nonnull
    @Override
    public <R> RetVal<R> then(@Nonnull final NonnullSupplier<RetVal<R>> supplier) {
        @SuppressWarnings("unchecked") final RetVal<R> ret = (RetVal<R>) this;
        return ret;
    }

    @Nonnull
    @Override
    public <R> RetVal<R> map(@Nonnull final NonnullSupplier<R> supplier) {
        @SuppressWarnings("unchecked") final RetVal<R> ret = (RetVal<R>) this;
        return ret;
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> thenNullable(@Nonnull final NonnullSupplier<RetNullable<R>> supplier) {
        @SuppressWarnings("unchecked") final RetNullable<R> ret = (RetNullable<R>) this;
        return ret;
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> mapNullable(@Nonnull final Supplier<R> supplier) {
        @SuppressWarnings("unchecked") final RetNullable<R> ret = (RetNullable<R>) this;
        return ret;
    }

    @Nonnull
    @Override
    public RetVoid thenVoid(@Nonnull final NonnullSupplier<RetVoid> supplier) {
        return this;
    }

    /**
     * Run the parameter, only if this instance has no problems.
     *
     * @param runner the runnable to execute if no problems exist
     * @return this instance
     */
    @Nonnull
    public MonitoredReturnProblem<T> thenRun(@Nonnull final Runnable runner) {
        return this;
    }

    /**
     * Run the consumer with the current value, only if this instance
     * has no problems.
     *
     * @param consumer the consumer of this value to run if no problems exist
     * @return this instance.
     */
    @Nonnull
    public MonitoredReturnProblem<T> thenRun(@Nonnull final NonnullConsumer<T> consumer) {
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
    public RetVoid thenVoid(@Nonnull final NonnullConsumer<T> consumer) {
        return this;
    }

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
    public RetVoid thenVoid(@Nonnull final NonnullFunction<T, RetVoid> func) {
        return this;
    }

    @Override
    public boolean hasProblems() {
        // This alone does not make a check.  The problems themselves must be extracted or
        // forwarded.
        return true;
    }

    @Override
    public boolean isProblem() {
        return hasProblems();
    }

    @Override
    public boolean isOk() {
        // This alone does not make a check.  The problems themselves must be extracted or
        // forwarded.
        return false;
    }

    @Nonnull
    @Override
    public Collection<Problem> anyProblems() {
        // This only counts as a check if there actually are problems.
        this.listener.onObserved();
        return this.problems;
    }

    @Nonnull
    @Override
    public Collection<Problem> validProblems() {
        // Because the hasProblems and isOk calls do not count as observations,
        // this one does.
        this.listener.onObserved();
        return this.problems;
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
        return "Ret(" + this.problems.size() + " problems: " +
                debugProblems("; ") + ")";
    }
}
