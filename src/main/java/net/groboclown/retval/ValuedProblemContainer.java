// Released under the MIT License. 
package net.groboclown.retval;

import javax.annotation.Nullable;

/**
 * A generic type that contains a value and possible problems.  Useful for user that need to
 * take either {@link RetNullable}, {@link RetVal}, or {@link WarningVal}, without needing
 * to create implementations for each one.
 *
 * @param <T> type of the underlying value.
 */
public interface ValuedProblemContainer<T> extends ProblemContainer {
    /**
     * Get the underlying value object, regardless of the problem state of the
     * container.
     *
     * @return the value associated with this container.
     */
    @Nullable
    T getValue();
}
