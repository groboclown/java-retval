// Released under the MIT License. 
package net.groboclown.retval.v1.function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A version of the {@link java.util.function.Function} interface with
 * an explicitly non-null return value, but nullable parameter value.
 *
 * @param <T> value type
 * @param <R> return type
 */
@FunctionalInterface
public interface NonnullReturnFunction<T, R> {
    /**
     * Run the function.
     *
     * @param value nullable parameter value
     * @return non-null return value
     */
    @Nonnull
    R apply(@Nullable T value);
}
