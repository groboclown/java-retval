// Released under the MIT License. 
package net.groboclown.retval.v1;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@FunctionalInterface
public interface NonnullParamFunction<T, R> {
    @Nullable
    R apply(@Nonnull T value);
}
