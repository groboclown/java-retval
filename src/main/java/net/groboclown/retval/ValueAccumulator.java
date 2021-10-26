// Released under the MIT License. 
package net.groboclown.retval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import net.groboclown.retval.function.NonnullReturnFunction;


/**
 * Allows for accumulating a list of values, some of which may have errors associated with them.
 */
public class ValueAccumulator<T> implements ProblemContainer {
    private final ProblemCollector problems = ProblemCollector.from();
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
     * Get the underlying collector object, for adding arbitrary problems to the accumulator.
     *
     * @return the underlying problem collector object.
     * @since 2.1
     */
    @Nonnull
    public ProblemCollector getCollector() {
        return this.problems;
    }

    /**
     * Add the given value or problems to this accumulator.
     *
     * @param value value or problems
     * @return this instance
     */
    @Nonnull
    public ValueAccumulator<T> with(@Nonnull final RetVal<T> value) {
        this.problems.withProblems(value.anyProblems());
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
    @Nonnull
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
    @Nonnull
    public final ValueAccumulator<T> with(
            @Nonnull final Collection<Problem> problemSet,
            @Nonnull final Collection<Problem>... problemSets
    ) {
        this.problems.addAll(Ret.joinProblemSets(problemSet, problemSets));
        return this;
    }

    /**
     * Add vararg list of values into this accumulator.
     *
     * @param values list of values to add into the internal accumulated list.
     * @return this accumulator
     * @since 2.1
     * @throws NullPointerException if the value array is null.
     */
    @SafeVarargs
    @Nonnull
    public final ValueAccumulator<T> withAllValues(final T... values) {
        this.values.addAll(Arrays.asList(Objects.requireNonNull(values, "values array")));
        return this;
    }

    /**
     * Add vararg list of values into this accumulator.
     *
     * @param values list of values to add into the internal accumulated list.
     * @return this accumulator
     * @since 2.1
     */
    @SafeVarargs
    @Nonnull
    public final ValueAccumulator<T> withAllValues(@Nonnull final Iterable<T>... values) {
        for (final Iterable<T> valueList : values) {
            valueList.forEach(this.values::add);
        }
        return this;
    }

    /**
     * Add a value into this accumulator if the return value is ok, otherwise add the
     * problems.
     *
     * @param res value to add into the internal accumulated list.
     * @since 2.1
     */
    public void add(@Nonnull final RetVal<T> res) {
        this.problems.addAll(res.anyProblems());
        if (res.isOk()) {
            this.values.add(res.result());
        }
    }

    /**
     * Add a value into this accumulator if the return value is ok, otherwise add the
     * problems.
     *
     * @param res value to add into the internal accumulated list.
     * @since 2.1
     */
    public void add(@Nonnull final RetNullable<T> res) {
        this.problems.addAll(res.anyProblems());
        if (res.isOk()) {
            this.values.add(res.result());
        }
    }

    /**
     * Add a value into this accumulator if the return value is ok, otherwise add the
     * problems.
     *
     * @param res value to add into the internal accumulated list.
     * @since 2.1
     */
    public void add(@Nonnull final ValuedProblemContainer<T> res) {
        this.problems.addAll(res.anyProblems());
        if (res.isOk()) {
            this.values.add(res.getValue());
        }
    }

    /**
     * Add a list of values into this accumulator.  If a value has problems, then the problems
     * are added; if the value has no problems, then the value is added.
     *
     * <p>This will work unexpectedly with
     * {@link WarningVal} objects; the value will only be added if no problems are in the
     * warning value.
     *
     * @param values list of values to add into the internal accumulated list.
     * @since 2.1
     */
    public void addAll(@Nonnull final Iterable<ValuedProblemContainer<T>> values) {
        for (final ValuedProblemContainer<T> res : values) {
            add(res);
        }
    }

    /**
     * Add vararg list of values into this accumulator.
     *
     * @param value value to add into the internal accumulated list.
     * @since 2.1
     */
    public void addValue(final T value) {
        this.values.add(value);
    }

    /**
     * Add vararg list of values into this accumulator.
     *
     * @param values list of values to add into the internal accumulated list.
     * @since 2.1
     */
    public void addAllValues(@Nonnull final Iterable<T> values) {
        values.forEach(this.values::add);
    }

    /**
     * If the argument is okay, then all the entries in the value are added into this
     * accumulator.  Otherwise, its problems are added into this accumulator.
     *
     * @param value RetVal instance with a collection of values to accumulate.
     * @return this instance
     * @since 2.1
     */
    @Nonnull
    public ValueAccumulator<T> withAll(@Nonnull final RetVal<? extends Iterable<T>> value) {
        this.problems.addAll(value.anyProblems());
        if (value.isOk()) {
            value.result().forEach(this.values::add);
        }
        return this;
    }

    /**
     * If the argument is okay, then all the entries in the value are added into this
     * accumulator (but only if the value is non-null).  Otherwise, its problems are added
     * into this accumulator.  An underlying null result is ignored.
     *
     * @param value RetNullable instance with a collection of values to accumulate.
     * @return this instance
     * @since 2.1
     */
    @Nonnull
    public ValueAccumulator<T> withAll(@Nonnull final RetNullable<? extends Iterable<T>> value) {
        this.problems.addAll(value.anyProblems());
        if (value.isOk()) {
            final Iterable<T> entries = value.result();
            if (entries != null) {
                entries.forEach(this.values::add);
            }
        }
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
     * Get all valid values collected so far, even if there are also problems.  Null values
     * may be included in the result if null values were added from a {@link RetNullable}
     * or transformed function.
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
        return RetVal.fromProblems(this.problems);
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
     * Return the value as an unmodifiable List, rather than as a generic Collection.
     *
     * @return a RetVal version of the instance with an underlying List implementation.
     * @since 2.1
     */
    @Nonnull
    public RetVal<List<T>> asRetValList() {
        if (isOk()) {
            return RetVal.ok(Collections.unmodifiableList(new ArrayList<>(this.values)));
        }
        return RetVal.fromProblems(this.problems);
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

    @Override
    public void joinProblemsWith(@Nonnull final Collection<Problem> problemList) {
        this.problems.joinProblemsWith(problemList);
    }
}
