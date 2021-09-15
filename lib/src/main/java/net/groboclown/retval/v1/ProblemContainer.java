// Released under the MIT License. package net.groboclown.retval.v1;
package net.groboclown.retval.v1;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface ProblemContainer {
    /**
     * Does this object contain 1 or more problems?
     *
     * @return true if there is a problem, false if there is no problem.
     */
    boolean isProblem();

    /**
     * Does this object contain 1 or more problems?
     * Duplicate of {@link #isProblem()} for English readability.
     *
     * @return true if there is a problem, false if there is no problem.
     */
    boolean hasProblems();

    /**
     * Does this object contain zero problems?
     *
     * @return true if there are no problems, false if there is a problem.
     */
    boolean isOk();

    /**
     * Return all the problems in this object, even if the container is "ok".
     *
     * @return the problems contained in this container, even if there are none.
     */
    @Nonnull
    Collection<Problem> anyProblems();

    /**
     * Returns all contained problems in this object, but only if this object
     * contains 1 or more problems.  If it contains 0, then this throws an
     * {@link IllegalStateException}.
     *
     * @return the problems in this container, and the collection will contain at least 1 item.
     * @throws IllegalStateException if this object has 0 problems.
     */
    @Nonnull
    Collection<Problem> validProblems();
}
