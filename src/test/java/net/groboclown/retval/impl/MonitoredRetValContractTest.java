// Released under the MIT License. 
package net.groboclown.retval.impl;

import java.util.List;
import javax.annotation.Nonnull;
import net.groboclown.retval.Problem;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.contract.RetValContract;

class MonitoredRetValContractTest extends RetValContract {

    @Nonnull
    @Override
    protected <T> RetVal<T> createForVal(final T value) {
        return new MonitoredReturnValue<>(value);
    }

    @Nonnull
    @Override
    protected <T> RetVal<T> createForValProblems(@Nonnull final List<Problem> problems) {
        return new MonitoredReturnProblem<>(problems);
    }
}
