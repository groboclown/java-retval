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
    // The general use case as not warnings has the value picked at for valid
    // data even if there are problems.  In this case, observability on the
    // value is not valid; there's a possibility that the problem state should
    // be inspected.

    private final List<Problem> problems;
    private final T value;

    // problems must be nonnull, immutable, and contain no null values.
    private WarningVal(@Nonnull final T value, @Nonnull final List<Problem> problems) {
        this.value = Objects.requireNonNull(value);
        this.problems = problems;
    }

    /**
     * Create the warning from a value with no problems.
     *
     * @param value value to store in the warning instance.
     * @param <T> value type
     * @return the warning
     */
    @Nonnull
    public static <T> WarningVal<T> from(@Nonnull final T value) {
        return new WarningVal<>(value, Collections.emptyList());
    }

    /**
     * Create a warning value with a collection (possibly empty) of problems.
     *
     * @param value value to store in the warning instance.
     * @param problems collection of problems associated with the warning object.
     * @param <T> value type
     * @return the warning
     */
    @Nonnull
    public static <T> WarningVal<T> from(
            @Nonnull final T value,
            @Nonnull final ProblemContainer problems
    ) {
        return new WarningVal<>(value, Ret.joinRetProblems(problems));
    }


    /**
     * Get the underlying value object.
     *
     * @return the value
     */
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
        return Ret.enforceHasProblems(this.problems);
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
