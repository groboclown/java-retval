// Released under the MIT License. 
package net.groboclown.retval.v1;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Collects problems from multiple requests to gather data.
 */
public class RetCollector {
    private final List<Problem> problems = new ArrayList<>();


    @Nonnull
    public static RetCollector start(@Nonnull ProblemContainer... containers) {
        return new RetCollector().with(containers);
    }

    @Nonnull
    public static <T> RetCollector start(
            @Nonnull final RetVal<T> value,
            @Nonnull final NonnullConsumer<T> setter
    ) {
        return new RetCollector().withValue(value, setter);
    }

    @Nonnull
    public static <T> RetCollector start(
            @Nonnull final RetNullable<T> value,
            @Nonnull final Consumer<T> setter
    ) {
        return new RetCollector().withValue(value, setter);
    }


    @Nonnull
    public RetCollector with(@Nonnull ProblemContainer... containers) {
        for (ProblemContainer container: containers) {
            this.problems.addAll(container.anyProblems());
        }
        return this;
    }

    @Nonnull
    public <T> RetCollector withValue(
            @Nonnull final RetVal<T> value,
            @Nonnull final NonnullConsumer<T> setter
    ) {
        this.problems.addAll(value.anyProblems());
        if (value.isOk()) {
            setter.accept(value.result());
        }
        return this;
    }

    @Nonnull
    public <T> RetCollector withValue(
            @Nonnull final RetNullable<T> value,
            @Nonnull final Consumer<T> setter
    ) {
        this.problems.addAll(value.anyProblems());
        if (value.isOk()) {
            setter.accept(value.result());
        }
        return this;
    }

    public <T> RetVal<T> thenValue(@Nonnull final Supplier<T> getter) {
        if (this.problems.isEmpty()) {
            return RetVal.ok(getter.get());
        }
        return RetVal.error(this.problems);
    }

    public <T> RetVal<T> then(@Nonnull final NonnullSupplier<RetVal<T>> getter) {
        if (this.problems.isEmpty()) {
            return getter.get();
        }
        return RetVal.error(this.problems);
    }

    public <T> RetNullable<T> thenRetNullable(@Nonnull final NonnullSupplier<RetNullable<T>> getter) {
        if (this.problems.isEmpty()) {
            return getter.get();
        }
        return RetNullable.error(this.problems);
    }

    public <T> RetNullable<T> thenNullable(@Nonnull final Supplier<T> getter) {
        if (this.problems.isEmpty()) {
            return RetNullable.ok(getter.get());
        }
        return RetNullable.error(this.problems);
    }
}
