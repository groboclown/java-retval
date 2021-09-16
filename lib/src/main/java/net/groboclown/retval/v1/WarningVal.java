// Released under the MIT License.
package net.groboclown.retval.v1;

import java.util.Collection;
import java.util.Objects;
import javax.annotation.Nonnull;


/**
 * A non-null value container that requires a non-null value and can have problems.
 */
public class WarningVal<T> implements ProblemContainer {
    private final RetCollector retCollector = RetCollector.from();
    private final T value;

    private WarningVal(@Nonnull final T value) {
        this.value = value;
    }

    @Nonnull
    public static <T> WarningVal<T> from(@Nonnull final T value) {
        return new WarningVal<>(Objects.requireNonNull(value));
    }

    @Override
    public boolean isProblem() {
        return this.retCollector.isProblem();
    }

    @Override
    public boolean hasProblems() {
        return this.retCollector.hasProblems();
    }

    @Override
    public boolean isOk() {
        return this.retCollector.isOk();
    }

    @Nonnull
    @Override
    public Collection<Problem> anyProblems() {
        return this.retCollector.anyProblems();
    }

    @Nonnull
    @Override
    public Collection<Problem> validProblems() {
        return this.retCollector.validProblems();
    }

    @Nonnull
    @Override
    public String debugProblems(@Nonnull final String joinedWith) {
        return this.retCollector.debugProblems(joinedWith);
    }

    @Nonnull
    public T getValue() {
        return this.value;
    }

    @Nonnull
    public RetCollector getCollector() {
        return this.retCollector;
    }
}
