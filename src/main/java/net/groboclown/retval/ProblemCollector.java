// Released under the MIT License. 
package net.groboclown.retval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.groboclown.retval.function.NonnullConsumer;
import net.groboclown.retval.function.NonnullSupplier;

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

    @Nonnull
    public static ProblemCollector from() {
        return new ProblemCollector();
    }


    @Nonnull
    public static ProblemCollector from(
            @Nonnull final ProblemContainer container,
            @Nonnull final ProblemContainer... containers
    ) {
        return new ProblemCollector().with(container, containers);
    }

    /**
     * Create a new RetCollector and invoke {@link #withValue(RetVal, NonnullConsumer)}.
     *
     * @param value value to pass to the setter, or problems to include in the collection.
     * @param setter passed the value.
     * @param <T> type of the value
     * @return a new collector
     */
    @Nonnull
    public static <T> ProblemCollector fromValue(
            @Nonnull final RetVal<T> value,
            @Nonnull final NonnullConsumer<T> setter
    ) {
        return new ProblemCollector().withValue(value, setter);
    }

    /**
     * Create a new RetCollector and invoke {@link #withValue(RetNullable, Consumer)}.
     *
     * @param value value to pass to the setter, or problems to include in the collection.
     * @param setter passed the value.
     * @param <T> type of the value
     * @return a new collector
     */
    @Nonnull
    public static <T> ProblemCollector fromValue(
            @Nonnull final RetNullable<T> value,
            @Nonnull final Consumer<T> setter
    ) {
        return new ProblemCollector().withValue(value, setter);
    }

    /**
     * Adds all the problems in the argument into this collector.
     *
     * @param problem a problem to add
     * @param problems optional additional problems.
     * @return this instance
     */
    @Nonnull
    public ProblemCollector with(
            @Nonnull final Problem problem,
            @Nonnull final Problem... problems) {
        this.problems.add(problem);
        this.problems.addAll(Arrays.asList(problems));
        return this;
    }

    /**
     * Adds all the problems in the argument into this collector.
     *
     * @param container problem container
     * @param containers optional additional problem containers.
     * @return this instance
     */
    @Nonnull
    public ProblemCollector with(
            @Nonnull final ProblemContainer container,
            @Nonnull final ProblemContainer... containers) {
        this.problems.addAll(container.anyProblems());
        for (final ProblemContainer pc : containers) {
            this.problems.addAll(pc.anyProblems());
        }
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
    public final ProblemCollector withProblemSets(
            @Nonnull final Collection<Problem> problems,
            @Nonnull final Collection<Problem>... problemList) {
        this.problems.addAll(Ret.joinProblemSets(problems, problemList));
        return this;
    }

    /**
     * Adds all the problems in the argument into this collector.
     *
     * @param containerSet problem containers
     * @param containerSets optional additional problem containers.
     * @return this instance
     */
    @SafeVarargs
    @Nonnull
    public final ProblemCollector withRetSets(
            @Nonnull final Collection<ProblemContainer> containerSet,
            @Nonnull final Collection<ProblemContainer>... containerSets) {
        this.problems.addAll(Ret.joinRetProblemSets(containerSet, containerSets));
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
    public <T> ProblemCollector withValue(
            @Nonnull final RetVal<T> value,
            @Nonnull final NonnullConsumer<T> setter
    ) {
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
    public <T> ProblemCollector withValue(
            @Nonnull final RetNullable<T> value,
            @Nonnull final Consumer<T> setter
    ) {
        this.problems.addAll(value.anyProblems());
        if (value.isOk()) {
            setter.accept(value.result());
        }
        return this;
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
    public <T> RetNullable<T> thenNullable(@Nonnull final NonnullSupplier<RetNullable<T>> getter) {
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
     * in which case a RetNullable is returned with the problems.
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

    @Nonnull
    public <T> WarningVal<T> warn(@Nonnull final T value) {
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
        return List.copyOf(this.problems);
    }

    @Nonnull
    @Override
    public Collection<Problem> validProblems() {
        Ret.enforceHasProblems(this.problems);
        return List.copyOf(this.problems);
    }

    @Nonnull
    @Override
    public String debugProblems(@Nonnull final String joinedWith) {
        return Ret.joinProblemMessages(joinedWith, this.problems);
    }
}
