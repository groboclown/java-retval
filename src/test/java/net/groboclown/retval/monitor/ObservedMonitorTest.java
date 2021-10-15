// Released under the MIT License.
package net.groboclown.retval.monitor;

import javax.annotation.Nonnull;
import net.groboclown.retval.ProblemContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObservedMonitorTest {
    ObservedMonitor<ProblemContainer> original;


    @Test
    void setGet() {
        final InnerObservedMonitor mon = new InnerObservedMonitor();
        ObservedMonitor.setCheckedInstance(mon);
        assertSame(mon, ObservedMonitor.getCheckedInstance());
    }


    @BeforeEach
    void beforeEach() {
        this.original = ObservedMonitor.getCheckedInstance();
    }

    @AfterEach
    void afterEach() {
        ObservedMonitor.setCheckedInstance(this.original);
    }

    static class InnerObservedMonitor extends ObservedMonitor<ProblemContainer> {

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
