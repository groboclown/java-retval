// Released under the MIT License.
package net.groboclown.retval;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.groboclown.retval.function.NonnullReturnFunction;

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
    private final CheckMonitor.CheckableListener listener;
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

    // package-protected to allow for memory efficient problem passing.
    // Must be non-null, non-empty, and immutable.
    RetNullable(@Nonnull final List<Problem> problems) {
        if (problems.isEmpty()) {
            throw new IllegalArgumentException("no problems defined");
        }
        this.problems = problems;
        this.listener = CheckMonitor.getInstance().registerErrorInstance(this);
        this.value = null;
    }

    private RetNullable(@Nullable final T value) {
        this.problems = Collections.emptyList();
        this.listener = CheckMonitor.getInstance().registerErrorInstance(this);
        this.value = value;
    }


    @Nullable
    public T getValue() {
        // Not considered a check, so the check listener is not called
        return this.value;
    }

    @Nullable
    public T result() {
        // Not considered a check, so the check listener is not called
        Ret.enforceNoProblems(this.problems);
        return this.value;
    }

    @Nonnull
    public <V> RetNullable<V> forwardError() {
        throw new IllegalStateException();
    }

    @Nonnull
    public RetVoid forwardVoid() {
        throw new IllegalStateException();
    }

    @Nonnull
    public <V> RetVal<V> forwardNonnull() {
        throw new IllegalStateException();
    }

    @Nonnull
    public RetVal<T> asNonnull() {
        throw new IllegalStateException();
    }

    /**
     * Return a non-null value, generated by processing the current value.  If this
     * object has problems, then the problems are returned and the function is not run.
     *
     * @param func functional object that returns a RetVal and takes the current value as argument.
     * @param <V> return value type
     * @return value or problems
     */
    @Nonnull
    public <V> RetVal<V> then(@Nonnull final NonnullReturnFunction<T, RetVal<V>> func) {
        if (isOk()) {
            return func.apply(result());
        }
        return forwardNonnull();
    }

    @Nonnull
    public <R> RetNullable<R> thenNullable(@Nonnull NonnullReturnFunction<T, RetNullable<R>> func) {
        return null;
    }

    @Nonnull
    public <R> RetNullable<R> mapNullable(@Nonnull Function<T, R> func) {
        return null;
    }

    @Nonnull
    public RetVoid thenVoid(@Nonnull Consumer<T> consumer) {
        return null;
    }

    @Nonnull
    public RetVoid thenRun(@Nonnull Runnable runnable) {
        return null;
    }

    @Override
    public boolean isProblem() {
        this.listener.onChecked();
        return ! this.problems.isEmpty();
    }

    @Override
    public boolean hasProblems() {
        this.listener.onChecked();
        return ! this.problems.isEmpty();
    }

    @Override
    public boolean isOk() {
        this.listener.onChecked();
        return this.problems.isEmpty();
    }

    @Nonnull
    @Override
    public Collection<Problem> anyProblems() {
        // Consider this as checking for problems.  Generally, this combines the problems
        // in this instance with a larger collection, which can itself be used to check if any
        // of the values had problems.
        this.listener.onChecked();
        return this.problems;
    }

    @Nonnull
    @Override
    public Collection<Problem> validProblems() {
        // Mark as checked before ensuring it has problems.
        this.listener.onChecked();
        Ret.enforceHasProblems(this.problems);
        return this.problems;
    }

    @Nonnull
    @Override
    public String debugProblems(@Nonnull final String joinedWith) {
        return Ret.joinProblemMessages(joinedWith, this.problems);
    }
}