// Released under the MIT License. 
package net.groboclown.retval.function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A version of the {@link java.util.function.Function} interface
 * where the parameter is explicitly non-null, and the return value is
 * nullable.
 *
 * @param <T> parameter type
 * @param <R> return type
 */
@FunctionalInterface
public interface NonnullParamFunction<T, R> {
    /**
     * Runs the function.
     * The caller must assert the
     * non-null validity of the argument; the receiver should safely assume the
     * argument is non-null.
     *
     * @param value non-null parameter value
     * @return nullable return value
     */
    @Nullable
    R apply(@Nonnull T value);
}
