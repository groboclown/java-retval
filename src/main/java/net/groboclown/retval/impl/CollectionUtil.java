// Released under the MIT License. 
package net.groboclown.retval.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import net.groboclown.retval.Problem;

/**
 * Internal helper class for working with collections.
 */
public class CollectionUtil {
    private CollectionUtil() {
        // Utility class
    }

    /**
     * Performs a copy that enforces the values to be non-null.
     *
     * <p>This is very similar to the behavior of later JDKs List.copyOf()
     *
     * @param problems list of problems to copy and enforce non-null entries.
     * @return a copy of the parameter, explicitly as a List, and unmodifiable.
     */
    @Nonnull
    public static List<Problem> copyNonNullValues(@Nonnull final Collection<Problem> problems) {
        final List<Problem> ret = new ArrayList<>(problems.size());
        for (final Problem problem : problems) {
            ret.add(Objects.requireNonNull(problem));
        }
        return Collections.unmodifiableList(ret);
    }
}
