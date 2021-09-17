// Released under the MIT License. 
package net.groboclown.retval.problems;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import net.groboclown.retval.Problem;

/**
 * Problem with a simple, localized message text.
 */
@Immutable
public class LocalizedProblem implements Problem {
    private final String localized;

    /**
     * Create a problem from a localized string.
     *
     * @param localizedMessage message text for the problem.
     * @return the problem value.
     */
    public static LocalizedProblem from(@Nonnull final String localizedMessage) {
        return new LocalizedProblem(Objects.requireNonNull(localizedMessage));
    }

    private LocalizedProblem(@Nonnull final String localized) {
        this.localized = localized;
    }

    @Nonnull
    @Override
    public String localMessage() {
        return this.localized;
    }

    @Override
    public String toString() {
        return localMessage();
    }

    @Override
    public int hashCode() {
        return this.localized.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (LocalizedProblem.class.equals(other.getClass())) {
            return this.localized.equals(((LocalizedProblem) other).localized);
        }
        return false;
    }
}
