// Released under the MIT License. 
package net.groboclown.retval.v1.function;

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
     *
     * @param value non-null parameter
     * @return non-null value
     */
    @Nonnull
    R apply(@Nonnull T value);
}
