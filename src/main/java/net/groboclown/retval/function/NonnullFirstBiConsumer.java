// Released under the MIT License. 
package net.groboclown.retval.function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An equivalent to {@link java.util.function.BiConsumer}, where the
 * first argument is non-null.
 *
 * @param <T> first argument value type
 * @param <V> second argument value type
 */
@FunctionalInterface
public interface NonnullFirstBiConsumer<T, V> {
    /**
     * The caller must assert the
     * non-null validity of the arguments; the receiver should safely assume the
     * argument is non-null.
     *
     * @param first first argument, which must be non-null
     * @param second second argument.
     */
    void accept(@Nonnull T first, @Nullable V second);
}
