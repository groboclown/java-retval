// Released under the MIT License.
package net.groboclown.retval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillClose;
import net.groboclown.retval.function.NonnullFunction;
import net.groboclown.retval.function.NonnullThrowsFunction;
import net.groboclown.retval.problems.UnhandledExceptionProblem;


/**
 * Utility class for working with collections of return values.
 */
public class Ret {
    public static final List<Problem> NO_PROBLEMS = Collections.emptyList();

    private Ret() {
        // Prevent instantiation.
    }

    /**
     * Convenience function to create a {@link ValueAccumulator}.
     *
     * @param <T> type of the accumulator
     * @return a value accumulator
     */
    @Nonnull
    public static <T> ValueAccumulator<T> accumulateValues() {
        return ValueAccumulator.from();
    }

    /**
     * Convenience function to create a {@link ProblemCollector}.
     *
     * @return a new problem collector.
     */
    @Nonnull
    public static ProblemCollector collectProblems() {
        return ProblemCollector.from();
    }

    /**
     * Convenience function to create a {@link WarningVal} instance for the given value.
     *
     * @param value value used to initialize the return value
     * @param <T> type of the value
     * @return an instance that collects problems associated with the value.
     */
    @Nonnull
    public static <T> ValueBuilder<T> buildValue(@Nonnull final T value) {
        return ValueBuilder.from(value);
    }

    /**
     * Join the array of lists of problems into a single list.
     *
     * @param problemSet collection of problems
     * @param problemSets varargs of optional collections of problems.
     * @return all the problems combined into a single, immutable list, with no null values.
     */
    @SafeVarargs
    @Nonnull
    public static List<Problem> joinProblemSets(
            @Nullable final Collection<Problem> problemSet,
            @Nonnull final Collection<Problem>... problemSets
    ) {
        final List<Problem> all = new ArrayList<>();
        if (problemSet != null) {
            for (final Problem problem : problemSet) {
                if (problem != null) {
                    all.add(problem);
                }
            }
        }
        for (final Iterable<Problem> problems : problemSets) {
            if (problems != null) {
                for (final Problem problem : problems) {
                    if (problem != null) {
                        all.add(problem);
                    }
                }
            }
        }
        if (all.isEmpty()) {
            return NO_PROBLEMS;
        }
        return Collections.unmodifiableList(all);
    }

    /**
     * Joins problems in collections of {@link ProblemContainer} instances.
     *
     * @param retSet collection of Ret values.
     * @param retSets vararg of optional collections of Ret values.
     * @return all the problems in a single, immutable list.
     */
    @SafeVarargs
    @Nonnull
    public static List<Problem> joinRetProblemSets(
            @Nullable final Collection<ProblemContainer> retSet,
            final Collection<ProblemContainer>... retSets
    ) {
        final List<Problem> all = new ArrayList<>();
        if (retSet != null) {
            for (final ProblemContainer ret : retSet) {
                if (ret != null) {
                    ret.joinProblemsWith(all);
                }
            }
        }
        for (final Collection<ProblemContainer> rets : retSets) {
            if (rets != null) {
                for (final ProblemContainer ret : rets) {
                    if (ret != null) {
                        ret.joinProblemsWith(all);
                    }
                }
            }
        }
        if (all.isEmpty()) {
            return NO_PROBLEMS;
        }
        return Collections.unmodifiableList(all);
    }

    /**
     * Joins {@link ProblemContainer} instances into a list of problems.
     *
     * @param ret Ret value.
     * @param rets vararg of optional Ret values.
     * @return all the problems in a single, immutable list.
     */
    @Nonnull
    public static List<Problem> joinRetProblems(
            @Nullable final ProblemContainer ret,
            final ProblemContainer... rets
    ) {
        final List<Problem> all = new ArrayList<>();
        if (ret != null) {
            ret.joinProblemsWith(all);
        }
        for (final ProblemContainer container : rets) {
            if (container != null) {
                container.joinProblemsWith(all);
            }
        }
        if (all.isEmpty()) {
            return NO_PROBLEMS;
        }
        return Collections.unmodifiableList(all);
    }


    /**
     * Joins problems into a list of problems.  This requires at least one problem.
     *
     * @param problem the first problem.
     * @param problems vararg of optional problem values.
     * @return all the problems in a single, immutable list, with no null values.
     */
    @Nonnull
    public static List<Problem> joinProblems(
            @Nullable final Problem problem, final Problem... problems
    ) {
        final List<Problem> all = new ArrayList<>(1 + problems.length);
        if (problem != null) {
            all.add(problem);
        }
        for (final Problem prob : problems) {
            if (prob != null) {
                all.add(prob);
            }
        }
        return Collections.unmodifiableList(all);
    }


    /**
     * Enforce that the list of problems is empty by throwing an
     * IllegalStateException if it contains problems.
     *
     * @param problems list of problems to check, which must contain non-null values.
     * @throws IllegalStateException if the problem list is not empty.
     */
    public static void enforceNoProblems(@Nonnull final Collection<Problem> problems) {
        if (! problems.isEmpty()) {
            throw new IllegalStateException("contains problems");
        }
    }


    /**
     * Enforce that the list of problems is not empty by throwing an
     * IllegalStateException if it does not contain problems.
     *
     * @param problems list of problems to check, which must contain non-null values.
     * @return the argument
     * @throws IllegalStateException if the problem list is not empty.
     */
    @Nonnull
    public static Collection<Problem> enforceHasProblems(
            @Nonnull final Collection<Problem> problems
    ) {
        if (problems.isEmpty()) {
            throw new IllegalStateException("contains no problems");
        }
        return problems;
    }


    /**
     * Join the problem's {@link Problem#localMessage()} text into a single string, joined by the
     * joinText.  If the problem list is empty, then an empty string is returned.
     *
     * @param joinText text used to join together the problems.
     * @param problems list of problems to join together.
     * @return the joined text, or an empty string if the list of problems is empty.
     */
    @Nonnull
    public static String joinProblemMessages(
            @Nonnull final String joinText, @Nonnull final Collection<Problem> problems
    ) {
        final StringBuilder ret = new StringBuilder();
        boolean first = true;
        for (final Problem problem : problems) {
            if (first) {
                first = false;
            } else {
                ret.append(joinText);
            }
            ret.append(problem.localMessage());
        }
        // Memory inefficient, but faster.
        // The general usage of this method keeps the return value around for short times.
        return ret.toString();
    }


    /**
     * Runs a function with a closable value.  When the function completes execution,
     * the closable value is closed.  If either the function or the close action causes an
     * exception, that is wrapped in a problem and returned.  Note that a problem is
     * returned for any kind of exception, not just expected ones; if the function is expected
     * to throw an IOException, but a NullPointerException is thrown, that is still returned
     * as a problem.
     *
     * @param value value passed to the function.  This will be closed
     *              after the function is called.
     * @param func function to run
     * @param <T> type of the value passed to the function
     * @param <R> return type
     * @return the function's return value, or the exception wrapped in a problem.
     */
    @WillClose
    @Nonnull
    public static <T extends AutoCloseable, R> RetVal<R> closeWith(
            @Nonnull final T value,
            @Nonnull final NonnullThrowsFunction<T, RetVal<R>> func
    ) {
        return closeWithWrapped(value, func, RetVal::fromProblem);
    }


    /**
     * Runs a function with a closable value.  When the function completes execution,
     * the closable value is closed.  If either the function or the close action causes an
     * exception, that is wrapped in a problem and returned.  Note that a problem is
     * returned for any kind of exception, not just expected ones; if the function is expected
     * to throw an IOException, but a NullPointerException is thrown, that is still returned
     * as a problem.
     *
     * @param value value passed to the function.  This will be closed
     *              after the function is called.
     * @param func function to run
     * @param <T> type of the value passed to the function
     * @param <R> return type
     * @return the function's return value, or the exception wrapped in a problem.
     */
    @WillClose
    @Nonnull
    public static <T extends AutoCloseable, R> RetNullable<R> closeWithNullable(
            @Nonnull final T value,
            @Nonnull final NonnullThrowsFunction<T, RetNullable<R>> func
    ) {
        return closeWithWrapped(value, func, RetNullable::fromProblem);
    }


    /**
     * Runs a function with a closable value.  When the function completes execution,
     * the closable value is closed.  If either the function or the close action causes an
     * exception, that is wrapped in a problem and returned.  Note that a problem is
     * returned for any kind of exception, not just expected ones; if the function is expected
     * to throw an IOException, but a NullPointerException is thrown, that is still returned
     * as a problem.
     *
     * @param value value passed to the function.  This will be closed
     *              after the function is called.
     * @param func function to run
     * @param <T> type of the value passed to the function
     * @return the function's return value, or the exception wrapped in a problem.
     */
    @WillClose
    @Nonnull
    public static <T extends AutoCloseable> RetVoid closeWithVoid(
            @Nonnull final T value,
            @Nonnull final NonnullThrowsFunction<T, RetVoid> func
    ) {
        return closeWithWrapped(value, func, RetVoid::fromProblem);
    }


    @WillClose
    @Nonnull
    private static <T extends AutoCloseable, R extends ProblemContainer> R closeWithWrapped(
            @Nonnull final T value,
            @Nonnull final NonnullThrowsFunction<T, R> func,
            @Nonnull final NonnullFunction<List<Problem>, R> problemFactory
    ) {
        final R ret;
        try {
            ret = func.apply(value);
        } catch (final ThreadDeath | VirtualMachineError err) {
            // never ever process these.
            throw err;
        } catch (final Throwable e) {
            try {
                value.close();
            } catch (final ThreadDeath | VirtualMachineError err) {
                // never ever process these.
                throw err;
            } catch (final Throwable suppressed) {
                e.addSuppressed(suppressed);
            }
            return problemFactory.apply(List.of(UnhandledExceptionProblem.wrap(e)));
        }

        try {
            value.close();
        } catch (final ThreadDeath | VirtualMachineError err) {
            // never ever process these.
            throw err;
        } catch (final Throwable e) {
            // The ret value must pass its observation on to
            // the return object.  This means it must be
            // observed, even if it's okay.
            ret.isOk();
            return problemFactory.apply(joinProblemSets(
                    ret.anyProblems(),
                    List.of(UnhandledExceptionProblem.wrap(e))));
        }
        return ret;
    }
}
