// Released under the MIT License.
package net.groboclown.retval.v1;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;


/**
 * A non-null value holder version of a problem container.
 *
 * @param <T> type of the contained value.
 */
public class RetVal<T> implements ProblemContainer, ContinuationValue<T> {
    private final List<Problem> problems;
    private final CheckMonitor.CheckableListener listener;
    private final T value;

    /**
     * Create a new RetVal instance that has a value and no problems.
     *
     * @param value non-null value.
     * @param <T> type of the value.
     * @return a RetVal containing the value.
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
    public static <T> RetVal<T> error(@Nonnull final Problem problem, @Nonnull final Problem... problems) {
        return new RetVal<>(Ret.joinRequiredProblems(problem, problems));
    }

    @SafeVarargs
    @Nonnull
    public static <T> RetVal<T> error(@Nonnull final Iterable<Problem>... problems) {
        return new RetVal<>(Ret.joinProblemSets(problems));
    }

    // made package-protected to allow other classes in this package to pass in known
    // non-null, non-empty, immutable problem lists.
    RetVal(@Nonnull final List<Problem> problems) {
        this.problems = problems;
        this.listener = CheckMonitor.getInstance().registerErrorInstance(this);
        this.value = null;
    }

    private RetVal(@Nonnull final T value) {
        this.problems = Collections.emptyList();
        this.listener = CheckMonitor.getInstance().registerErrorInstance(this);
        this.value = value;
    }

    /**
     * Get the value contained in this instance.  If this is an error state, then the value will be null.
     * @return the value, which will be null if there are problems.
     */
    @Nullable
    public T getValue() {
        // This does not indicate that a check was made.  An implicit one can be made by the
        // caller, but this method does not mark it as such.
        return this.value;
    }

    /**
     * Get the result, which is always non-null.  If this instance has problems, then a runtime exception
     * is thrown.
     * @return the non-null value, only if this instance is ok.
     */
    @Nonnull
    public T result() {
        // This implicitly means that a check was made.
        this.listener.onChecked();

        Ret.enforceNoProblems(this.problems);
        // Based on the constructor, by having no problems, the value must be non-null.
        return this.value;
    }

    /**
     * Forward this object to a different typed RetVal instance.  This will only work when the
     * instance has problems.
     *
     * @param <V> altered type.
     * @return the type-altered version
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public <V> RetVal<V> forwardError() {
        Ret.enforceHasProblems(this.problems);
        // most memory efficient.  This will work because null is any object type.
        // Becaue the same instance is returned, the onCheck is not called.
        return (RetVal<V>) this;
    }

    /**
     * Forward this instance as a nullable with a different value type, but only if it has errors.  If it
     * does not have errors, then a runtime exception is thrown.
     *
     * @param <V> destination type
     * @return the value, only if this instance has errors.
     */
    @Nonnull
    public <V> RetNullable<V> forwardErrorNullable() {
        this.listener.onChecked();
        Ret.enforceHasProblems(this.problems);
        // memory efficient access.
        return new RetNullable<>(this.problems);
    }

    /**
     * Forward this instance as a RetVoid object.
     * @return this instance as a RetVoid object.  Any problems in this will be moved into the returned object.
     */
    @Nonnull
    public RetVoid asVoid() {
        return thenWrapped(RetVoid::ok, () -> new RetVoid(this.problems));
    }

    /**
     * Forward this instance as a nullable instance with the same value type.
     * @return a nullable version of the same instance.
     */
    @Nonnull
    public RetNullable<T> asNullable() {
        return thenWrapped(() -> RetNullable.ok(this.value), () -> new RetNullable<>(this.problems));
    }

    @Nonnull
    @Override
    public <R> RetVal<R> then(@Nonnull final NonnullFunction<T, RetVal<R>> func) {
        return thenWrapped(() -> func.apply(this.value), this::forwardError);
    }

    @Nonnull
    @Override
    public <R> RetVal<R> thenValue(@Nonnull final NonnullFunction<T, R> func) {
        return thenWrapped(() -> ok(func.apply(this.value)), this::forwardError);
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> thenNullable(@Nonnull final NonnullFunction<T, RetNullable<R>> func) {
        return thenWrapped(() -> func.apply(this.value), this::forwardErrorNullable);
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> thenNullableValue(@Nonnull final NonnullParamFunction<T, R> func) {
        return thenWrapped(() -> RetNullable.ok(func.apply(this.value)), this::forwardErrorNullable);
    }

    @Nonnull
    @Override
    public RetVoid thenRun(@Nonnull final Runnable runner) {
        return thenWrapped(() -> {
            runner.run();
            return RetVoid.ok();
        }, () -> new RetVoid(this.problems));
    }

    @Nonnull
    @Override
    public <R> RetVal<R> then(@Nonnull final NonnullSupplier<RetVal<R>> supplier) {
        return thenWrapped(supplier, this::forwardError);
    }

    @Nonnull
    @Override
    public <R> RetVal<R> thenValue(@Nonnull final NonnullSupplier<R> supplier) {
        return thenWrapped(() -> RetVal.ok(supplier.get()), this::forwardError);
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> thenNullable(@Nonnull final NonnullSupplier<RetNullable<R>> supplier) {
        return thenWrapped(supplier, this::forwardErrorNullable);
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> thenNullableValue(@Nonnull final Supplier<R> supplier) {
        return thenWrapped(() -> RetNullable.ok(supplier.get()), this::forwardErrorNullable);
    }

    @Nonnull
    @Override
    public RetVoid thenVoid(@Nonnull final NonnullConsumer<T> consumer) {
        return thenWrapped(() -> {
            consumer.accept(this.value);
            return RetVoid.ok();
        }, () -> new RetVoid(this.problems));
    }

    @Nonnull
    @Override
    public RetVoid thenVoid(@Nonnull final NonnullSupplier<RetVoid> supplier) {
        return thenWrapped(supplier, () -> new RetVoid(this.problems));
    }

    private <R> R thenWrapped(
            @Nonnull final NonnullSupplier<R> supplier, @Nonnull final NonnullSupplier<R> errorFactory
    ) {
        // Pass the checking ownership to the created object.
        this.listener.onChecked();

        if (hasProblems()) {
            return errorFactory.get();
        }
        return supplier.get();
    }

    @Nonnull
    @Override
    public <R> RetVal<R> with(@Nonnull final NonnullFunction<T, RetVal<R>> func) {
        return withWrapped(() -> func.apply(this.value), RetVal::new);
    }

    @Nonnull
    @Override
    public <R> RetVal<R> withValue(@Nonnull final NonnullFunction<T, R> func) {
        return withWrapped(() -> RetVal.ok(func.apply(this.value)), RetVal::new);
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> withNullable(@Nonnull final NonnullFunction<T, RetNullable<R>> func) {
        return withWrapped(() -> func.apply(this.value), RetNullable::new);
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> withNullableValue(@Nonnull final NonnullParamFunction<T, R> func) {
        return withWrapped(() -> RetNullable.ok(func.apply(this.value)), RetNullable::new);
    }

    @Nonnull
    @Override
    public <R> RetVal<R> with(@Nonnull final NonnullSupplier<RetVal<R>> supplier) {
        return withWrapped(supplier, RetVal::new);
    }

    @Nonnull
    @Override
    public <R> RetVal<R> withValue(@Nonnull final NonnullSupplier<R> supplier) {
        return withWrapped(() -> RetVal.ok(supplier.get()), RetVal::new);
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> withNullable(@Nonnull final NonnullSupplier<RetNullable<R>> supplier) {
        return withWrapped(supplier, RetNullable::new);
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> withNullableValue(@Nonnull final Supplier<R> supplier) {
        return withWrapped(() -> RetNullable.ok(supplier.get()), RetNullable::new);
    }

    @Nonnull
    @Override
    public RetVoid withVoid(@Nonnull final NonnullSupplier<RetVoid> supplier) {
        return withWrapped(supplier, RetVoid::new);
    }

    private <R extends ProblemContainer> R withWrapped(
            @Nonnull final NonnullSupplier<R> supplier, @Nonnull final NonnullFunction<List<Problem>, R> errorFactory
    ) {
        // Pass the checking ownership to the created object.
        this.listener.onChecked();
        final R value = supplier.get();
        if (hasProblems() && ! value.hasProblems()) {
            // super memory efficient version
            return errorFactory.apply(this.problems);
        }
        if (hasProblems() || value.hasProblems()) {
            // joinRetProblems returns an immutable list.
            return errorFactory.apply(Ret.joinRetProblems(this, value));
        }
        return value;
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
        // Consider this as checking for problems.
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
