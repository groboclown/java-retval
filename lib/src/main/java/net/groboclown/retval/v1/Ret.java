// Released under the MIT License.
package net.groboclown.retval.v1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;


/**
 * Utility class for working with collections of return values.
 */
public class Ret {
    public static final List<Problem> NO_PROBLEMS = Collections.emptyList();

    private Ret() {
        // Prevent instantiation.
    }

    /**
     * Join the array of lists of problems into a single list.
     *
     * @param problemSets varargs of collections of problems.
     * @return all the problems combined into a single, immutable list.
     */
    @SafeVarargs
    @Nonnull
    public static List<Problem> joinProblemSets(final Iterable<Problem>... problemSets) {
        if (problemSets.length <= 0) {
            return NO_PROBLEMS;
        }
        final List<Problem> all = new ArrayList<>();
        for (final Iterable<Problem> problems : problemSets) {
            if (problems != null) {
                for (final Problem problem : problems) {
                    all.add(problem);
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
     * @param retSets vararg of collections of Ret values.
     * @return all the problems in a single, immutable list.
     */
    @SafeVarargs
    @Nonnull
    public static List<Problem> joinRetProblemSets(final Iterable<ProblemContainer>... retSets) {
        if (retSets.length <= 0) {
            return NO_PROBLEMS;
        }
        final List<Problem> all = new ArrayList<>();
        for (final Iterable<ProblemContainer> rets : retSets) {
            if (rets != null) {
                for (final ProblemContainer ret : rets) {
                    if (ret != null) {
                        all.addAll(ret.anyProblems());
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
            @Nonnull final ProblemContainer ret,
            final ProblemContainer... rets
    ) {
        final List<Problem> all = new ArrayList<>(ret.anyProblems());
        for (final ProblemContainer container : rets) {
            if (container != null) {
                all.addAll(container.anyProblems());
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
     * @return all the problems in a single, immutable list.
     */
    @Nonnull
    public static List<Problem> joinRequiredProblems(
            @Nonnull final Problem problem, final Problem... problems
    ) {
        final List<Problem> all = new ArrayList<>(1 + problems.length);
        all.add(problem);
        all.addAll(List.of(problems));
        return Collections.unmodifiableList(all);
    }


    /**
     * Enforce that the list of problems is empty by throwing an
     * IllegalStateException if it contains problems.
     *
     * @param problems list of problems to check.
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
     * @param problems list of problems to check.
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
}
