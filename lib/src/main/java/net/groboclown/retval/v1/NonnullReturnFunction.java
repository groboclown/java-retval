// Released under the MIT License. 
package net.groboclown.retval.v1;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@FunctionalInterface
public interface NonnullReturnFunction<T, R> {
    @Nonnull
    R apply(@Nullable T value);
}
