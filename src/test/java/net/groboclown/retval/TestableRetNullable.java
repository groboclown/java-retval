// Released under the MIT License.
package net.groboclown.retval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.groboclown.retval.function.NonnullReturnFunction;


// A dumb version to test default methods.
class TestableRetNullable<T> implements RetNullable<T> {
    public final List<Problem> problems = new ArrayList<>();
    public T value;

    @Override
    public boolean isProblem() {
        return !problems.isEmpty();
    }

    @Override
    public boolean hasProblems() {
        return !problems.isEmpty();
    }

    @Override
    public boolean isOk() {
        return problems.isEmpty();
    }

    @Nonnull
    @Override
    public Collection<Problem> anyProblems() {
        return problems;
    }

    @Nonnull
    @Override
    public Collection<Problem> validProblems() {
        Ret.enforceHasProblems(this.problems);
        return this.problems;
    }

    @Nonnull
    @Override
    public String debugProblems(@Nonnull String joinedWith) {
        return Ret.joinProblemMessages(joinedWith, this.problems);
    }

    @Override
    public void joinProblemsWith(@Nonnull Collection<Problem> problemList) {
        problemList.addAll(this.problems);
    }

    @Nullable
    @Override
    public T getValue() {
        return this.value;
    }

    @Nonnull
    @Override
    public Optional<T> asOptional() {
        return Optional.ofNullable(this.value);
    }

    @Nullable
    @Override
    public T result() {
        Ret.enforceNoProblems(this.problems);
        return this.value;
    }

    @Nonnull
    @Override
    public Optional<T> requireOptional() {
        Ret.enforceNoProblems(this.problems);
        return Optional.empty();
    }

    @Nonnull
    @Override
    public <V> RetVal<V> forwardProblems() {
        return RetVal.fromProblems(this);
    }

    @Nonnull
    @Override
    public <V> RetNullable<V> forwardNullableProblems() {
        return RetNullable.fromProblems(this);
    }

    @Nonnull
    @Override
    public RetVoid forwardVoidProblems() {
        return RetVoid.fromProblems(this);
    }

    @Nonnull
    @Override
    public RetNullable<T> thenValidate(@Nonnull Function<T, ProblemContainer> checker) {
        if (hasProblems()) {
            return forwardNullableProblems();
        }
        final ProblemContainer found = checker.apply(this.value);
        if (found.hasProblems()) {
            return RetNullable.fromProblems(found);
        }
        return this;
    }

    @Nonnull
    @Override
    public <R> RetVal<R> then(@Nonnull NonnullReturnFunction<T, RetVal<R>> func) {
        if (hasProblems()) {
            return forwardProblems();
        }
        return func.apply(this.value);
    }

    @Nonnull
    @Override
    public <R> RetVal<R> map(@Nonnull NonnullReturnFunction<T, R> func) {
        if (hasProblems()) {
            return forwardProblems();
        }
        return RetVal.ok(func.apply(this.value));
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> thenNullable(@Nonnull NonnullReturnFunction<T, RetNullable<R>> func) {
        if (hasProblems()) {
            return forwardNullableProblems();
        }
        return func.apply(this.value);
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> mapNullable(@Nonnull Function<T, R> func) {
        if (hasProblems()) {
            return forwardNullableProblems();
        }
        return RetNullable.ok(func.apply(this.value));
    }

    @Nonnull
    @Override
    public RetNullable<T> thenRunNullable(@Nonnull Runnable runner) {
        if (isOk()) {
            runner.run();
        }
        return this;
    }

    @Nonnull
    @Override
    public RetNullable<T> thenRunNullable(@Nonnull Consumer<T> consumer) {
        if (isOk()) {
            consumer.accept(this.value);
        }
        return this;
    }

    @Nonnull
    @Override
    public RetVoid thenVoid(@Nonnull Consumer<T> consumer) {
        if (hasProblems()) {
            return forwardVoidProblems();
        }
        consumer.accept(this.value);
        return RetVoid.ok();
    }

    @Nonnull
    @Override
    public RetVoid thenVoid(@Nonnull NonnullReturnFunction<T, RetVoid> func) {
        if (hasProblems()) {
            return forwardVoidProblems();
        }
        return func.apply(this.value);
    }

    @Nonnull
    @Override
    public RetVoid consume(@Nonnull Consumer<T> consumer) {
        if (hasProblems()) {
            return forwardVoidProblems();
        }
        consumer.accept(this.value);
        return RetVoid.ok();
    }

    @Nonnull
    @Override
    public RetVoid produceVoid(@Nonnull NonnullReturnFunction<T, RetVoid> func) {
        if (hasProblems()) {
            return forwardVoidProblems();
        }
        return func.apply(this.value);
    }
}
