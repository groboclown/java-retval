// Released under the MIT License. 
package net.groboclown.retval.function;

import javax.annotation.Nonnull;

/**
 * A version of {@link java.util.function.Consumer} but with an explicit nonnull
 * value.
 */
@FunctionalInterface
public interface NonnullConsumer<T> {
    /**
     * Accept the value into the consumer.
     * The caller must assert the
     * non-null validity of the argument; the receiver should safely assume the
     * argument is non-null.
     *
     * @param t parameter value, which must be non-null
     */
    void accept(@Nonnull T t);
}
