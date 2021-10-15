// Released under the MIT License. 
package net.groboclown.retval.impl;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.groboclown.retval.Problem;
import net.groboclown.retval.RetNullable;
import net.groboclown.retval.contract.RetNullableContract;

class MonitoredRetNullableContractTest extends RetNullableContract {
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
}
