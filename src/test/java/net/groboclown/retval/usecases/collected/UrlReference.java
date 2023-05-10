// Released under the MIT License.
package net.groboclown.retval.usecases.collected;

import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.groboclown.retval.ProblemCollector;
import net.groboclown.retval.Ret;
import net.groboclown.retval.RetVal;

/**
 * Example code where a builder model would be too much.
 */
public class UrlReference {
    private static final String DEFAULT_SCHEMA = "http";
    private static final String DEFAULT_PATH = "";

    private final String schemaRef;
    private final String hostRef;
    private final String portRef;
    private final String pathRef;

    /**
     * URL built by resource references.
     *
     * @param schemaRef name of the schema resource
     * @param hostRef name of the host resource
     * @param portRef name of the port resource
     * @param pathRef name of the path resource
     */
    public UrlReference(
            @Nullable final String schemaRef,
            @Nonnull final String hostRef,
            @Nonnull final String portRef,
            @Nullable final String pathRef) {
        this.schemaRef = schemaRef;
        this.hostRef = Objects.requireNonNull(hostRef);
        this.portRef = Objects.requireNonNull(portRef);
        this.pathRef = pathRef;
    }

    /**
     * Convert the URL references into a proper URL.
     *
     * @param store resource store, where the references pull
     * @return the URL string or problems
     */
    @Nonnull
    public RetVal<String> toUrl(@Nonnull final ResourceStore store) {
        ProblemCollector problems = Ret.collectProblems();
        final Optional<String> schema = problems.collectOptional(
                store.getResource(this.schemaRef, String.class));
        final String host = problems.collect(
                store.requireResource(this.hostRef, String.class));
        final Integer port = problems.collect(
                store.requireResource(this.portRef, Integer.class));
        final Optional<String> path = problems.collectOptional(
                store.getResource(this.pathRef, String.class));

        return problems
            .thenValue(() ->
                    // Called only when the collection didn't encounter any errors.
                    schema.orElse(DEFAULT_SCHEMA)
                    + "://"
                    + Objects.requireNonNull(host)
                    + ":"
                    + Objects.requireNonNull(port)
                    + "/"
                    + path.orElse(DEFAULT_PATH)
            );
    }
}
