// Released under the MIT License. 
package net.groboclown.retval.function;

import javax.annotation.Nonnull;

/**
 * A version of the {@link java.util.function.Supplier} interface
 * with an explicit non-null return value.
 *
 * @param <T> return type
 */
@FunctionalInterface
public interface NonnullSupplier<T> {
    /**
     * Generates the return value.
     *
     * @return the non-null return value
     */
    @Nonnull
    T get();
}
