// Released under the MIT License. 
package net.groboclown.retval.impl;

import java.util.List;
import javax.annotation.Nonnull;
import net.groboclown.retval.Problem;
import net.groboclown.retval.RetVoid;
import net.groboclown.retval.contract.RetVoidContract;

class MonitoredRetVoidContractTest extends RetVoidContract {
    @Nonnull
    @Override
    protected RetVoid createForVoid() {
        return new MonitoredRetVoidOk();
    }

    @Nonnull
    @Override
    protected RetVoid createForVoidProblems(@Nonnull final List<Problem> problems) {
        return new MonitoredReturnProblem<>(problems);
    }
}
