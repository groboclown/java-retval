// Released under the MIT License. 
package net.groboclown.retval.function;

import javax.annotation.Nonnull;

/**
 * An equivalent to {@link java.util.function.BiConsumer}, where the
 * parameters are non-null.
 *
 * @param <T> first parameter type
 * @param <V> second parameter type
 */
@FunctionalInterface
public interface NonnullBiConsumer<T, V> {
    /**
     * Accept the two values into the consumer.  The caller must assert the
     * non-null validity of the arguments; the receiver should safely assume the
     * arguments are non-null.
     *
     * @param first first parameter value.
     * @param second second parameter value
     */
    void accept(@Nonnull T first, @Nonnull V second);
}
