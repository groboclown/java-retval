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

/**
 * A shared implementation of the Ret* interfaces, which only contains problems and is not
 * monitored.
 *
 * @param <T> type of the referenced value.
 */
public class SimpleReturnProblem<T> implements RetVal<T>, RetNullable<T>, RetVoid {
    private final List<Problem> problems;

    // made package-protected to allow other classes in this package to pass in known
    // non-null, non-empty, immutable problem lists.
    SimpleReturnProblem(@Nonnull final List<Problem> problems) {
        // an empty problem list is the marker for an error, so it can't accept a
        // problem state with no problems.
        if (problems.isEmpty()) {
            throw new IllegalArgumentException("no problems defined");
        }
        this.problems = problems;
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
        throw new IllegalStateException("contains problems");
    }

    @Nonnull
    @Override
    public Optional<T> requireOptional() {
        throw new IllegalStateException("contains problems");
    }

    @Nonnull
    @Override
    public <V> SimpleReturnProblem<V> forwardProblems() {
        @SuppressWarnings("unchecked")
        final SimpleReturnProblem<V> t = (SimpleReturnProblem<V>) this;
        return t;
    }

    @Nonnull
    @Override
    public <V> RetNullable<V> forwardNullableProblems() {
        @SuppressWarnings("unchecked") final RetNullable<V> ret = (RetNullable<V>) this;
        return ret;
    }

    @Nonnull
    @Override
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
    public SimpleReturnProblem<T> thenValidate(
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
        @SuppressWarnings("unchecked") final RetNullable<R> ret = (RetNullable<R>) this;
        return ret;
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> thenNullable(
            @Nonnull final NonnullSupplier<RetNullable<R>> supplier) {
        @SuppressWarnings("unchecked") final RetNullable<R> ret = (RetNullable<R>) this;
        return ret;
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> thenNullable(
            @Nonnull final NonnullReturnFunction<T, RetNullable<R>> func
    ) {
        @SuppressWarnings("unchecked")
        final SimpleReturnProblem<R> ret = (SimpleReturnProblem<R>) this;
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
    public RetNullable<T> asNullable() {
        return this;
    }

    @Nonnull
    @Override
    public <R> RetVal<R> then(@Nonnull final NonnullFunction<T, RetVal<R>> func) {
        @SuppressWarnings("unchecked") final RetVal<R> ret = (RetVal<R>) this;
        return ret;
    }

    @Nonnull
    @Override
    public <R> RetVal<R> then(@Nonnull final NonnullReturnFunction<T, RetVal<R>> func) {
        @SuppressWarnings("unchecked")
        final SimpleReturnProblem<R> ret = (SimpleReturnProblem<R>) this;
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
    public <R> RetVal<R> map(@Nonnull final NonnullReturnFunction<T, R> func) {
        @SuppressWarnings("unchecked")
        final SimpleReturnProblem<R> ret = (SimpleReturnProblem<R>) this;
        return ret;
    }

    @Override
    @Nonnull
    public <R> RetVal<R> map(@Nonnull final NonnullFunction<T, R> func) {
        @SuppressWarnings("unchecked") final RetVal<R> ret = (RetVal<R>) this;
        return ret;
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> mapNullable(@Nonnull final Function<T, R> func) {
        @SuppressWarnings("unchecked")
        final SimpleReturnProblem<R> ret = (SimpleReturnProblem<R>) this;
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
    public <R> RetNullable<R> mapNullable(@Nonnull final NonnullParamFunction<T, R> func) {
        @SuppressWarnings("unchecked") final RetNullable<R> ret = (RetNullable<R>) this;
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
    public RetVoid consume(@Nonnull final NonnullConsumer<T> consumer) {
        return this;
    }

    @Nonnull
    @Override
    public RetVoid consume(@Nonnull final Consumer<T> consumer) {
        return this;
    }

    @Nonnull
    @Override
    public RetVoid produceVoid(@Nonnull final NonnullReturnFunction<T, RetVoid> func) {
        return this;
    }

    @Nonnull
    @Override
    public RetVoid produceVoid(@Nonnull final NonnullFunction<T, RetVoid> func) {
        return this;
    }

    @Nonnull
    @Override
    public SimpleReturnProblem<T> thenRun(@Nonnull final Runnable runner) {
        return this;
    }

    @Nonnull
    @Override
    public SimpleReturnProblem<T> thenRun(@Nonnull final NonnullConsumer<T> consumer) {
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
        return this.problems;
    }

    @Nonnull
    @Override
    public Collection<Problem> validProblems() {
        return this.problems;
    }

    @Nonnull
    @Override
    public String debugProblems(@Nonnull final String joinedWith) {
        return Ret.joinProblemMessages(joinedWith, this.problems);
    }

    @Override
    public void joinProblemsWith(@Nonnull final Collection<Problem> problemList) {
        problemList.addAll(this.problems);
    }

    @Nonnull
    @Override
    public RetVal<T> requireNonNull(
            @Nonnull final Problem problem,
            @Nonnull final Problem... problems) {
        return forwardProblems();
    }

    @Nonnull
    @Override
    public RetVal<T> defaultAs(@Nonnull T defaultValue) {
        return forwardProblems();
    }

    @Nonnull
    @Override
    public RetVoid consumeIfNonnull(@Nonnull final NonnullConsumer<T> consumer) {
        return forwardVoidProblems();
    }

    @Nonnull
    @Override
    public <R> RetVal<R> defaultOrMap(
            @Nonnull final R defaultValue,
            @Nonnull final NonnullFunction<T, R> func) {
        return forwardProblems();
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> nullOrMap(
            @Nonnull final NonnullParamFunction<T, R> func) {
        return forwardNullableProblems();
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> nullOrThenNullable(
            @Nonnull final NonnullFunction<T, RetNullable<R>> func) {
        return forwardNullableProblems();
    }

    @Override
    public String toString() {
        return "Ret(" + this.problems.size() + " problems: " + debugProblems("; ") + ")";
    }
}
