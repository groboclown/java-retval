// Released under the MIT License. 
package net.groboclown.retval.function;

import javax.annotation.Nonnull;

/**
 * A version of {@link java.util.function.Function}
 * that explicitly returns a non-null value, and takes
 * a non-null parameter value.
 *
 * @param <T> argument type
 * @param <R> return type
 */
@FunctionalInterface
public interface NonnullFunction<T, R> {
    /**
     * Run the function.
     * The caller must assert the
     * non-null validity of the argument; the receiver should safely assume the
     * argument is non-null.
     *
     * @param value non-null parameter
     * @return non-null value
     */
    @Nonnull
    R apply(@Nonnull T value);
}
