// Released under the MIT License. 
package net.groboclown.retval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.groboclown.retval.function.NonnullConsumer;
import net.groboclown.retval.function.NonnullSupplier;
import net.groboclown.retval.impl.CollectionUtil;

/**
 * Collects problems from multiple requests to gather data.
 *
 * <p>This has subtle differences in usage with the {@link ValueBuilder}.  The builder collects
 * information about one non-null value under construction, while this collects problems
 * for one or more values that generally take all arguments in a constructor.
 */
public class ProblemCollector implements ProblemContainer {
    private final List<Problem> problems = new ArrayList<>();

    private ProblemCollector() {
        // Use static constructors.
    }

    /**
     * Create an empty problem collector.
     *
     * @return an empty problem collector.
     */
    @Nonnull
    public static ProblemCollector from() {
        return new ProblemCollector();
    }


    /**
     * Populate this collector from lists of problems.
     *
     * @param problems list of problems.
     * @param problemLists vararg optional lists of problems.
     * @return a problem collector containing all the problems.
     */
    @SafeVarargs
    @Nonnull
    public static ProblemCollector from(
            @Nonnull final List<Problem> problems,
            @Nonnull final List<Problem>... problemLists
    ) {
        return new ProblemCollector().withProblems(problems, problemLists);
    }


    /**
     * Populate this collector from problems.
     *
     * @param problem problem.
     * @param problems vararg optional problems.
     * @return a problem collector containing all the problems.
     */
    @Nonnull
    public static ProblemCollector from(
            @Nonnull final Problem problem,
            @Nonnull final Problem... problems
    ) {
        return new ProblemCollector().withProblem(problem, problems);
    }

    /**
     * Create a new RetCollector and invoke {@link #with(RetVal, NonnullConsumer)}.
     *
     * @param value problems to include in the collection.
     * @param values vararg optional additional values.
     * @return a new collector
     */
    @Nonnull
    public static ProblemCollector from(
            @Nonnull final RetVoid value,
            final RetVoid... values
    ) {
        return new ProblemCollector().withProblems(
                Ret.joinRetProblems(value, values)
        );
    }

    /**
     * Create a new RetCollector and invoke {@link #with(RetVal, NonnullConsumer)}.
     *
     * @param value value to pass to the setter, or problems to include in the collection.
     * @param setter passed the value.
     * @param <T> type of the value
     * @return a new collector
     */
    @Nonnull
    public static <T> ProblemCollector from(
            @Nonnull final RetVal<T> value,
            @Nonnull final NonnullConsumer<T> setter
    ) {
        return new ProblemCollector().with(value, setter);
    }

    /**
     * Create a new RetCollector and invoke {@link #with(RetNullable, Consumer)}.
     *
     * @param value value to pass to the setter, or problems to include in the collection.
     * @param setter passed the value.
     * @param <T> type of the value
     * @return a new collector
     */
    @Nonnull
    public static <T> ProblemCollector from(
            @Nonnull final RetNullable<T> value,
            @Nonnull final Consumer<T> setter
    ) {
        return new ProblemCollector().with(value, setter);
    }

    /**
     * Adds a single problem to the collection.  Reflects a Collection "add" call.
     *
     * @param problem problem object
     * @since 2.1
     */
    public void add(@Nonnull final Problem problem) {
        this.problems.add(Objects.requireNonNull(problem));
    }

    /**
     * Adds a single problem to the collection.  Reflects a Collection "add" call.
     *
     * @param problems problem object
     * @since 2.1
     */
    public void addAll(@Nonnull final Iterable<Problem> problems) {
        for (final Problem problem : problems) {
            this.problems.add(Objects.requireNonNull(problem));
        }
    }

    /**
     * Adds all the problems in the argument into this collector.
     *
     * @param problem a problem to add
     * @param problems optional additional problems.
     * @return this instance
     */
    @Nonnull
    public ProblemCollector withProblem(
            @Nonnull final Problem problem,
            @Nonnull final Problem... problems) {
        this.problems.addAll(Ret.joinProblems(problem, problems));
        return this;
    }

    /**
     * Adds all the problems in the argument into this collector.
     *
     * @param problems problem containers
     * @param problemList optional additional problem containers.
     * @return this instance
     */
    @SafeVarargs
    @Nonnull
    public final ProblemCollector withProblems(
            @Nonnull final Collection<Problem> problems,
            @Nonnull final Collection<Problem>... problemList) {
        this.problems.addAll(Ret.joinProblemSets(problems, problemList));
        return this;
    }

    /**
     * If the value has problems, load them into this collector,
     * otherwise pass the value as an argument to the setter.
     *
     * @param value non-null value
     * @param setter function that handles the non-null value if it has no problems.
     * @param <T> type of the value
     * @return this collector
     */
    @Nonnull
    public <T> ProblemCollector with(
            @Nonnull final RetVal<T> value,
            @Nonnull final NonnullConsumer<T> setter
    ) {
        // This method doesn't call joinProblems, because the error state and value are
        // pulled in.
        this.problems.addAll(value.anyProblems());
        if (value.isOk()) {
            setter.accept(value.result());
        }
        return this;
    }

    /**
     * If the value has problems, load them into this collector,
     * otherwise pass the value as an argument to the setter.
     *
     * @param value nullable value
     * @param setter function that handles the nullable value if it has no problems.
     * @param <T> type of the value
     * @return this collector
     */
    @Nonnull
    public <T> ProblemCollector with(
            @Nonnull final RetNullable<T> value,
            @Nonnull final Consumer<T> setter
    ) {
        // This method doesn't call joinProblems, because the error state and value are
        // pulled in.
        this.problems.addAll(value.anyProblems());
        if (value.isOk()) {
            setter.accept(value.result());
        }
        return this;
    }

    /**
     * If the value has problems, load them into this collector.  Always pass the value
     * to the setter.
     *
     * @param value nullable value
     * @param setter function that handles the nullable value if it has no problems.
     * @param <T> type of the value
     * @return this collector
     * @since 2.1
     */
    @Nonnull
    public <T> ProblemCollector with(
            @Nonnull final WarningVal<T> value,
            @Nonnull final NonnullConsumer<T> setter
    ) {
        // This method doesn't call joinProblems, because the error state and value are
        // pulled in.
        this.problems.addAll(value.anyProblems());
        setter.accept(value.getValue());
        return this;
    }

    /**
     * Add in any problems from the value into this collector.
     *
     * @param value value that may have problems.
     * @return this collector.
     */
    @Nonnull
    public ProblemCollector with(@Nonnull final RetVoid value) {
        this.problems.addAll(value.anyProblems());
        return this;
    }

    /**
     * If the value has problems, load them into this collector,
     * otherwise pass the value as an argument to the setter.
     *
     * @param value nullable value
     * @param setter function that handles the nullable value if it has no problems.
     * @param <T> type of the value
     * @return this collector
     * @since 2.1
     */
    @Nonnull
    public <T> ProblemCollector withValuedProblemContainer(
            @Nonnull final ValuedProblemContainer<T> value,
            @Nonnull final Consumer<T> setter
    ) {
        // This method doesn't call joinProblems, because the error state and value are
        // pulled in.
        this.problems.addAll(value.anyProblems());
        if (value.isOk()) {
            setter.accept(value.getValue());
        }
        return this;
    }

    /**
     * Add into this collector any problems from each of the values.
     *
     * @param values collection of values that may have problems.
     * @return this collector.
     * @since 2.1
     */
    @Nonnull
    public ProblemCollector withAll(@Nonnull final RetVoid... values) {
        for (final RetVoid value : values) {
            this.problems.addAll(value.anyProblems());
        }
        return this;
    }

    /**
     * Add into this collector any problems from each of the values.
     *
     * @param values collection of values that may have problems.
     * @return this collector.
     * @since 2.1
     */
    @Nonnull
    public ProblemCollector withAll(@Nonnull final Iterable<RetVoid> values) {
        for (final RetVoid value : values) {
            this.problems.addAll(value.anyProblems());
        }
        return this;
    }

    /**
     * For each item in the {@literal values} collection, the function is
     * called, giving it a chance to return problems associated with the
     * value.  The function may return null to indicate no problems.
     *
     * <p>This function does not have a requirement on nullability, as that is
     * entirely within the domain of the caller to track.
     *
     * @param values collection of values to call into the validation function.  This will be
     *               done for each item, irrespective of the active problem state.
     * @param validateFunc function called for each entry to validate.
     * @param <T> collection value type
     * @return this instance.
     */
    @Nonnull
    public <T> ProblemCollector validateEach(
            @Nonnull final Collection<T> values,
            @Nonnull final Function<T, ProblemContainer> validateFunc
    ) {
        for (final T value : values) {
            final ProblemContainer res = validateFunc.apply(value);
            if (res != null) {
                res.joinProblemsWith(this.problems);
            }
        }
        return this;
    }

    /**
     * Collect the problems in the argument into this problem collector,
     * and pass-through the argument's stored value.
     *
     * <p>Allows for collecting values without needing the builder pattern.
     * It strips off {@literal null} safety.
     *
     * <p>This counts as an observation on the passed-in value.
     *
     * @param value problem-wrapped value
     * @param <T> collection type value
     * @return the value contained; possibly null if it contains a null
     *      value, or if the container has problems.
     * @since 2.4
     */
    @Nullable
    public <T> T collect(@Nonnull final ValuedProblemContainer<T> value) {
        // Collection counts as an observation.  To ensure this counts as an
        // observation, perform explicit validation checking.
        if (value.isProblem()) {
            addAll(value.validProblems());
        }
        return value.getValue();
    }

    /**
     * Collect the problems in the argument into this problem collector,
     * and pass-through the argument's stored value as an Optional type.
     *
     * <p>Allows for collecting values without needing the builder pattern.
     * It strips off {@literal null} safety knowledge and replaces it with
     * Optional's null checking.
     *
     * <p>This counts as an observation on the passed-in value.
     *
     * @param value problem-wrapped value
     * @param <T> collection type value
     * @return the value contained; possibly null if it contains a null
     *      value, or if the container has problems.
     * @since 2.4
     */
    @Nonnull
    public <T> Optional<T> collectOptional(@Nonnull final ValuedProblemContainer<T> value) {
        // Collection counts as an observation.  To ensure this counts as an
        // observation, perform explicit validation checking.
        if (value.isProblem()) {
            addAll(value.validProblems());
        }
        return Optional.ofNullable(value.getValue());
    }

    /**
     * Generate a non-null value if this collector contains no
     * problems, otherwise return the problems.
     *
     * @param getter the non-null value generator
     * @param <T> type of the value
     * @return the non-null value or problems
     */
    @Nonnull
    public <T> RetVal<T> then(@Nonnull final NonnullSupplier<RetVal<T>> getter) {
        if (this.problems.isEmpty()) {
            return getter.get();
        }
        return RetVal.fromProblem(this.problems);
    }

    /**
     * Generate a non-null value if this collector contains no
     * problems, otherwise return the problems.
     *
     * @param getter the non-null value generator
     * @param <T> type of the value
     * @return the non-null value or problems
     */
    @Nonnull
    public <T> RetVal<T> thenValue(@Nonnull final NonnullSupplier<T> getter) {
        if (this.problems.isEmpty()) {
            return RetVal.ok(getter.get());
        }
        return RetVal.fromProblem(this.problems);
    }

    /**
     * Generate a nullable value if this collector contains no
     * problems, otherwise return the problems.
     *
     * @param getter the nullable value generator
     * @param <T> type of the value
     * @return the nullable value or problems
     */
    @Nonnull
    public <T> RetNullable<T> thenNullable(
            @Nonnull final NonnullSupplier<RetNullable<T>> getter
    ) {
        if (this.problems.isEmpty()) {
            return getter.get();
        }
        return RetNullable.fromProblem(this.problems);
    }

    /**
     * Generate a nullable value if this collector contains no
     * problems, otherwise return the problems.
     *
     * @param getter the nullable value generator
     * @param <T> type of the value
     * @return the nullable value or problems
     */
    @Nonnull
    public <T> RetNullable<T> thenNullableValue(@Nonnull final Supplier<T> getter) {
        if (this.problems.isEmpty()) {
            return RetNullable.ok(getter.get());
        }
        return RetNullable.fromProblem(this.problems);
    }

    /**
     * If this collector has no problems, then run the
     * runner and return a void value.  If this has problems,
     * then return the problems.
     *
     * @param runner runner to execute if there are no problems.
     * @return the problems, if any exist before running.
     */
    @Nonnull
    public RetVoid thenRun(@Nonnull final Runnable runner) {
        if (this.problems.isEmpty()) {
            runner.run();
            return RetVoid.ok();
        }
        return RetVoid.fromProblem(this.problems);
    }

    /**
     * Return a non-null value, or, if the collector has problems,
     * the problems.
     *
     * @param value the returned non-null value.
     * @param <T> type of the value
     * @return the problems or the value
     */
    @Nonnull
    public <T> RetVal<T> complete(@Nonnull final T value) {
        if (this.problems.isEmpty()) {
            return RetVal.ok(value);
        }
        return RetVal.fromProblem(this.problems);
    }

    /**
     * Return a nullable value, unless this collector has problems,
     * in which case a {@link RetNullable} is returned with the problems.
     *
     * @param value nullable value to include in the return value
     * @param <T> type of the value
     * @return problems or the value
     */
    @Nonnull
    public <T> RetNullable<T> completeNullable(@Nullable final T value) {
        if (this.problems.isEmpty()) {
            return RetNullable.ok(value);
        }
        return RetNullable.fromProblem(this.problems);
    }

    /**
     *  Return the collection of problems as a {@link RetVoid}.  If there are no problems,
     *  then the returned value is ok.
     *
     *  <p>In general, it's better to use
     *  {@link RetVoid#fromProblems(ProblemContainer, ProblemContainer...)}, taking this
     *  collector as the argument, but it's provided here for symmetry with the other
     *  complete methods.
     *
     * @return problems or an ok value
     * @since 2.1
     */
    @Nonnull
    public RetVoid completeVoid() {
        return RetVoid.fromProblem(this.problems);
    }


    /**
     * Convert this collection to a warning, containing all this collector's problems along with
     * the argument value.
     *
     * @param value value to include in the warning.
     * @param <T> type of the value.
     * @return the warning with this collector's problems and the value.
     */
    @Nonnull
    public <T> WarningVal<T> asWarning(@Nonnull final T value) {
        return WarningVal.from(value, this);
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
