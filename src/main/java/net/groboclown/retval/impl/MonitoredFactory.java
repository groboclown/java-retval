// Released under the MIT License. 
package net.groboclown.retval.impl;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.groboclown.retval.Problem;
import net.groboclown.retval.RetNullable;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.RetVoid;
import net.groboclown.retval.monitor.ObservedMonitor;


/**
 * Generates monitored versions of the return types.
 */
public class MonitoredFactory implements ReturnTypeFactory {
    public static MonitoredFactory INSTANCE = new MonitoredFactory();

    private MonitoredFactory() {
        // Utility class
    }

    @Nonnull
    @Override
    public <T> RetNullable<T> createNullableOk(@Nullable final T value) {
        return new MonitoredReturnValue<>(value);
    }

    @Nonnull
    @Override
    public <T> RetNullable<T> createNullableFromProblems(@Nonnull final List<Problem> problems) {
        return new MonitoredReturnProblem<>(problems);
    }

    @Nonnull
    @Override
    public <T> RetVal<T> createValOk(@Nonnull final T value) {
        return new MonitoredReturnValue<>(value);
    }

    @Nonnull
    @Override
    public <T> RetVal<T> createValFromProblems(@Nonnull final List<Problem> problems) {
        return new MonitoredReturnProblem<>(problems);
    }

    @Nonnull
    @Override
    public RetVoid createVoidOk() {
        if (ObservedMonitor.getCheckedInstance().isTraceEnabled()) {
            return new MonitoredRetVoidOk();
        }
        return SimpleRetVoidOk.OK;
    }

    @Nonnull
    @Override
    public RetVoid voidFromProblems(@Nonnull final List<Problem> problems) {
        return new MonitoredReturnProblem<Void>(problems);
    }
}
