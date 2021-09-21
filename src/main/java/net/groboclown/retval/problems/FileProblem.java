// Released under the MIT License.
package net.groboclown.retval.problems;

import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import net.groboclown.retval.SourcedProblem;

/**
 * A problem that comes from a known file.
 */
@Immutable
public class FileProblem implements SourcedProblem {
    private final String filePath;
    private final String localMessage;

    /**
     * Create the file problem from the file path and message.
     *
     * @param filePath path to the source that caused the problem.  In some cases,
     *                 this may be something other than a file, such as a URI.
     * @param localMessage informative user message about the problem.
     * @return the problem instance.
     */
    @Nonnull
    public static FileProblem from(
            @Nonnull final String filePath, @Nonnull final String localMessage
    ) {
        return new FileProblem(filePath, localMessage);
    }

    /**
     * Create a file problem from a file object and an I/O exception.
     *
     * @param file source file
     * @param ex exception generated from the operation.
     * @return the file problem object.
     */
    @Nonnull
    public static FileProblem from(@Nonnull final File file, @Nonnull final IOException ex) {
        return from(file.getPath(), ex);
    }

    /**
     * Create a file problem from a file object and an I/O exception.
     *
     * @param source source of the problem
     * @param ex exception generated from the operation.
     * @return the file problem object.
     */
    @Nonnull
    public static FileProblem from(@Nonnull final String source, @Nonnull final IOException ex) {
        return new FileProblem(
                source,
                UnhandledExceptionProblem.exceptionText(ex, source + " caused a problem")
        );
    }

    private FileProblem(@Nonnull final String filePath, @Nonnull final String localMessage) {
        this.filePath = filePath;
        this.localMessage = localMessage;
    }

    @Nonnull
    @Override
    public String localMessage() {
        return this.localMessage;
    }

    @Nonnull
    @Override
    public String getSource() {
        return this.filePath;
    }

    @Override
    public String toString() {
        return getSource() + ": " + localMessage();
    }
}
