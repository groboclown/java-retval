// Released under the MIT License.
package net.groboclown.retval.v1;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * A standard problem that prevents a valid value from being returned.
 */
@Immutable
public interface Problem {
    @Nonnull
    String localMessage();
}
