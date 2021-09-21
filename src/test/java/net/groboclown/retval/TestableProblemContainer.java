// Released under the MIT License. 
package net.groboclown.retval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/** A container that can contain null problems. */
public class TestableProblemContainer implements ProblemContainer {
    private final List<Problem> problems;
    private final int problemCount;

    /** Constructor with 0 or more, possibly null, values. */
    public TestableProblemContainer(final Problem... problems) {
        final List<Problem> probs = new ArrayList<>();
        int count = 0;
        for (final Problem problem : problems) {
            if (problem != null) {
                count++;
            }
            probs.add(problem);
        }
        this.problems = Collections.unmodifiableList(probs);
        this.problemCount = count;
    }

    @Override
    public boolean isProblem() {
        return this.problemCount > 0;
    }

    @Override
    public boolean hasProblems() {
        return this.problemCount > 0;
    }

    @Override
    public boolean isOk() {
        return this.problemCount <= 0;
    }

    @Nonnull
    @Override
    public Collection<Problem> anyProblems() {
        return this.problems;
    }

    @Nonnull
    @Override
    public Collection<Problem> validProblems() {
        if (this.problemCount <= 0) {
            throw new IllegalStateException();
        }
        return this.problems;
    }

    @Nonnull
    @Override
    public String debugProblems(@Nonnull final String joinedWith) {
        return Ret.joinProblemMessages(joinedWith, this.problems);
    }

    @Override
    public void joinProblemsWith(@Nonnull final Collection<Problem> problemList) {
        for (final Problem problem : this.problems) {
            if (problem != null) {
                problemList.add(problem);
            }
        }
    }

    // For test compatibility
    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (other.getClass().equals(TestableProblemContainer.class)) {
            return this.problems.equals(((TestableProblemContainer) other).problems);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.problems.hashCode();
    }
}
