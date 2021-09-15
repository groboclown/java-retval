// Released under the MIT License. 
package net.groboclown.retval.v1.problems;

import net.groboclown.retval.v1.Problem;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

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
        return new LocalizedProblem(localizedMessage);
    }

    private LocalizedProblem(String localized) {
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
}
