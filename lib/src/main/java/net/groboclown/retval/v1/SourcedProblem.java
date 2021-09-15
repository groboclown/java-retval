// Released under the MIT License.
package net.groboclown.retval.v1;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * A problem that includes a known source.
 */
@Immutable
public interface SourcedProblem extends Problem {
    /**
     * A single source of the problem.
     *
     * @return the source of the problem.
     */
    @Nonnull
    String getSource();
}
