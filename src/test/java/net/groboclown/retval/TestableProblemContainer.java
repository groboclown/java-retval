// Released under the MIT License. 
package net.groboclown.retval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

// A container that can contain null values.
class TestableProblemContainer implements ProblemContainer {
    private final List<Problem> problems;
    private final int problemCount;

    TestableProblemContainer(final Problem... problems) {
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
}
