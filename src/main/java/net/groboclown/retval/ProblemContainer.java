// Released under the MIT License. package net.groboclown.retval.v1;
package net.groboclown.retval;

import java.util.Collection;
import javax.annotation.Nonnull;

/**
 * An object that contains problems.
 *
 * <p>Some implementations may be immutable, while others aren't.
 */
public interface ProblemContainer {
    /**
     * Returns whether this object contains 1 or more problems.
     *
     * @return true if there is a problem, false if there is no problem.
     */
    boolean isProblem();

    /**
     * Returns whether this object contains 1 or more problems.
     * Duplicate of {@link #isProblem()} for English readability.
     *
     * @return true if there is a problem, false if there is no problem.
     */
    boolean hasProblems();

    /**
     * Returns whether this object contains zero problems.
     *
     * @return true if there are no problems, false if there is a problem.
     */
    boolean isOk();

    /**
     * Return all the problems in this object, even if the container is "ok".
     *
     * <p>Generally, this combines the problems in this instance with a larger collection,
     * which can itself be used to check if any of the values had problems.
     *
     * <p>The returned collection is read-only, and contains no null values.
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
     * <p>The returned collection is read-only, and contains no null values.
     *
     * @return the problems in this container, and the collection will contain at least 1 item.
     * @throws IllegalStateException if this object has 0 problems.
     */
    @Nonnull
    Collection<Problem> validProblems();

    /**
     * Return the problems as a single string, which combines the {@link Object#toString()}
     * output of each problem with the given joining string parameter.  If this object contains
     * no problems, then an empty string is returned instead.
     *
     * @param joinedWith the text to join multiple problem strings together.
     * @return the combined text of the problems, or an empty string if there are no problems.
     */
    @Nonnull
    String debugProblems(@Nonnull String joinedWith);

    /**
     * Add all problems in this container into the argument.  This has a very specific
     * usage to indicate that this container, even if it has no problems, is part of a
     * bigger issue.  Therefore, this is fine to call even if there are no problems in
     * this container.
     *
     * @param problemList a modifiable collection of zero or more problems.
     */
    void joinProblemsWith(@Nonnull Collection<Problem> problemList);
}
