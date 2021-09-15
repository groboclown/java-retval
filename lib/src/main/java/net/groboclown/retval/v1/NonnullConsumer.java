// Released under the MIT License. 
package net.groboclown.retval.v1;

import javax.annotation.Nonnull;

/**
 * A version of {@link java.util.function.Consumer} but with an explicit nonnull
 * value.
 */
@FunctionalInterface
public interface NonnullConsumer<T> {
    void accept(@Nonnull T t);
}
