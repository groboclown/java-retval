// Released under the MIT License. 
package net.groboclown.retval.impl;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.groboclown.retval.Problem;
import net.groboclown.retval.RetNullable;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.RetVoid;
import net.groboclown.retval.contract.MonitoredContract;


class MonitoredContractTest extends MonitoredContract {
    @Nonnull
    @Override
    protected <T> RetNullable<T> createForNullable(@Nullable final T value) {
        return new MonitoredReturnValue<>(value);
    }

    @Nonnull
    @Override
    protected <T> RetNullable<T> createForNullableProblems(@Nonnull final List<Problem> problems) {
        return new MonitoredReturnProblem<>(problems);
    }

    @Nonnull
    @Override
    protected <T> RetVal<T> createForVal(@Nullable final T value) {
        return new MonitoredReturnValue<>(value);
    }

    @Nonnull
    @Override
    protected <T> RetVal<T> createForValProblems(@Nonnull final List<Problem> problems) {
        return new MonitoredReturnProblem<>(problems);
    }

    @Nonnull
    @Override
    protected RetVoid createForVoid() {
        return new MonitoredReturnValue<>(null);
    }

    @Nonnull
    @Override
    protected RetVoid createForVoidProblems(@Nonnull final List<Problem> problems) {
        return new MonitoredReturnProblem<>(problems);
    }
}
