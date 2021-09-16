// Released under the MIT License.
package net.groboclown.retval.v1;

import net.groboclown.retval.v1.impl.NoOpCheckMonitor;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;


/**
 * A simple returnable that can have problems, but no value.
 */
@Immutable
public class RetVoid implements ProblemContainer, ContinuationVoid {
    /**
     * Constant value for a RetVoid with no problems.  This is private to force
     * use of the "ok" function call, which may be replaced to return a new object
     * each time.  While the constant OK is fine for production, it means check tracing
     * can't be performed.
     */
    private static final RetVoid OK = new RetVoid();

    private final List<Problem> problems;
    private final CheckMonitor.CheckableListener listener;

    @Nonnull
    public static RetVoid ok() {
        if (CheckMonitor.getInstance().isTraceEnabled()) {
            return new RetVoid(Collections.emptyList());
        }
        // Tracing is disabled, so use the more memory efficient version.
        return OK;
    }

    /**
     * Constructs a {@link RetVoid} instance with the collections of problems.
     * This is optimized to reduce the memory load where easy.
     *
     * @param problemSets problems that should be included in this object.
     * @return a RetVoid with all the given problems.
     */
    @SafeVarargs
    @Nonnull
    public static RetVoid fromProblemSets(final Iterable<Problem>... problemSets) {
        // Simple, easy check.
        if (problemSets.length <= 0) {
            return ok();
        }
        final List<Problem> all = Ret.joinProblemSets(problemSets);
        if (all.isEmpty()) {
            return ok();
        }
        return new RetVoid(all);
    }

    @Nonnull
    public static RetVoid error(final Problem... problems) {
        // Simple, easy check.
        if (problems.length <= 0) {
            return ok();
        }
        return new RetVoid(List.of(problems));
    }

    @SafeVarargs
    @Nonnull
    public static RetVoid fromRetSets(final Iterable<ProblemContainer>... rets) {
        // Simple, easy check.
        if (rets.length <= 0) {
            return ok();
        }
        final List<Problem> all = Ret.joinRetProblemSets(rets);
        if (all.isEmpty()) {
            return ok();
        }
        return new RetVoid(all);
    }

    @Nonnull
    public static RetVoid errors(final ProblemContainer... rets) {
        // Simple, easy check.
        if (rets.length <= 0) {
            return ok();
        }
        final List<Problem> all = Ret.joinRetProblems(rets);
        if (all.isEmpty()) {
            return ok();
        }
        return new RetVoid(all);
    }

    private RetVoid() {
        // This should only be called for the OK object, which, being a constant,
        // must not incorrectly report errors on the checking.  So use the no-op checker.
        this.listener = NoOpCheckMonitor.CHECKABLE_LISTENER;
        this.problems = Collections.emptyList();
    }

    // package-protected to allow for limiting memory use in the package, specifically to
    // pass known immutable lists.
    RetVoid(@Nonnull final List<Problem> problems) {
        // This must be ensured on entry as immutable.
        this.problems = problems;
        this.listener = CheckMonitor.getInstance().registerErrorInstance(this);
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
        this.listener.onChecked();
        return this.problems;
    }

    @Nonnull
    @Override
    public Collection<Problem> validProblems() {
        this.listener.onChecked();
        return Ret.enforceHasProblems(this.problems);
    }

    @Nonnull
    @Override
    public String debugProblems(@Nonnull final String joinedWith) {
        // This is not a check.  This is a debug.
        return Ret.joinProblemMessages(joinedWith, this.problems);
    }

    @Nonnull
    @Override
    public <R> RetVal<R> then(@Nonnull final NonnullSupplier<RetVal<R>> supplier) {
        return thenWrapVal(supplier);
    }

    @Nonnull
    @Override
    public <R> RetVal<R> thenValue(@Nonnull final NonnullSupplier<R> supplier) {
        return thenWrapVal(() -> RetVal.ok(supplier.get()));
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> thenNullable(@Nonnull final NonnullSupplier<RetNullable<R>> supplier) {
        return thenWrapNullable(supplier);
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> thenNullableValue(@Nonnull final Supplier<R> supplier) {
        return thenWrapNullable(() -> RetNullable.ok(supplier.get()));
    }

    @Nonnull
    @Override
    public RetVoid thenVoid(@Nonnull final NonnullSupplier<RetVoid> supplier) {
        return thenWrapVoid(supplier);
    }

    @Nonnull
    @Override
    public RetVoid thenRun(@Nonnull final Runnable runnable) {
        return thenWrapVoid(() -> {
            runnable.run();
            return ok();
        });
    }

    /**
     * Helper function to perform the correct wrappings around a "then" style call.
     *
     * @param supplier function that returns the expected value when the problem inspections clear.
     * @param <R> type of the real "then" return.
     */
    @Nonnull
    private <R> R thenWrap(
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
    protected <R> RetNullable<R> thenWrapNullable(@Nonnull final NonnullSupplier<RetNullable<R>> supplier) {
        // Be as memory efficient as possible.
        return thenWrap(supplier, () -> new RetNullable<>(this.problems));
    }

    @Nonnull
    protected <R> RetVal<R> thenWrapVal(@Nonnull final NonnullSupplier<RetVal<R>> supplier) {
        // Be as memory efficient as possible.
        return thenWrap(supplier, () -> new RetVal<>(this.problems));
    }

    @Nonnull
    protected RetVoid thenWrapVoid(@Nonnull final NonnullSupplier<RetVoid> supplier) {
        // Be as memory efficient as possible.
        return thenWrap(supplier, () -> new RetVoid(this.problems));
    }


    @Nonnull
    @Override
    public <R> RetVal<R> with(@Nonnull final NonnullSupplier<RetVal<R>> supplier) {
        return withWrapper(supplier, RetVal::new);
    }

    @Nonnull
    @Override
    public <R> RetVal<R> withValue(@Nonnull final NonnullSupplier<R> supplier) {
        return withWrapper(() -> RetVal.ok(supplier.get()), RetVal::new);
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> withNullable(@Nonnull final NonnullSupplier<RetNullable<R>> supplier) {
        return withWrapper(supplier, RetNullable::new);
    }

    @Nonnull
    @Override
    public <R> RetNullable<R> withNullableValue(@Nonnull final Supplier<R> supplier) {
        return withWrapper(() -> RetNullable.ok(supplier.get()), RetNullable::new);
    }

    @Nonnull
    @Override
    public RetVoid withVoid(@Nonnull final NonnullSupplier<RetVoid> supplier) {
        return withWrapper(supplier, RetVoid::new);
    }


    /**
     * Helper function to perform the correct wrappings around a "then" style call.
     *
     * @param supplier function that returns the expected value when the problem inspections clear.
     * @param <R> type of the real "then" return.
     */
    @Nonnull
    private <R extends ProblemContainer> R withWrapper(
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
}
