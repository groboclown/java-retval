// Released under the MIT License. 
package net.groboclown.retval.problems;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import net.groboclown.retval.Problem;

/**
 * A problem that comes from an exception that wasn't properly handled.
 */
@Immutable
public class UnhandledExceptionProblem implements Problem {
    // The exception is maintained primarily for developers.
    private final Throwable source;

    /**
     * Create an {@link UnhandledExceptionProblem} from an exception.
     *
     * @param source source exception.
     * @return the exception wrapped in a problem.
     */
    @Nonnull
    public static UnhandledExceptionProblem wrap(@Nonnull final Throwable source) {
        return new UnhandledExceptionProblem(source);
    }

    private UnhandledExceptionProblem(@Nonnull final Throwable source) {
        this.source = source;
    }

    @Nonnull
    @Override
    public String localMessage() {
        return exceptionText(this.source, this.source.getClass().getName());
    }

    /**
     * Get the exception that caused this problem.
     *
     * @return source exception
     */
    @Nonnull
    public Throwable getSourceException() {
        return this.source;
    }

    @Override
    public String toString() {
        return localMessage();
    }

    @Nonnull
    static String exceptionText(@Nonnull final Throwable ex, @Nonnull final String defaultText) {
        return ex.getLocalizedMessage() == null
                ? (
                    ex.getMessage() == null
                    ? defaultText
                    : ex.getMessage()
                )
                : ex.getLocalizedMessage();
    }
}
