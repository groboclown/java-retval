// Released under the MIT License.
package net.groboclown.retval.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
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

/**
 * Intended to be a version of the Ret* classes, with no error and a value, and has no monitor.
 * This does not implement {@link RetVoid}, as it has a special no-state version.
 *
 * @param <T> type of the contained value.
 */
public class SimpleReturnValue<T> implements RetVal<T>, RetNullable<T> {
    private final T value;

    SimpleReturnValue(@Nullable final T value) {
        this.value = value;
    }

    @Override
    @Nullable
    public T getValue() {
        return this.value;
    }

    @Override
    @Nonnull
    public Optional<T> asOptional() {
        return Optional.ofNullable(this.value);
    }

    // @Nullable inherited from RetNullable, @Nonnull inherited from RetValue
    @SuppressWarnings("NullableProblems")
    @Override
    public T result() {
        //noinspection ConstantConditions
        return this.value;
    }

    @Nonnull
    @Override
    public Optional<T> requireOptional() {
        return Optional.ofNullable(this.value);
    }

    @Override
    @Nonnull
    public <V> SimpleReturnValue<V> forwardProblems() {
        throw new IllegalStateException("contains no problems");
    }

    @Override
    @Nonnull
    public <V> RetNullable<V> forwardNullableProblems() {
        throw new IllegalStateException("contains no problems");
    }

    @Override
    @Nonnull
    public RetVoid forwardVoidProblems() {
        throw new IllegalStateException("contains no problems");
    }

    @Nonnull
    @Override
    public RetVal<T> thenValidate(
            @Nonnull final NonnullParamFunction<T, ProblemContainer> checker
    ) {
        //noinspection ConstantConditions
        final ProblemContainer problems = checker.apply(this.value);
        if (problems != null) {
            // Behind-the-scenes optimization.
            if (problems instanceof SimpleReturnProblem) {
                @SuppressWarnings("unchecked") final RetVal<T> ret =
                        ((SimpleReturnProblem<T>) problems).forwardProblems();
                return ret;
            }
            if (problems.hasProblems()) {
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
            if (problems instanceof SimpleReturnProblem) {
                @SuppressWarnings("unchecked") final RetNullable<T> ret =
                        ((SimpleReturnProblem<T>) problems).forwardNullableProblems();
                return ret;
            }
            if (problems.hasProblems()) {
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
        //noinspection ConstantConditions
        return func.apply(this.value);
    }

    @Nonnull
    @Override
    public <R> RetVal<R> then(@Nonnull final NonnullReturnFunction<T, RetVal<R>> func) {
        return func.apply(this.value);
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> thenNullable(
            @Nonnull final NonnullReturnFunction<T, RetNullable<R>> func) {
        return func.apply(this.value);
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> thenNullable(
            @Nonnull final NonnullFunction<T, RetNullable<R>> func
    ) {
        //noinspection ConstantConditions
        return func.apply(this.value);
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> mapNullable(@Nonnull final NonnullParamFunction<T, R> func) {
        //noinspection ConstantConditions
        return RetGenerator.nullableOk(func.apply(this.value));
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> mapNullable(@Nonnull final Function<T, R> func) {
        return RetGenerator.nullableOk(func.apply(this.value));
    }

    @Override
    @Nonnull
    public <R> RetVal<R> map(@Nonnull final NonnullFunction<T, R> func) {
        //noinspection ConstantConditions
        return RetGenerator.valOk(func.apply(this.value));
    }

    @Nonnull
    @Override
    public <R> RetVal<R> map(@Nonnull final NonnullReturnFunction<T, R> func) {
        return RetGenerator.valOk(func.apply(this.value));
    }

    @Nonnull
    @Override
    public RetVoid thenVoid(@Nonnull final NonnullReturnFunction<T, RetVoid> func) {
        return func.apply(this.value);
    }

    @Nonnull
    @Override
    public RetVoid thenVoid(@Nonnull final NonnullConsumer<T> consumer) {
        //noinspection ConstantConditions
        consumer.accept(this.value);
        return RetGenerator.voidOk();
    }

    @Nonnull
    @Override
    public RetVoid thenVoid(@Nonnull final NonnullFunction<T, RetVoid> func) {
        //noinspection ConstantConditions
        return func.apply(this.value);
    }

    @Nonnull
    @Override
    public RetVoid thenVoid(@Nonnull final Consumer<T> consumer) {
        consumer.accept(this.value);
        return RetGenerator.voidOk();
    }

    @Nonnull
    @Override
    public RetVoid consume(@Nonnull final NonnullConsumer<T> consumer) {
        //noinspection ConstantConditions
        consumer.accept(this.value);
        return RetGenerator.voidOk();
    }

    @Nonnull
    @Override
    public RetVoid consume(@Nonnull final Consumer<T> consumer) {
        consumer.accept(this.value);
        return RetGenerator.voidOk();
    }

    @Nonnull
    @Override
    public RetVoid produceVoid(@Nonnull final NonnullReturnFunction<T, RetVoid> func) {
        return func.apply(this.value);
    }

    @Nonnull
    @Override
    public RetVoid produceVoid(@Nonnull final NonnullFunction<T, RetVoid> func) {
        //noinspection ConstantConditions
        return func.apply(this.value);
    }

    @Nonnull
    @Override
    public SimpleReturnValue<T> thenRun(@Nonnull final Runnable runner) {
        runner.run();
        return this;
    }

    @Nonnull
    @Override
    public SimpleReturnValue<T> thenRun(@Nonnull final NonnullConsumer<T> consumer) {
        //noinspection ConstantConditions
        consumer.accept(this.value);
        return this;
    }

    @Nonnull
    @Override
    public RetVal<T> requireNonNull(
            @Nonnull final Problem problem,
            @Nonnull final Problem... problems) {
        if (this.value == null) {
            return RetGenerator.valFromProblem(Ret.joinProblems(problem, problems));
        }
        return this;
    }

    @Nonnull
    @Override
    public RetVal<T> defaultAs(@Nonnull T defaultValue) {
        if (this.value == null) {
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
        return RetGenerator.voidOk();
    }

    @Nonnull
    @Override
    public RetVoid produceVoidIfNonnull(@Nonnull final NonnullFunction<T, RetVoid> func) {
        if (this.value != null) {
            return func.apply(this.value);
        }
        return RetGenerator.voidOk();
    }

    @Nonnull
    @Override
    public <R> RetVal<R> defaultOrMap(
            @Nonnull final R defaultValue,
            @Nonnull final NonnullFunction<T, R> func) {
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
        return func.apply(this.value);
    }

    @Override
    public boolean hasProblems() {
        return false;
    }

    @Override
    public boolean isProblem() {
        return hasProblems();
    }

    @Override
    public boolean isOk() {
        return true;
    }

    @Nonnull
    @Override
    public Collection<Problem> anyProblems() {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public Collection<Problem> validProblems() {
        throw new IllegalStateException("contains no problems");
    }

    @Nonnull
    @Override
    public String debugProblems(@Nonnull final String joinedWith) {
        return "";
    }

    @Override
    public void joinProblemsWith(@Nonnull final Collection<Problem> problemList) {
        // No-op
    }

    @Override
    public String toString() {
        return "Ret(value: " + this.value + ")";
    }
}
