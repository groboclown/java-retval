// Released under the MIT License.
package net.groboclown.retval.monitor;

import javax.annotation.Nonnull;
import net.groboclown.retval.ProblemContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObservedMonitorRegistrarTest {
    ObservedMonitor<ProblemContainer> original;


    @Test
    void setGet() {
        final InnerObservedMonitor mon = new InnerObservedMonitor();
        ObservedMonitorRegistrar.setCheckedInstance(mon);
        assertSame(mon, ObservedMonitorRegistrar.getCheckedInstance());
    }


    @BeforeEach
    void beforeEach() {
        this.original = ObservedMonitorRegistrar.getCheckedInstance();
    }

    @AfterEach
    void afterEach() {
        ObservedMonitorRegistrar.setCheckedInstance(this.original);
    }

    static class InnerObservedMonitor implements ObservedMonitor<ProblemContainer> {

        @Nonnull
        @Override
        public Listener registerInstance(@Nonnull final ProblemContainer instance) {
            throw new IllegalStateException("not implemented");
        }

        @Override
        public boolean isTraceEnabled() {
            throw new IllegalStateException("not implemented");
        }
    }
}
