// Released under the MIT License.
package net.groboclown.retval.v1;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * A standard problem that prevents a valid value from being returned.
 */
@Immutable
public interface Problem {
    /**
     * A human-consumable message describing the problem.
     *
     * @return the message, localized for the end-user.
     */
    @Nonnull
    String localMessage();
}
