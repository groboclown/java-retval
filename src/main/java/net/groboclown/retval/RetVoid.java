// Released under the MIT License.
package net.groboclown.retval;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import net.groboclown.retval.function.NonnullFunction;
import net.groboclown.retval.function.NonnullSupplier;
import net.groboclown.retval.monitor.CheckMonitor;
import net.groboclown.retval.monitor.NoOpCheckMonitor;


/**
 * A simple returnable that can have problems, but no value.
 *
 * <p>Because these instances have no value, they allow for more flexibility in their
 * functionality than the other classes.
 */
@Immutable
public class RetVoid implements ProblemContainer {
    /**
     * Constant value for a RetVoid with no problems.  This is private to force
     * use of the "ok" function call, which may be replaced to return a new object
     * each time.  While the constant OK is fine for production, it means check tracing
     * can't be performed.
     */
    private static final RetVoid OK = new RetVoid();

    private final List<Problem> problems;
    private final CheckMonitor.CheckableListener listener;

    /**
     * Return a void object with no problems.
     *
     * @return a no-problem void object.
     */
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
    public static RetVoid fromProblem(@Nonnull final Collection<Problem>... problemSets) {
        // Simple, easy check.
        if (problemSets.length <= 0) {
            return ok();
        }
        final List<Problem> all = Ret.joinProblemSets(List.of(), problemSets);
        if (all.isEmpty()) {
            return ok();
        }
        return new RetVoid(all);
    }

    /**
     * Create a RetVoid containing the passed-in problems.  If there was
     * no problem in any of the arguments, then this will return a no-problem
     * value.
     *
     * @param problems a list of problems.
     * @return a new void instance, possibly without problems.
     */
    @Nonnull
    public static RetVoid fromProblem(final Problem... problems) {
        // Simple, easy check.
        if (problems.length <= 0) {
            return ok();
        }
        return new RetVoid(List.of(problems));
    }

    /**
     * Create a RetVoid containing the passed-in problems.  If there was
     * no problem in any of the arguments, then this will return a no-problem
     * value.
     *
     * @param retSet primary collection of problem containers.
     * @param retSets vararg optional problem container collections.
     * @return a new void instance, possibly without problems.
     */
    @SafeVarargs
    @Nonnull
    public static RetVoid fromProblems(
            @Nonnull final Collection<ProblemContainer> retSet,
            @Nonnull final Collection<ProblemContainer>... retSets
    ) {
        final List<Problem> all = Ret.joinRetProblemSets(retSet, retSets);
        if (all.isEmpty()) {
            return ok();
        }
        return new RetVoid(all);
    }

    /**
     * Create a RetVoid containing the passed-in problems.  If there was
     * no problem in any of the arguments, then this will return a no-problem
     * value.
     *
     * @param ret a problem container
     * @param rets an optional list of problem containers.
     * @return a new void instance, possibly without problems.
     */
    @Nonnull
    public static RetVoid fromProblems(
            @Nullable final ProblemContainer ret,
            @Nonnull final ProblemContainer... rets
    ) {
        final List<Problem> all = Ret.joinRetProblems(ret, rets);
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
        // For RetVoid, the problem list may be empty.
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

    /**
     * Return a non-null {@link RetVal} value using a supplier that itself returns a {@link RetVal}.
     * The supplier is called only if this object has no error.
     *
     * @param supplier functional object that returns a RetVal.
     * @param <R> type of the returned value.
     * @return the error of the current value, if it is an error, or the object returned by
     *     the supplier.
     */
    @Nonnull
    public <R> RetVal<R> then(@Nonnull final NonnullSupplier<RetVal<R>> supplier) {
        return thenWrapVal(supplier);
    }

    /**
     * Return a non-null {@link RetVal} value using a supplier that returns a value.
     * The supplier is called only if this object has no error.
     *
     * @param supplier functional object that returns a non-null value.
     * @param <R> return value type
     * @return a RetVal, either an error if this object has an error, or the value returned by
     *     the supplier.
     */
    @Nonnull
    public <R> RetVal<R> thenValue(@Nonnull final NonnullSupplier<R> supplier) {
        return thenWrapVal(() -> RetVal.ok(supplier.get()));
    }

    @Nonnull
    public <R> RetNullable<R> thenNullable(
            @Nonnull final NonnullSupplier<RetNullable<R>> supplier
    ) {
        return thenWrapNullable(supplier);
    }

    @Nonnull
    public <R> RetNullable<R> thenNullableValue(@Nonnull final Supplier<R> supplier) {
        return thenWrapNullable(() -> RetNullable.ok(supplier.get()));
    }

    @Nonnull
    public RetVoid thenVoid(@Nonnull final NonnullSupplier<RetVoid> supplier) {
        return thenWrapVoid(supplier);
    }

    @Nonnull
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
            @Nonnull final NonnullSupplier<R> supplier,
            @Nonnull final NonnullSupplier<R> errorFactory
    ) {
        // Pass the checking ownership to the created object.
        this.listener.onChecked();

        if (hasProblems()) {
            return errorFactory.get();
        }
        return supplier.get();
    }

    @Nonnull
    protected <R> RetNullable<R> thenWrapNullable(
            @Nonnull final NonnullSupplier<RetNullable<R>> supplier
    ) {
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


    /**
     * Runs the supplier regardless of the current object's error state.  If the supplier
     * returns an error, or if the current object has an error, then the errors are combined.
     * Only if the current object has no error and the supplier returns no error does the
     * returned object have a value.  If the current object has an error and the supplier
     * returns a value, the value is lost.
     *
     * @param supplier functional object that returns a RetVal.  Always called.
     * @param <R> type of the returned value.
     * @return a RetVal with the combined errors of the current object and the supplier.  In the
     *     case where both objects have no errors, the returned object will contain the value of
     *     the supplier.
     */
    @Nonnull
    public <R> RetVal<R> with(@Nonnull final NonnullSupplier<RetVal<R>> supplier) {
        return withWrapper(supplier, RetVal::new);
    }

    @Nonnull
    public <R> RetVal<R> withValue(@Nonnull final NonnullSupplier<R> supplier) {
        return withWrapper(() -> RetVal.ok(supplier.get()), RetVal::new);
    }

    @Nonnull
    public <R> RetNullable<R> withNullable(
            @Nonnull final NonnullSupplier<RetNullable<R>> supplier
    ) {
        return withWrapper(supplier, RetNullable::new);
    }

    @Nonnull
    public <R> RetNullable<R> withNullableValue(@Nonnull final Supplier<R> supplier) {
        return withWrapper(() -> RetNullable.ok(supplier.get()), RetNullable::new);
    }

    @Nonnull
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
            @Nonnull final NonnullSupplier<R> supplier,
            @Nonnull final NonnullFunction<List<Problem>, R> errorFactory
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
