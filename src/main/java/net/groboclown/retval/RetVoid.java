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
import net.groboclown.retval.monitor.NoOpObservedMonitor;
import net.groboclown.retval.monitor.ObservedMonitor;


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
    private final ObservedMonitor.Listener listener;

    /**
     * Return a void object with no problems.
     *
     * @return a no-problem void object.
     */
    @Nonnull
    public static RetVoid ok() {
        if (ObservedMonitor.getCheckedInstance().isTraceEnabled()) {
            return new RetVoid(Collections.emptyList());
        }
        // Tracing is disabled, so use the more memory efficient version.
        return OK;
    }

    /**
     * Constructs a {@link RetVoid} instance with the collections of problems.
     * This is optimized to reduce the memory load where easy.
     *
     * @param problemSet first collection of problems
     * @param problemSets vararg optional problems that should be included in this object.
     * @return a RetVoid with all the given problems.
     */
    @SafeVarargs
    @Nonnull
    public static RetVoid fromProblem(
            @Nonnull final Collection<Problem> problemSet,
            @Nonnull final Collection<Problem>... problemSets
    ) {
        final List<Problem> all = Ret.joinProblemSets(problemSet, problemSets);
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
     * @param problem initial problem
     * @param problems vararg optional list of problems.
     * @return a new void instance, possibly without problems.
     */
    @Nonnull
    public static RetVoid fromProblem(
            @Nullable final Problem problem,
            final Problem... problems) {
        final List<Problem> all = Ret.joinProblems(problem, problems);
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
        this.listener = NoOpObservedMonitor.LISTENER;
        this.problems = Collections.emptyList();
    }

    // package-protected to allow for limiting memory use in the package, specifically to
    // pass known immutable lists.
    RetVoid(@Nonnull final List<Problem> problems) {
        // This must be ensured on entry as immutable.
        // For RetVoid, the problem list may be empty.
        this.problems = problems;
        this.listener = ObservedMonitor.getCheckedInstance().registerInstance(this);
    }

    @Override
    public boolean hasProblems() {
        // This acts as a check only if there is no problem, because there is
        // no value the developer needs to extract.
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
        // This acts as a check only if there is no problem, because there is
        // no value the developer needs to extract.
        if (this.problems.isEmpty()) {
            this.listener.onObserved();
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public Collection<Problem> anyProblems() {
        // This acts as a check, because there is no value to extract.
        this.listener.onObserved();
        return this.problems;
    }

    @Nonnull
    @Override
    public Collection<Problem> validProblems() {
        // Put the observation check first.  Extraction counts as an observation.
        this.listener.onObserved();
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
     * <p>Formally, this doesn't "map" one value to another.  However, for symmetry with the
     * other Ret classes, it is here called map.
     *
     * @param supplier functional object that returns a non-null value.
     * @param <R> return value type
     * @return a RetVal, either an error if this object has an error, or the value returned by
     *     the supplier.
     */
    @Nonnull
    public <R> RetVal<R> map(@Nonnull final NonnullSupplier<R> supplier) {
        return thenWrapVal(() -> RetVal.ok(supplier.get()));
    }

    /**
     * If there is no problem, run the supplier and return its value.  Otherwise, return
     * this object's problems.
     *
     * @param supplier supplier of the return value; run only if this object has no problems.
     * @param <R> return value type
     * @return the problems in this object, or the supplier's return value.
     */
    @Nonnull
    public <R> RetNullable<R> thenNullable(
            @Nonnull final NonnullSupplier<RetNullable<R>> supplier
    ) {
        return thenWrapNullable(supplier);
    }


    /**
     * Return a non-null {@link RetNullable} value using a supplier that returns a value.
     * The supplier is called only if this object has no error.
     *
     * <p>Formally, this doesn't "map" one value to another.  However, for symmetry with the
     * other Ret classes, it is here called map.
     *
     * @param supplier functional object that returns a non-null value.
     * @param <R> return value type
     * @return a RetVal, either an error if this object has an error, or the value returned by
     *     the supplier.
     */
    @Nonnull
    public <R> RetNullable<R> mapNullable(@Nonnull final Supplier<R> supplier) {
        return thenWrapNullable(() -> RetNullable.ok(supplier.get()));
    }


    /**
     * Return the supplier if there is no problem.
     *
     * @param supplier function to run
     * @return this object if there are problems in this object, otherwise the supplier's
     *      return value.
     */
    @Nonnull
    public RetVoid thenVoid(@Nonnull final NonnullSupplier<RetVoid> supplier) {
        if (! this.problems.isEmpty()) {
            // Return this object without a check.
            return this;
        }
        // Otherwise, pass the check onto the supplier's return value.
        this.listener.onObserved();
        return supplier.get();
    }

    /**
     * Run the runnable if there is no problem.
     *
     * @param runnable function to run
     * @return this object.
     */
    @Nonnull
    public RetVoid thenRun(@Nonnull final Runnable runnable) {
        if (this.problems.isEmpty()) {
            runnable.run();
        }
        return this;
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
        this.listener.onObserved();

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

    @Override
    public void joinProblemsWith(@Nonnull final Collection<Problem> problemList) {
        // This acts as closing off this value and passing the problem state to the
        // list.
        this.listener.onObserved();
        problemList.addAll(this.problems);
    }

    @Override
    public String toString() {
        return "RetVoid("
                + (this.problems.isEmpty()
                    ? "ok"
                    : (this.problems.size() + " problems: " + debugProblems("; "))
                ) + ")";
    }
}
