// Released under the MIT License.
package net.groboclown.retval.v1;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for working with collections of return values.
 */
public class Ret {
    public static final List<Problem> NO_PROBLEMS = Collections.emptyList();

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
        for (final Iterable<Problem> problems: problemSets) {
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
        for (final Iterable<ProblemContainer> rets: retSets) {
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
     * @param rets vararg of Ret values.
     * @return all the problems in a single, immutable list.
     */
    @Nonnull
    public static List<Problem> joinRetProblems(final ProblemContainer... rets) {
        if (rets.length <= 0) {
            return NO_PROBLEMS;
        }
        final List<Problem> all = new ArrayList<>();
        for (final ProblemContainer ret : rets) {
            if (ret != null) {
                all.addAll(ret.anyProblems());
            }
        }
        if (all.isEmpty()) {
            return NO_PROBLEMS;
        }
        return Collections.unmodifiableList(all);
    }


    /**
     * Enforce that the list of problems is empty by throwing an
     * IllegalStateException if it contains problems.
     *
     * @param problems list of problems to check.
     * @return the argument
     * @throws IllegalStateException if the problem list is not empty.
     */
    @Nonnull
    public static Collection<Problem> enforceNoProblems(@Nonnull final Collection<Problem> problems) {
        if (! problems.isEmpty()) {
            throw new IllegalStateException("contains problems");
        }
        return problems;
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
    public static Collection<Problem> enforceHasProblems(@Nonnull final Collection<Problem> problems) {
        if (problems.isEmpty()) {
            throw new IllegalStateException("contains no problems");
        }
        return problems;
    }
}
