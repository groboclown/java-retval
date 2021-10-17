// Released under the MIT License.
package net.groboclown.retval.env;

import java.util.Map;
import net.groboclown.retval.ProblemContainer;
import net.groboclown.retval.monitor.DebugObservedMonitor;
import net.groboclown.retval.monitor.NoOpObservedMonitor;
import net.groboclown.retval.monitor.ObservedMonitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObservedMonitorDetectionTest {
    Map<String, String> env;

    @Test
    void discoverCheckedInstance_noMonitor() {
        SystemEnvUtil.getSettings().remove("RETVAL_MONITOR_DEBUG");
        final ObservedMonitor<ProblemContainer> monitor =
                ObservedMonitorDetection.discoverCheckedInstance();
        assertSame(NoOpObservedMonitor.getInstance(), monitor);
    }

    @Test
    void discoverCheckedInstance_debugMonitor() {
        SystemEnvUtil.getSettings().put("RETVAL_MONITOR_DEBUG", "true");
        final ObservedMonitor<ProblemContainer> monitor =
                ObservedMonitorDetection.discoverCheckedInstance();
        assertEquals(DebugObservedMonitor.class, monitor.getClass());
    }

    @BeforeEach
    void beforeEach() {
        this.env = Map.copyOf(SystemEnvUtil.getSettings());
    }

    @AfterEach
    void afterEach() {
        SystemEnvUtil.getSettings().clear();
        SystemEnvUtil.getSettings().putAll(this.env);
    }
}
