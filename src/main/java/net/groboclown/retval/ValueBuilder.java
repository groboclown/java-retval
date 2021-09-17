// Released under the MIT License.
package net.groboclown.retval;

import java.util.Collection;
import java.util.Objects;
import javax.annotation.Nonnull;
import net.groboclown.retval.function.NonnullBiConsumer;
import net.groboclown.retval.function.NonnullConsumer;
import net.groboclown.retval.function.NonnullFirstBiConsumer;


/**
 * Contains a non-null value and accumulates problems as aspects of the value are updated.
 *
 * <p>This class is commonly used to track problems associated with the gradual
 * construction of a value, and keep track of all problems discovered.  Care must be taken to
 * ensure proper "then" logic if one value depends on another (say, to get the stored email,
 * the user ID must be known, but the user ID must be valid).
 */
public class ValueBuilder<T> implements ProblemContainer {
    // Developer note: this contains no generic "with(ProblemContainer)", because that
    // form can cause data values to be lost.

    private final ProblemCollector problems = ProblemCollector.from();
    private final T value;

    private ValueBuilder(@Nonnull final T value) {
        this.value = Objects.requireNonNull(value);
    }

    /**
     * Construct a new value builder for the given initial state of the value.
     *
     * @param value source value
     * @param <T> type of the value
     * @return the new builder.
     */
    @Nonnull
    public static <T> ValueBuilder<T> from(@Nonnull final T value) {
        return new ValueBuilder<>(Objects.requireNonNull(value));
    }


    @Nonnull
    public T getValue() {
        return this.value;
    }


    @Nonnull
    public RetVal<T> then() {
        return this.problems.complete(this.value);
    }


    @Nonnull
    public WarningVal<T> asWarning() {
        return this.problems.warn(this.value);
    }

    /**
     * Calls the consumer with arguments <code>(getValue(), arg.result())</code> if
     * {@literal arg} has no problems.  This ignores the current problem state of the
     * builder.
     *
     * <p>Used to set or update the value stored by this builder based on a problematic
     * value.
     *
     * @param arg a new parameter to include in the built value.
     * @param consumer updates the built value with the value of the parameter.
     * @param <V> type of the argument
     * @return this builder
     */
    @Nonnull
    public <V> ValueBuilder<T> with(
            @Nonnull final RetVal<V> arg,
            @Nonnull final NonnullBiConsumer<T, V> consumer) {
        this.problems.with(arg);
        if (arg.isOk()) {
            consumer.accept(this.value, arg.result());
        }
        return this;
    }

    /**
     * Calls the consumer with arguments <code>(getValue(), arg.result())</code> if
     * {@literal arg} has no problems.  This ignores the current problem state of the
     * builder.
     *
     * <p>Used to set or update the value stored by this builder based on a problematic
     * value.
     *
     * @param arg a new parameter to include in the built value.
     * @param consumer updates the built value with the value of the parameter.
     * @param <V> type of the argument
     * @return this builder
     */
    @Nonnull
    public <V> ValueBuilder<T> with(
            @Nonnull final RetNullable<V> arg,
            @Nonnull final NonnullFirstBiConsumer<T, V> consumer) {
        this.problems.with(arg);
        if (arg.isOk()) {
            consumer.accept(this.value, arg.result());
        }
        return this;
    }

    /**
     * Join any problems in the argument with this builder.
     *
     * <p>This commonly has use when another process builds values that are not directly
     * set in the caller.  For example, if the built value has a final value that is populated
     * elsewhere, its problems will need to be included here.
     *
     * @param problems possible problem-contained non-value.
     * @return this instance.
     */
    @Nonnull
    public ValueBuilder<T> with(@Nonnull final RetVoid problems) {
        this.problems.with(problems);
        return this;
    }

    /**
     * Call the consumer with the current value; commonly used to set a single, no-problem
     * attribute on the value.  Provided for symmetry with the other "with" calls.
     *
     * <p>A common use case is to have the builder method return the value builder without
     * intermediate steps.  This method allows for chained calls to flow fluently:
     * <pre>
     *     RetVal&lt;StringBuilder&gt; create() {
     *         return ValueBuilder.from(new StringBuilder("Name: "))
     *              .with(discoverName(), StringBuilder::append)
     *              .withValue((sb) -&gt; sb.append("; Age: "))
     *              .with(discoverAge(), StringBuilder::append)
     *              .then();
     *     }
     * </pre>
     *
     * @param consumer accepts the value
     * @return this instance.
     */
    @Nonnull
    public ValueBuilder<T> withValue(
            @Nonnull final NonnullConsumer<T> consumer
    ) {
        consumer.accept(this.value);
        return this;
    }

    @Nonnull
    public ProblemCollector getCollector() {
        return this.problems;
    }


    @Override
    public boolean isProblem() {
        return this.problems.isProblem();
    }

    @Override
    public boolean hasProblems() {
        return this.problems.hasProblems();
    }

    @Override
    public boolean isOk() {
        return this.problems.isOk();
    }

    @Nonnull
    @Override
    public Collection<Problem> anyProblems() {
        return this.problems.anyProblems();
    }

    @Nonnull
    @Override
    public Collection<Problem> validProblems() {
        return this.problems.validProblems();
    }

    @Nonnull
    @Override
    public String debugProblems(@Nonnull final String joinedWith) {
        return this.problems.debugProblems(joinedWith);
    }
}
