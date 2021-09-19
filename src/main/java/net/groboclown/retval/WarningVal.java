// Released under the MIT License.
package net.groboclown.retval;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;


/**
 * A container for a non-null value and associated problems.  Reflects an object that,
 * while valid, may have messages that the user should be made aware of.
 */
@Immutable
public class WarningVal<T> implements ProblemContainer {
    // Because these are warnings, the problems do not need to be checked.

    private final List<Problem> problems;
    private final T value;

    // problems must be nonnull, immutable, and contain no null values.
    private WarningVal(@Nonnull final T value, @Nonnull final List<Problem> problems) {
        this.value = Objects.requireNonNull(value);
        this.problems = problems;
    }

    @Nonnull
    public static <T> WarningVal<T> from(@Nonnull final T value) {
        return new WarningVal<>(value, Collections.emptyList());
    }

    @Nonnull
    public static <T> WarningVal<T> from(
            @Nonnull final T value,
            @Nonnull final ProblemContainer problems
    ) {
        return new WarningVal<>(value, Ret.joinRetProblems(problems));
    }


    @Nonnull
    public T getValue() {
        return this.value;
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
        Ret.enforceHasProblems(this.problems);
        return this.problems;
    }

    @Nonnull
    @Override
    public String debugProblems(@Nonnull final String joinedWith) {
        return Ret.joinProblemMessages(joinedWith, this.problems);
    }

    @Override
    public void joinProblemsWith(@Nonnull final Collection<Problem> problemList) {
        problemList.addAll(this.problems);
    }
}
