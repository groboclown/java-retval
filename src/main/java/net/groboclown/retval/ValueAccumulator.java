// Released under the MIT License. 
package net.groboclown.retval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import net.groboclown.retval.function.NonnullReturnFunction;
import net.groboclown.retval.impl.CollectionUtil;

/**
 * Allows for accumulating a list of values, some of which may have errors associated with them.
 */
public class ValueAccumulator<T> implements ProblemContainer {
    private final List<Problem> problems = new ArrayList<>();
    private final List<T> values = new ArrayList<>();

    private ValueAccumulator() {
        // empty constructor
    }

    /**
     * Create a new, empty value accumulator instance.
     *
     * @param <T> value type
     * @return new instance.
     */
    @Nonnull
    public static <T> ValueAccumulator<T> from() {
        return new ValueAccumulator<>();
    }

    /**
     * Create a new value accumulator that starts with the given value or problems.
     *
     * @param value value or problem.
     * @param <T> type of the value
     * @return new instance.
     */
    @Nonnull
    public static <T> ValueAccumulator<T> from(@Nonnull final RetVal<T> value) {
        return new ValueAccumulator<T>().with(value);
    }

    /**
     * Create a new value accumulator that starts with the given value or problems.
     *
     * @param value value or problem.
     * @param <T> type of the value
     * @return new instance.
     */
    @Nonnull
    public static <T> ValueAccumulator<T> from(@Nonnull final RetNullable<T> value) {
        return new ValueAccumulator<T>().with(value);
    }


    /**
     * Create a new value accumulator that starts with the given problem(s).
     *
     * @param problem a problem.
     * @param problems optional additional problems.
     * @param <T> type of the value
     * @return new instance.
     */
    @Nonnull
    public static <T> ValueAccumulator<T> from(
            @Nonnull final Problem problem,
            @Nonnull final Problem... problems
    ) {
        return new ValueAccumulator<T>().with(problem, problems);
    }

    /**
     * Add the given value or problems to this accumulator.
     *
     * @param value value or problems
     * @return this instance
     */
    @Nonnull
    public ValueAccumulator<T> with(@Nonnull final RetVal<T> value) {
        this.problems.addAll(value.anyProblems());
        if (value.isOk()) {
            this.values.add(value.result());
        }
        return this;
    }

    /**
     * Add the given value or problems to this accumulator.
     *
     * @param value value or problems
     * @return this instance
     */
    @Nonnull
    public ValueAccumulator<T> with(@Nonnull final RetNullable<T> value) {
        this.problems.addAll(value.anyProblems());
        if (value.isOk()) {
            this.values.add(value.result());
        }
        return this;
    }

    /**
     * Add the given possible problems to the accumulator.
     *
     * @param problem a problem.
     * @param problems optional additional problems.
     * @return this instance
     */
    public ValueAccumulator<T> with(
            @Nonnull final Problem problem,
            @Nonnull final Problem... problems
    ) {
        this.problems.add(problem);
        this.problems.addAll(Arrays.asList(problems));
        return this;
    }

    /**
     * Add the given possible problems to the accumulator.
     *
     * @param problemSet a collection of problems.
     * @param problemSets vararg optional additional problems.
     * @return this instance
     */
    @SafeVarargs
    public final ValueAccumulator<T> with(
            @Nonnull final Collection<Problem> problemSet,
            @Nonnull final Collection<Problem>... problemSets
    ) {
        this.problems.addAll(Ret.joinProblemSets(problemSet, problemSets));
        return this;
    }

    /**
     * For each element in the input list, process it through the
     * function argument to transform into the accumulator type.
     *
     * @param input list of input values
     * @param func mapping function or validator
     * @param <V> input value type
     * @return this instance
     */
    public <V> ValueAccumulator<T> withEach(
            @Nonnull final Collection<V> input,
            @Nonnull final NonnullReturnFunction<V, RetVal<T>> func
    ) {
        input.forEach((v) -> with(func.apply(v)));
        return this;
    }

    /**
     * For each element in the input list, process it through the
     * function argument to transform into the accumulator type.  If the
     * input value has problems, then it is not processed.
     *
     * @param inputRes list of input values or problems
     * @param func mapping function or validator
     * @param <V> input value type
     * @return this instance
     */
    public <V> ValueAccumulator<T> withEach(
            @Nonnull final RetVal<? extends Collection<V>> inputRes,
            @Nonnull final NonnullReturnFunction<V, RetVal<T>> func
    ) {
        if (inputRes.hasProblems()) {
            return with(inputRes.anyProblems());
        }
        inputRes.result().forEach((v) -> with(func.apply(v)));
        return this;
    }

    /**
     * Get all valid values collected so far, even if there are also problems.
     *
     * @return read-only collection of problems.
     */
    @Nonnull
    public Collection<T> getValues() {
        // Note: cannot be a List.of, because of possible null values.
        return Collections.unmodifiableCollection(new ArrayList<>(this.values));
    }

    /**
     * Return a collection of the accumulated values, or the problems collected.
     *
     * <p>This is functionally identical to {@link #asRetVal()}, but logically means a
     * continuation of processing within the same call chain.
     *
     * @return the problems or values collected.
     */
    @Nonnull
    public RetVal<Collection<T>> then() {
        if (isOk()) {
            return RetVal.ok(getValues());
        }
        return RetVal.fromProblem(this.problems);
    }

    /**
     * Returns the accumulated values as a RetVal if there are no problems,
     * otherwise the enclosed problems.
     *
     * <p>This is functionally identical to {@link #then()}, but logically means a
     * conversion to a standard object and completing the call chain.
     *
     * @return a RetVal representation of this instance.
     */
    @Nonnull
    public RetVal<Collection<T>> asRetVal() {
        return then();
    }

    /**
     * Return this accumulator as a warning value, containing both
     * all collected values (up to this point) and discovered problems.
     *
     * @return warning version of the accumulator.
     */
    @Nonnull
    public WarningVal<Collection<T>> asWarning() {
        return WarningVal.from(this.getValues(), this);
    }

    @Override
    public boolean isProblem() {
        return ! this.problems.isEmpty();
    }

    @Override
    public boolean hasProblems() {
        return ! this.problems.isEmpty();
    }

    @Override
    public boolean isOk() {
        return this.problems.isEmpty();
    }

    @Nonnull
    @Override
    public Collection<Problem> anyProblems() {
        return CollectionUtil.copyNonNullValues(this.problems);
    }

    @Nonnull
    @Override
    public Collection<Problem> validProblems() {
        Ret.enforceHasProblems(this.problems);
        return CollectionUtil.copyNonNullValues(this.problems);
    }

    @Nonnull
    @Override
    public String debugProblems(@Nonnull final String joinedWith) {
        return Ret.joinProblemMessages(joinedWith, this.problems);
    }

    @Override
    public void joinProblemsWith(@Nonnull final Collection<Problem> problemList) {
        problemList.addAll(this.problems);
    }
}
