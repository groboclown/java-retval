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
        this.listener = ObservedMonitorRegistrar.registerCheckedInstance(this);
    }

    @Nullable
    @Override
    public T getValue() {
        // This does not indicate that a check was made.  An implicit one can be made by the
        // caller, but this method does not mark it as such, because all problem values are lost.
        return null;
    }

    @Nonnull
    @Override
    public Optional<T> asOptional() {
        // This does not indicate a check was made.  An implicit one can be made by the
        // caller, but this method does not mark it as such, because all problem values are lost.
        return Optional.empty();
    }

    @Nonnull
    @Override
    public T result() {
        // For problem scenarios, this counts as a check to prevent double error reporting.
        this.listener.onObserved();
        throw new IllegalStateException("contains problems");
    }

    @Nonnull
    @Override
    public Optional<T> requireOptional() {
        // For problem scenarios, this counts as a check to prevent double error reporting.
        this.listener.onObserved();
        throw new IllegalStateException("contains problems");
    }

    @Nonnull
    @Override
    public <V> MonitoredReturnProblem<V> forwardProblems() {
        @SuppressWarnings("unchecked")
        final MonitoredReturnProblem<V> t = (MonitoredReturnProblem<V>) this;
        return t;
    }

    @Nonnull
    @Override
    public <V> RetNullable<V> forwardNullableProblems() {
        @SuppressWarnings("unchecked")
        final RetNullable<V> ret = (RetNullable<V>) this;
        return ret;
    }

    @Nonnull
    @Override
    public RetVoid forwardVoidProblems() {
        return this;
    }

    @Nonnull
    @Override
    public RetNullable<T> asNullable() {
        return this;
    }

    @Nonnull
    @Override
    public RetNullable<T> thenValidate(@Nonnull final Function<T, ProblemContainer> checker) {
        return this;
    }

    @Nonnull
    @Override
    public MonitoredReturnProblem<T> thenValidate(
            @Nonnull final NonnullParamFunction<T, ProblemContainer> checker
    ) {
        // This does not count as a validity check, so don't run the listener.
        return this;
    }

    @Override
    @Nonnull
    public <R> RetNullable<R> thenNullable(
            @Nonnull final NonnullFunction<T, RetNullable<R>> func
    ) {
        @SuppressWarnings("unchecked")
        final RetNullable<R> ret = (RetNullable<R>) this;
        return ret;
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> thenNullable(
            @Nonnull final NonnullSupplier<RetNullable<R>> supplier
    ) {
        @SuppressWarnings("unchecked")
        final RetNullable<R> ret = (RetNullable<R>) this;
        return ret;
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> thenNullable(
            @Nonnull final NonnullReturnFunction<T, RetNullable<R>> func
    ) {
        @SuppressWarnings("unchecked")
        final MonitoredReturnProblem<R> ret = (MonitoredReturnProblem<R>) this;
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
    public <R> RetNullable<R> mapNullable(@Nonnull final Supplier<R> supplier) {
        @SuppressWarnings("unchecked")
        final RetNullable<R> ret = (RetNullable<R>) this;
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
    public <R> RetNullable<R> mapNullable(@Nonnull final Function<T, R> func) {
        @SuppressWarnings("unchecked")
        final MonitoredReturnProblem<R> ret = (MonitoredReturnProblem<R>) this;
        return ret;
    }

    @Nonnull
    @Override
    public <R> RetVal<R> then(@Nonnull final NonnullSupplier<RetVal<R>> supplier) {
        @SuppressWarnings("unchecked")
        final RetVal<R> ret = (RetVal<R>) this;
        return ret;
    }

    @Nonnull
    @Override
    public <R> RetVal<R> then(@Nonnull final NonnullFunction<T, RetVal<R>> func) {
        @SuppressWarnings("unchecked")
        final RetVal<R> ret = (RetVal<R>) this;
        return ret;
    }

    @Nonnull
    @Override
    public <R> RetVal<R> then(@Nonnull final NonnullReturnFunction<T, RetVal<R>> func) {
        @SuppressWarnings("unchecked")
        final MonitoredReturnProblem<R> ret = (MonitoredReturnProblem<R>) this;
        return ret;
    }

    @Nonnull
    @Override
    public <R> RetVal<R> map(@Nonnull final NonnullReturnFunction<T, R> func) {
        @SuppressWarnings("unchecked")
        final MonitoredReturnProblem<R> ret = (MonitoredReturnProblem<R>) this;
        return ret;
    }

    @Override
    @Nonnull
    public <R> RetVal<R> map(@Nonnull final NonnullFunction<T, R> func) {
        @SuppressWarnings("unchecked")
        final RetVal<R> ret = (RetVal<R>) this;
        return ret;
    }

    @Nonnull
    @Override
    public <R> RetVal<R> map(@Nonnull final NonnullSupplier<R> supplier) {
        @SuppressWarnings("unchecked")
        final RetVal<R> ret = (RetVal<R>) this;
        return ret;
    }

    @Nonnull
    @Override
    public RetVoid thenVoid(@Nonnull final NonnullSupplier<RetVoid> supplier) {
        return this;
    }

    @Nonnull
    @Override
    public RetVoid thenVoid(@Nonnull final NonnullConsumer<T> consumer) {
        return this;
    }

    @Nonnull
    @Override
    public RetVoid thenVoid(@Nonnull final NonnullFunction<T, RetVoid> func) {
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

    @Nonnull
    @Override
    public MonitoredReturnProblem<T> thenRun(@Nonnull final Runnable runner) {
        return this;
    }

    @Nonnull
    @Override
    public MonitoredReturnProblem<T> thenRun(@Nonnull final NonnullConsumer<T> consumer) {
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
        return "Ret(" + this.problems.size() + " problems: " + debugProblems("; ") + ")";
    }
}
