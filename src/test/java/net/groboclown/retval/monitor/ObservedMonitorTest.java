// Released under the MIT License.
package net.groboclown.retval.monitor;

import java.util.Map;
import net.groboclown.retval.ProblemContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObservedMonitorTest {
    Map<String, String> env;

    @Test
    void discoverCheckedInstance_noMonitor() {
        SystemEnvUtil.getSettings().remove("RETVAL_MONITOR_DEBUG");
        final ObservedMonitor<ProblemContainer> monitor = ObservedMonitor.discoverCheckedInstance();
        assertSame(NoOpObservedMonitor.getInstance(), monitor);
    }

    @Test
    void discoverCheckedInstance_debugMonitor() {
        SystemEnvUtil.getSettings().put("RETVAL_MONITOR_DEBUG", "true");
        final ObservedMonitor<ProblemContainer> monitor = ObservedMonitor.discoverCheckedInstance();
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