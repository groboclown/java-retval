// Released under the MIT License.
package net.groboclown.retval.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.groboclown.retval.Problem;
import net.groboclown.retval.ProblemContainer;
import net.groboclown.retval.Ret;
import net.groboclown.retval.RetNullable;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.RetVoid;
import net.groboclown.retval.function.NonnullConsumer;
import net.groboclown.retval.function.NonnullFunction;
import net.groboclown.retval.function.NonnullParamFunction;
import net.groboclown.retval.function.NonnullReturnFunction;
import net.groboclown.retval.function.NonnullSupplier;
import net.groboclown.retval.monitor.ObservedMonitor;
import net.groboclown.retval.monitor.ObservedMonitorRegistrar;

/**
 * Intended to be a version of the Ret* classes, with no error and a value.
 *
 * @param <T> type of the contained value.
 */
public class MonitoredReturnValue<T> implements RetVal<T>, RetNullable<T>, RetVoid {
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

    MonitoredReturnValue(@Nullable final T value) {
        // The observable listeners are not passed to constructors.  This allows the developer
        // to know which specific place caused the value to be lost, not where it originated from.
        this.listener = ObservedMonitorRegistrar.registerCheckedInstance(this);
        this.value = value;
    }

    @Override
    @Nullable
    public T getValue() {
        // This does not indicate that a check was made.  An implicit one can be made by the
        // caller, but this method does not mark it as such, because all problem values are lost.
        return this.value;
    }

    @Override
    @Nonnull
    public Optional<T> asOptional() {
        // This does not indicate a check was made.  An implicit one can be made by the
        // caller, but this method does not mark it as such, because all problem values are lost.
        return Optional.ofNullable(this.value);
    }

    // @Nullable inherited from RetNullable, @Nonnull inherited from RetValue
    @SuppressWarnings("NullableProblems")
    @Override
    public T result() {
        // This does not indicate a check.  If this is marked as a check and the enforcement
        // passes, then that can disguise scenarios where testing doesn't encounter an error,
        // then in production, when an error is found, this fails.  The developer should be
        // notified during development about the incorrect usage.

        return this.value;
    }

    @Nonnull
    @Override
    public Optional<T> requireOptional() {
        return Optional.ofNullable(this.value);
    }

    @Override
    @Nonnull
    public <V> MonitoredReturnValue<V> forwardProblems() {
        // Prevent the duplicate errors by marking this as an observation.
        this.listener.onObserved();
        throw new IllegalStateException("contains no problems");
    }

    @Override
    @Nonnull
    public <V> RetNullable<V> forwardNullableProblems() {
        // Prevent the duplicate errors by marking this as an observation.
        this.listener.onObserved();
        throw new IllegalStateException("contains no problems");
    }

    @Override
    @Nonnull
    public RetVoid forwardVoidProblems() {
        // Prevent the duplicate errors by marking this as an observation.
        this.listener.onObserved();
        throw new IllegalStateException("contains no problems");
    }

    @Nonnull
    @Override
    public RetVal<T> thenValidate(
            @Nonnull final NonnullParamFunction<T, ProblemContainer> checker
    ) {
        final ProblemContainer problems = checker.apply(this.value);
        if (problems != null) {
            // Behind-the-scenes optimization.
            if (problems instanceof MonitoredReturnProblem) {
                // Forward on the problems, which means mark this as observed.
                this.listener.onObserved();
                @SuppressWarnings("unchecked")
                final RetVal<T> ret =
                        ((MonitoredReturnProblem<T>) problems).forwardProblems();
                return ret;
            }
            if (problems.hasProblems()) {
                // Forward on the problems, which means mark this as observed.
                this.listener.onObserved();
                // Note the call to get valid problems.  For places where the container
                // is a Ret* value, that will trigger an observation.
                return RetGenerator.valFromProblem(problems.validProblems());
            }
        }
        return this;
    }

    @Nonnull
    @Override
    public RetNullable<T> thenValidate(@Nonnull final Function<T, ProblemContainer> checker) {
        final ProblemContainer problems = checker.apply(this.value);
        if (problems != null) {
            // Behind-the-scenes optimization.
            if (problems instanceof MonitoredReturnProblem) {
                // Forward on the problems, which means mark this as observed.
                this.listener.onObserved();
                @SuppressWarnings("unchecked")
                final RetNullable<T> ret =
                        ((MonitoredReturnProblem<T>) problems).forwardNullableProblems();
                return ret;
            }
            if (problems.hasProblems()) {
                // Forward on the problems, which means mark this as observed.
                this.listener.onObserved();
                // Note the call to get valid problems.  For places where the container
                // is a Ret* value, that will trigger an observation.
                return RetGenerator.nullableFromProblem(problems.validProblems());
            }
        }
        return this;
    }

    @Nonnull
    @Override
    public RetNullable<T> thenRunNullable(@Nonnull final Runnable runner) {
        runner.run();
        return this;
    }

    @Nonnull
    @Override
    public RetNullable<T> thenRunNullable(@Nonnull final Consumer<T> consumer) {
        consumer.accept(this.value);
        return this;
    }

    @Nonnull
    @Override
    public RetNullable<T> asNullable() {
        // This should perform a null value check, but it *should* only be called from a
        // RetVoid value.
        return this;
    }

    @Nonnull
    @Override
    public <R> RetVal<R> then(@Nonnull final NonnullFunction<T, RetVal<R>> func) {
        this.listener.onObserved();
        return func.apply(this.value);
    }

    @Nonnull
    @Override
    public <R> RetVal<R> then(@Nonnull final NonnullReturnFunction<T, RetVal<R>> func) {
        this.listener.onObserved();
        return func.apply(this.value);
    }

    @Nonnull
    @Override
    public <R> RetVal<R> then(@Nonnull final NonnullSupplier<RetVal<R>> supplier) {
        this.listener.onObserved();
        return supplier.get();
    }

    @Nonnull
    @Override
    public <R> RetVal<R> map(@Nonnull final NonnullSupplier<R> supplier) {
        this.listener.onObserved();
        return RetGenerator.valOk(supplier.get());
    }

    @Nonnull
    @Override
    public <R> RetVal<R> map(@Nonnull final NonnullReturnFunction<T, R> func) {
        this.listener.onObserved();
        return RetGenerator.valOk(func.apply(this.value));
    }

    @Override
    @Nonnull
    public <R> RetVal<R> map(@Nonnull final NonnullFunction<T, R> func) {
        this.listener.onObserved();
        return RetGenerator.valOk(func.apply(this.value));
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> thenNullable(
            @Nonnull final NonnullReturnFunction<T, RetNullable<R>> func) {
        this.listener.onObserved();
        return func.apply(this.value);
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> thenNullable(
            @Nonnull final NonnullFunction<T, RetNullable<R>> func
    ) {
        this.listener.onObserved();
        return func.apply(this.value);
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> thenNullable(
            @Nonnull final NonnullSupplier<RetNullable<R>> supplier) {
        this.listener.onObserved();
        return supplier.get();
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> mapNullable(@Nonnull final NonnullParamFunction<T, R> func) {
        this.listener.onObserved();
        return RetGenerator.nullableOk(func.apply(this.value));
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> mapNullable(@Nonnull final Function<T, R> func) {
        this.listener.onObserved();
        return RetGenerator.nullableOk(func.apply(this.value));
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> mapNullable(@Nonnull final Supplier<R> supplier) {
        this.listener.onObserved();
        return RetGenerator.nullableOk(supplier.get());
    }

    @Nonnull
    @Override
    public MonitoredReturnValue<T> thenRun(@Nonnull final Runnable runner) {
        // thenWrapped performs a "onChecked" call, and this method
        // returns "this", so this cannot use the wrapped helper.
        // By returning "this", it means that the "checked" call can only be done
        // if this function is considered performing a check, which this isn't.
        runner.run();
        return this;
    }

    @Nonnull
    @Override
    public MonitoredReturnValue<T> thenRun(@Nonnull final NonnullConsumer<T> consumer) {
        // thenWrapped performs a "onChecked" call, and this method
        // returns "this", so this cannot use the wrapped helper.
        // By returning "this", it means that the "checked" call can only be done
        // if this function is considered performing a check, which this isn't.
        consumer.accept(this.value);
        return this;
    }

    @Nonnull
    @Override
    public RetVoid thenVoid(@Nonnull final NonnullSupplier<RetVoid> supplier) {
        this.listener.onObserved();
        return supplier.get();
    }

    @Nonnull
    @Override
    public RetVoid thenVoid(@Nonnull final Consumer<T> consumer) {
        consumer.accept(this.value);
        return this;
    }

    @Nonnull
    @Override
    public RetVoid thenVoid(@Nonnull final NonnullReturnFunction<T, RetVoid> func) {
        this.listener.onObserved();
        return func.apply(this.value);
    }

    @Nonnull
    @Override
    public RetVoid thenVoid(@Nonnull final NonnullConsumer<T> consumer) {
        // Because the returned object is supposed to be value-free, and we know it
        // has no problems, this same object can be returned.  This may introduce an
        // unexpected hold in memory on the value in the returned object, though.
        // The intended use case is for the returned value to have a relatively short
        // life cycle.
        // Some applications may see this as a memory leak, though.

        // This, though, has an unexpected side effect where the RetVoid has ever-so slightly
        // meanings behind the relationships on calls and observations.
        consumer.accept(this.value);
        return this;
    }

    @Nonnull
    @Override
    public RetVoid thenVoid(@Nonnull final NonnullFunction<T, RetVoid> func) {
        this.listener.onObserved();
        return func.apply(this.value);
    }

    @Nonnull
    @Override
    public RetVoid consume(@Nonnull final Consumer<T> consumer) {
        // This does not count as an observation, because there was no check whether
        // the value was ok or not.  There might be internal logic inside the consumer
        // to state that it's okay, but that's breaking the usage pattern for the api.
        consumer.accept(this.value);
        return this;
    }

    @Nonnull
    @Override
    public RetVoid consume(@Nonnull final NonnullConsumer<T> consumer) {
        // This does not count as an observation, because there was no check whether
        // the value was ok or not.  There might be internal logic inside the consumer
        // to state that it's okay, but that's breaking the usage pattern for the api.
        consumer.accept(this.value);
        return this;
    }

    @Nonnull
    @Override
    public RetVoid produceVoid(@Nonnull final NonnullReturnFunction<T, RetVoid> func) {
        // Passing the observation ball to the new returned value.
        this.listener.onObserved();
        return func.apply(this.value);
    }

    @Nonnull
    @Override
    public RetVoid produceVoid(@Nonnull final NonnullFunction<T, RetVoid> func) {
        // Passing the observation ball to the new returned value.
        this.listener.onObserved();
        return func.apply(this.value);
    }

    @Nonnull
    @Override
    public RetVal<T> requireNonNull(
            @Nonnull final Problem problem,
            @Nonnull final Problem... problems) {
        if (this.value == null) {
            // Pass the observation ball to the returned value.
            this.listener.onObserved();
            return RetGenerator.valFromProblem(Ret.joinProblems(problem, problems));
        }
        return this;
    }

    @Nonnull
    @Override
    public RetVal<T> defaultAs(@Nonnull T defaultValue) {
        if (this.value == null) {
            // Pass the observation ball to the returned value.
            this.listener.onObserved();
            return RetGenerator.valOk(defaultValue);
        }
        return this;
    }

    @Nonnull
    @Override
    public RetVoid consumeIfNonnull(@Nonnull final NonnullConsumer<T> consumer) {
        if (this.value != null) {
            consumer.accept(this.value);
        }
        return this;
    }

    @Nonnull
    @Override
    public RetVoid produceVoidIfNonnull(@Nonnull final NonnullFunction<T, RetVoid> func) {
        if (this.value != null) {
            // Pass the observation ball to the returned value.
            this.listener.onObserved();
            return func.apply(this.value);
        }
        return this;
    }

    @Nonnull
    @Override
    public <R> RetVal<R> defaultOrMap(
            @Nonnull final R defaultValue,
            @Nonnull final NonnullFunction<T, R> func) {
        // Pass the observation ball to the returned value.
        this.listener.onObserved();
        if (this.value == null) {
            return RetGenerator.valOk(defaultValue);
        }
        return RetGenerator.valOk(func.apply(this.value));
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> nullOrMap(
            @Nonnull final NonnullParamFunction<T, R> func) {
        if (this.value == null) {
            //noinspection unchecked
            return (RetNullable<R>) this;
        }
        // Pass the observation ball to the returned value.
        this.listener.onObserved();
        return RetGenerator.nullableOk(func.apply(this.value));
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> nullOrThenNullable(
            @Nonnull final NonnullFunction<T, RetNullable<R>> func) {
        if (this.value == null) {
            //noinspection unchecked
            return (RetNullable<R>) this;
        }
        // Pass the observation ball to the returned value.
        this.listener.onObserved();
        return func.apply(this.value);
    }

    @Override
    public boolean hasProblems() {
        // This alone does not make a check.  The problems themselves must be extracted or
        // forwarded.  However, because the result call also does not count as a check,
        // this will count as the check only if there are no problems.
        this.listener.onObserved();
        return false;
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
        this.listener.onObserved();
        return true;
    }

    @Nonnull
    @Override
    public Collection<Problem> anyProblems() {
        // This only counts as a check if there actually are problems. Generally, this
        // combines the problems in this instance with a larger collection, which can
        // itself be used to check if any of the values had problems.
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public Collection<Problem> validProblems() {
        // validProblems will always mark the value as observed.  In the case
        // where no problems exist, a programmer exception is raised, and forcing
        // the observation prevents a double error.
        this.listener.onObserved();
        throw new IllegalStateException("contains no problems");
    }

    @Nonnull
    @Override
    public String debugProblems(@Nonnull final String joinedWith) {
        return "";
    }

    @Override
    public void joinProblemsWith(@Nonnull final Collection<Problem> problemList) {
        // This acts as closing off this value and passing the problem state to the
        // list.
        this.listener.onObserved();
    }

    @Override
    public String toString() {
        return "Ret(value: " + this.value + ")";
    }
}
