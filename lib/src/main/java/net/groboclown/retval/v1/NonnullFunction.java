// Released under the MIT License. 
package net.groboclown.retval.v1;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface NonnullFunction<T, R> {
    @Nonnull
    R apply(@Nonnull T value);
}
