// Released under the MIT License.
package net.groboclown.retval;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.groboclown.retval.function.NonnullConsumer;
import net.groboclown.retval.function.NonnullFunction;
import net.groboclown.retval.function.NonnullParamFunction;
import net.groboclown.retval.function.NonnullSupplier;
import net.groboclown.retval.monitor.ObservedMonitor;

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
public class RetVal<T> implements ProblemContainer {
    private final List<Problem> problems;
    private final ObservedMonitor.Listener listener;
    private final T value;

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

    /**
     * Create a new RetVal instance that has a value and no problems.
     *
     * @param value non-null value.
     * @param <T> type of the value.
     * @return a RetVal containing the value.
     * @throws NullPointerException if the value is null.
     */
    @Nonnull
    public static <T> RetVal<T> ok(@Nonnull final T value) {
        return new RetVal<>(Objects.requireNonNull(value));
    }

    /**
     * Create a new RetVal instance that has errors.
     *
     * @param problem the first problem.
     * @param problems optional list of other problems to include in this value.
     * @param <T> type of the value
     * @return an error RetVal.
     */
    @Nonnull
    public static <T> RetVal<T> fromProblem(
            @Nonnull final Problem problem,
            @Nonnull final Problem... problems
    ) {
        return new RetVal<>(Ret.joinProblems(problem, problems));
    }

    /**
     * Create a new RetVal instance that has errors stored in collections of
     * problems.  The arguments must contain at least one problem.
     *
     * @param problem the first problem.
     * @param problems optional list of other problems to include in this value.
     * @param <T> type of the value
     * @return an error RetVal.
     * @throws IllegalArgumentException if no problems exist within the arguments.
     */
    @SafeVarargs
    @Nonnull
    public static <T> RetVal<T> fromProblem(
            @Nonnull final Collection<Problem> problem,
            @Nonnull final Collection<Problem>... problems
    ) {
        return new RetVal<>(Ret.joinProblemSets(problem, problems));
    }

    /**
     * Create a new RetVal instance that has errors.  This is only valid if at least one
     * problem exists within all the arguments.
     *
     * <p>Normally, you would use this in situations where you collect several validations
     * together, when you know at least one of them has a problem, if not more.
     *
     * @param problem the first problem container.
     * @param problems optional list of other problem containers to include in this value.
     * @param <T> type of the value
     * @return an error RetVal.
     * @throws IllegalArgumentException if no problems exist within the arguments.
     */
    @Nonnull
    public static <T> RetVal<T> fromProblems(
            @Nonnull final ProblemContainer problem, @Nonnull final ProblemContainer... problems
    ) {
        // ProblemContainer instances include Ret* objects, which can contain values without
        // problems.  However, this form of the constructor requires at least one problem, and
        // the standard use case is for returning a known bad state, so any values are
        // considered to be okay to lose.
        return new RetVal<>(Ret.joinRetProblems(problem, problems));
    }

    /**
     * Create a new RetVal instance that has errors.  This is only valid if at least one
     * problem exists within all the arguments.
     *
     * <p>Normally, you would use this in situations where you collect several validations
     * together, when you know at least one of them has a problem, if not more.
     *
     * @param problem the first problem container.
     * @param problems optional list of other problem containers to include in this value.
     * @param <T> type of the value
     * @return an error RetVal.
     * @throws IllegalArgumentException if no problems exist within the arguments.
     */
    @SafeVarargs
    @Nonnull
    public static <T> RetVal<T> fromProblems(
            @Nonnull final Collection<ProblemContainer> problem,
            @Nonnull final Collection<ProblemContainer>... problems
    ) {
        return new RetVal<>(Ret.joinRetProblemSets(problem, problems));
    }

    // made package-protected to allow other classes in this package to pass in known
    // non-null, non-empty, immutable problem lists.
    RetVal(@Nonnull final List<Problem> problems) {
        // an empty problem list is the marker for an error, so it can't accept a
        // problem state with no problems.
        if (problems.isEmpty()) {
            throw new IllegalArgumentException("no problems defined");
        }
        this.problems = problems;
        // The observable listeners are not passed to constructors.  This allows the developer
        // to know which specific place caused the value to be lost, not where it originated from.
        this.listener = ObservedMonitor.getCheckedInstance().registerInstance(this);
        this.value = null;
    }

    private RetVal(@Nonnull final T value) {
        this.problems = Collections.emptyList();
        // The observable listeners are not passed to constructors.  This allows the developer
        // to know which specific place caused the value to be lost, not where it originated from.
        this.listener = ObservedMonitor.getCheckedInstance().registerInstance(this);
        this.value = value;
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
        return this.value;
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
        return Optional.ofNullable(this.value);
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

        Ret.enforceNoProblems(this.problems);
        // Based on the constructor, by having no problems, the value must be non-null.
        return this.value;
    }

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
    public <V> RetVal<V> forwardProblems() {
        Ret.enforceHasProblems(this.problems);

        // There are circumstances where returning the exact same object isn't the right
        // operation, because the returned value should require a separate check, and any call
        // into this function usually means the caller already performed a check.
        // If traces are enabled, then the memory efficient form can't be used.
        if (ObservedMonitor.getCheckedInstance().isTraceEnabled()) {
            // This object will go out of scope, but the problems are returned, so mark it
            // as checked.
            this.listener.onObserved();

            // However, we can still reuse the read-only problem list.  So at least there's that.
            return new RetVal<>(this.problems);
        }

        // Else, use the super memory efficient way.
        @SuppressWarnings("unchecked")
        final RetVal<V> t = (RetVal<V>) this;
        return t;
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
        Ret.enforceHasProblems(this.problems);
        // pass the check to the returned value
        this.listener.onObserved();
        // memory efficient access.
        return new RetNullable<>(this.problems);
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
        return thenWrapped(
                () -> RetNullable.ok(this.value),
                // problem condition will pass the check to a new object
                true,
                () -> new RetNullable<>(this.problems));
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
    public RetVal<T> thenValidate(
            @Nonnull final NonnullParamFunction<T, ProblemContainer> checker
    ) {
        // This does not count as a validity check, so don't run the listener.
        if (! this.problems.isEmpty()) {
            return this;
        }
        final ProblemContainer discovered = checker.apply(this.value);
        if (discovered != null && discovered.hasProblems()) {
            // Move the checking to the returned value.
            this.listener.onObserved();
            return RetVal.fromProblems(this, discovered);
        }
        return this;
    }

    /**
     * Return a non-null {@link RetVal} value using a function that itself returns a {@link RetVal},
     * taking the current non-nullable value as an argument.
     * The function is called only if this object has no error.  If it has an error, then
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
    public <R> RetVal<R> then(@Nonnull final NonnullFunction<T, RetVal<R>> func) {
        return thenWrapped(
                () -> func.apply(this.value),
                // forwardProblems hsa its own check semantics
                false,
                this::forwardProblems
        );
    }

    /**
     * Return a non-null {@link RetVal} value, using a function that returns a non-null value,
     * taking the current non-null value as an argument.
     * The function is called only if this object has no problem.  If it has an error, then
     * the current list of problems is returned as the new type.
     *
     * <p>This is similar to {@link #then(NonnullFunction)}, but logically
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
    public <R> RetVal<R> map(@Nonnull final NonnullFunction<T, R> func) {
        return thenWrapped(
                () -> ok(func.apply(this.value)),
                // forwardProblems has its own check semantics
                false,
                this::forwardProblems
        );
    }

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
     * @return the error of the current value, if it is an error, or the object returned by
     *     the supplier.
     */
    @Nonnull
    public <R> RetNullable<R> thenNullable(
            @Nonnull final NonnullFunction<T, RetNullable<R>> func
    ) {
        return thenWrapped(
                () -> func.apply(this.value),
                // use a forwarder with its own check semantics
                false,
                this::forwardNullableProblems
        );
    }

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
    public <R> RetNullable<R> mapNullable(@Nonnull final NonnullParamFunction<T, R> func) {
        return thenWrapped(
                () -> RetNullable.ok(func.apply(this.value)),
                // problem forwarder has its own checking
                false,
                this::forwardNullableProblems
        );
    }

    /**
     * Run the parameter, only if this instance has no problems.
     *
     * @param runner the runnable to execute if no problems exist
     * @return this instance
     */
    @Nonnull
    public RetVal<T> thenRun(@Nonnull final Runnable runner) {
        // thenWrapped performs a "onChecked" call, and this method
        // returns "this", so this cannot use the wrapped helper.
        // By returning "this", it means that the "checked" call can only be done
        // if this function is considered performing a check, which this isn't.
        if (this.problems.isEmpty()) {
            runner.run();
        }
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
    public RetVal<T> thenRun(@Nonnull final NonnullConsumer<T> consumer) {
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
    public RetVoid thenVoid(@Nonnull final NonnullConsumer<T> consumer) {
        return thenWrapped(
                () -> {
                    consumer.accept(this.value);
                    return RetVoid.ok();
                },
                // problem condition will pass the check to a new object
                true,
                () -> new RetVoid(this.problems));
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
        return thenWrapped(
                () -> func.apply(this.value),
                // problem condition will pass the check to a new object
                true,
                () -> new RetVoid(this.problems)
        );
    }

    private <R> R thenWrapped(
            @Nonnull final NonnullSupplier<R> supplier,
            final boolean problemIsCheck,
            @Nonnull final NonnullSupplier<R> problemFactory
    ) {
        if (! this.problems.isEmpty()) {
            if (problemIsCheck) {
                this.listener.onObserved();
            }
            return problemFactory.get();
        }

        // The supplier will always return a new object and, and this method must pass the
        // checking on to it.
        this.listener.onObserved();
        return supplier.get();
    }

    @Override
    public boolean hasProblems() {
        // This alone does not make a check.  The problems themselves must be extracted or
        // forwarded.  However, because the result call also does not count as a check,
        // this will count as the check only if there are no problems.
        if (this.problems.isEmpty()) {
            this.listener.onObserved();
            return false;
        }
        return true;
    }

    @Override
    public boolean isProblem() {
        return hasProblems();
    }

    @Override
    public boolean isOk() {
        // This alone does not make a check.  The problems themselves must be extracted or
        // forwarded.  However, because the result call also does not count as a check,
        // this will count as the check only if there are no problems.
        if (this.problems.isEmpty()) {
            this.listener.onObserved();
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public Collection<Problem> anyProblems() {
        // This only counts as a check if there actually are problems. Generally, this
        // combines the problems in this instance with a larger collection, which can
        // itself be used to check if any of the values had problems.
        if (! this.problems.isEmpty()) {
            this.listener.onObserved();
        }
        return this.problems;
    }

    @Nonnull
    @Override
    public Collection<Problem> validProblems() {
        // Just like result() doesn't trigger an observation, so this one doesn't either.
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
        return "RetVal("
                + (this.problems.isEmpty()
                    ? ("value: " + this.value)
                    : (this.problems.size() + " problems: " + debugProblems("; "))
                ) + ")";
    }
}
