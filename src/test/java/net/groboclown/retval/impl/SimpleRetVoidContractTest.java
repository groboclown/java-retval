// Released under the MIT License. 
package net.groboclown.retval.impl;

import java.util.List;
import javax.annotation.Nonnull;
import net.groboclown.retval.Problem;
import net.groboclown.retval.RetVoid;
import net.groboclown.retval.contract.RetVoidContract;

class SimpleRetVoidContractTest extends RetVoidContract {
    @Nonnull
    @Override
    protected RetVoid createForVoid() {
        return SimpleFactory.INSTANCE.createVoidOk();
    }

    @Nonnull
    @Override
    protected RetVoid createForVoidProblems(@Nonnull final List<Problem> problems) {
        return SimpleFactory.INSTANCE.createVoidFromProblems(problems);
    }
}
