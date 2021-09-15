// Released under the MIT License.
package net.groboclown.retval.v1.problems;

import net.groboclown.retval.v1.SourcedProblem;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.io.File;
import java.io.IOException;

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
    public static FileProblem from(@Nonnull final String filePath, @Nonnull final String localMessage) {
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
        return new FileProblem(
                file.getPath(),
                ex.getLocalizedMessage() == null
                        ? (ex.getMessage() == null
                            ? (file.getPath() + " caused an error")
                            : ex.getMessage())
                        : ex.getLocalizedMessage()
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
}
