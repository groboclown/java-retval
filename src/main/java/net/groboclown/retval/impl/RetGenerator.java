// Released under the MIT License. 
package net.groboclown.retval.impl;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.groboclown.retval.Problem;
import net.groboclown.retval.ProblemContainer;
import net.groboclown.retval.Ret;
import net.groboclown.retval.RetNullable;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.RetVoid;
import net.groboclown.retval.monitor.ObservedMonitor;


/**
 * Central handler for creating Ret* instances.
 */
public class RetGenerator {
    private RetGenerator() {
        // No state, so no constructor.
    }


    /**
     * Create a {@link RetNullable} instance with a value.
     *
     * @param value value to store in the return object.
     * @param <T> type of the value.
     * @return the return object.
     */
    @Nonnull
    public static <T> RetNullable<T> nullableOk(@Nullable final T value) {
        return new MonitoredReturnValue<>(value);
    }


    /**
     * Create a {@link RetNullable} instance with one or more problems.
     *
     * @param problems collection of problem values.
     * @param <T> type of the value.
     * @return the return object.
     */
    @Nonnull
    public static <T> RetNullable<T> nullableFromProblem(
            @Nonnull final Collection<Problem> problems
    ) {
        if (problems.isEmpty()) {
            throw new IllegalStateException("Problem return objects must have at least 1 problem");
        }
        // Need to create a copy of the problems.
        return new MonitoredReturnProblem<>(List.copyOf(problems));
    }


    /**
     * Create a new RetVal instance that has a value and no problems.
     *
     * @param value non-null value.
     * @param <T> type of the value.
     * @return a RetVal containing the value.
     * @throws NullPointerException if the value is null.
     */
    @Nonnull
    public static <T> RetVal<T> valOk(@Nonnull final T value) {
        return new MonitoredReturnValue<>(Objects.requireNonNull(value));
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
    public static <T> RetVal<T> valFromProblem(
            @Nonnull final Problem problem,
            @Nonnull final Problem... problems
    ) {
        return new MonitoredReturnProblem<>(Ret.joinProblems(problem, problems));
    }


    /**
     * Create a new RetVal instance that has errors stored in collections of
     * problems.  The arguments must contain at least one problem.
     *
     * @param problem the first problem.
     * @param problems optional list of other problems to include in this value.
     * @param <T> type of the value
     * @return an error RetVal.
     * @throws IllegalArgumentException if no problems exist within the arguments.
     */
    @SafeVarargs
    @Nonnull
    public static <T> RetVal<T> valFromProblem(
            @Nonnull final Collection<Problem> problem,
            @Nonnull final Collection<Problem>... problems
    ) {
        return new MonitoredReturnProblem<>(Ret.joinProblemSets(problem, problems));
    }


    /**
     * Create a new RetVal instance that has errors.  This is only valid if at least one
     * problem exists within all the arguments.
     *
     * <p>Normally, you would use this in situations where you collect several validations
     * together, when you know at least one of them has a problem, if not more.
     *
     * @param problem the first problem container.
     * @param problems optional list of other problem containers to include in this value.
     * @param <T> type of the value
     * @return an error RetVal.
     * @throws IllegalArgumentException if no problems exist within the arguments.
     */
    @Nonnull
    public static <T> RetVal<T> valFromProblems(
            @Nonnull final ProblemContainer problem, @Nonnull final ProblemContainer... problems
    ) {
        // ProblemContainer instances include Ret* objects, which can contain values without
        // problems.  However, this form of the constructor requires at least one problem, and
        // the standard use case is for returning a known bad state, so any values are
        // considered to be okay to lose.
        return new MonitoredReturnProblem<>(Ret.joinRetProblems(problem, problems));
    }


    /**
     * Create a new RetVal instance that has errors.  This is only valid if at least one
     * problem exists within all the arguments.
     *
     * <p>Normally, you would use this in situations where you collect several validations
     * together, when you know at least one of them has a problem, if not more.
     *
     * @param problem the first problem container.
     * @param problems optional list of other problem containers to include in this value.
     * @param <T> type of the value
     * @return an error RetVal.
     * @throws IllegalArgumentException if no problems exist within the arguments.
     */
    @SafeVarargs
    @Nonnull
    public static <T> RetVal<T> valFromProblems(
            @Nonnull final Collection<ProblemContainer> problem,
            @Nonnull final Collection<ProblemContainer>... problems
    ) {
        return new MonitoredReturnProblem<>(Ret.joinRetProblemSets(problem, problems));
    }


    /**
     * Return a void object with no problems.
     *
     * @return a no-problem void object.
     */
    @Nonnull
    public static RetVoid voidOk() {
        if (ObservedMonitor.getCheckedInstance().isTraceEnabled()) {
            return new MonitoredRetVoidOk();
        }
        // Tracing is disabled, so use the more memory efficient version.
        return MonitoredRetVoidOk.OK;
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
    public static RetVoid voidFromProblem(
            @Nonnull final Collection<Problem> problemSet,
            @Nonnull final Collection<Problem>... problemSets
    ) {
        final List<Problem> all = Ret.joinProblemSets(problemSet, problemSets);
        if (all.isEmpty()) {
            return voidOk();
        }
        return new MonitoredReturnProblem<>(all);
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
    public static RetVoid voidFromProblem(
            @Nullable final Problem problem,
            final Problem... problems) {
        final List<Problem> all = Ret.joinProblems(problem, problems);
        if (all.isEmpty()) {
            return voidOk();
        }
        return new MonitoredReturnProblem<>(all);
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
    public static RetVoid voidFromProblems(
            @Nonnull final Collection<ProblemContainer> retSet,
            @Nonnull final Collection<ProblemContainer>... retSets
    ) {
        final List<Problem> all = Ret.joinRetProblemSets(retSet, retSets);
        if (all.isEmpty()) {
            return voidOk();
        }
        return new MonitoredReturnProblem<>(all);
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
    public static RetVoid voidFromProblems(
            @Nullable final ProblemContainer ret,
            @Nonnull final ProblemContainer... rets
    ) {
        final List<Problem> all = Ret.joinRetProblems(ret, rets);
        if (all.isEmpty()) {
            return voidOk();
        }
        return new MonitoredReturnProblem<>(all);
    }
}
