// Released under the MIT License. 
package net.groboclown.retval.v1.function;

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
     *
     * @param value non-null parameter value
     * @return nullable return value
     */
    @Nullable
    R apply(@Nonnull T value);
}
