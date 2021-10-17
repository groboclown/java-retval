// Released under the MIT License. 
package net.groboclown.retval.impl;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.groboclown.retval.Problem;
import net.groboclown.retval.RetNullable;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.RetVoid;


/**
 * Generates unmonitored, streamlined implementations.
 */
public class SimpleFactory implements ReturnTypeFactory {
    public static final SimpleFactory INSTANCE = new SimpleFactory();

    private SimpleFactory() {
        // Utility class
    }

    @Nonnull
    @Override
    public <T> RetNullable<T> createNullableOk(@Nullable final T value) {
        return new SimpleReturnValue<>(value);
    }

    @Nonnull
    @Override
    public <T> RetNullable<T> createNullableFromProblems(@Nonnull final List<Problem> problems) {
        return new SimpleReturnProblem<>(problems);
    }

    @Nonnull
    @Override
    public <T> RetVal<T> createValOk(@Nonnull final T value) {
        return new SimpleReturnValue<>(value);
    }

    @Nonnull
    @Override
    public <T> RetVal<T> createValFromProblems(@Nonnull final List<Problem> problems) {
        return new SimpleReturnProblem<>(problems);
    }

    @Nonnull
    @Override
    public RetVoid createVoidOk() {
        return SimpleRetVoidOk.OK;
    }

    @Nonnull
    @Override
    public RetVoid createVoidFromProblems(@Nonnull final List<Problem> problems) {
        return new SimpleReturnProblem<Void>(problems);
    }
}
