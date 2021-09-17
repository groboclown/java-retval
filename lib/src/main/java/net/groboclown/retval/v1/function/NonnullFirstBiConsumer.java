// Released under the MIT License. 
package net.groboclown.retval.v1.function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@FunctionalInterface
public interface NonnullFirstBiConsumer<T, V> {
    void accept(@Nonnull T first, @Nullable V second);
}
