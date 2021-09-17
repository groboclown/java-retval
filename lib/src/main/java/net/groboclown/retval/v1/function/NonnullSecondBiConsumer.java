// Released under the MIT License. 
package net.groboclown.retval.v1.function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@FunctionalInterface
public interface NonnullSecondBiConsumer<T, V> {
    void accept(@Nullable T first, @Nonnull V second);
}
