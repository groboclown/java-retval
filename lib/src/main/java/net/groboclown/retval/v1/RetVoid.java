// Released under the MIT License.
package net.groboclown.retval.v1;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A simple returnable that can have problems, but no value.
 */
@Immutable
public class RetVoid implements ProblemContainer, Returnable {
    /**
     * Constant value for a RetVoid with no problems.
     */
    public static final RetVoid OK = new RetVoid();

    private final List<Problem> problems;

    private RetVoid() {
        this.problems = Collections.emptyList();
    }

    private RetVoid(@Nonnull List<Problem> problems) {
        // This must be ensured on entry as immutable.
        this.problems = problems;
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
    public static RetVoid withProblemSets(final Iterable<Problem>... problemSets) {
        // Simple, easy check.
        if (problemSets.length <= 0) {
            return OK;
        }
        final List<Problem> all = Ret.joinProblemSets(problemSets);
        if (all.isEmpty()) {
            return OK;
        }
        return new RetVoid(all);
    }

    @Nonnull
    public static RetVoid withProblems(final Problem... problems) {
        // Simple, easy check.
        if (problems.length <= 0) {
            return OK;
        }
        return new RetVoid(List.of(problems));
    }

    @SafeVarargs
    @Nonnull
    public static RetVoid withRetSets(final Iterable<ProblemContainer>... rets) {
        // Simple, easy check.
        if (rets.length <= 0) {
            return OK;
        }
        final List<Problem> all = Ret.joinRetProblemSets(rets);
        if (all.isEmpty()) {
            return OK;
        }
        return new RetVoid(all);
    }

    @Nonnull
    public static RetVoid withRets(final ProblemContainer... rets) {
        // Simple, easy check.
        if (rets.length <= 0) {
            return OK;
        }
        final List<Problem> all = Ret.joinRetProblems(rets);
        if (all.isEmpty()) {
            return OK;
        }
        return new RetVoid(all);
    }

    @Override
    public boolean isProblem() {
        return ! this.problems.isEmpty();
    }

    @Override
    public boolean hasProblems() {
        return ! this.problems.isEmpty();
    }

    @Override
    public boolean isOk() {
        return this.problems.isEmpty();
    }

    @Nonnull
    @Override
    public Collection<Problem> anyProblems() {
        return this.problems;
    }

    @Nonnull
    @Override
    public Collection<Problem> validProblems() {
        return Ret.enforceHasProblems(this.problems);
    }
}
