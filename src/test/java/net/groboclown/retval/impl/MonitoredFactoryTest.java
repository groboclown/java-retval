// Released under the MIT License. 
package net.groboclown.retval.impl;

import java.util.List;
import net.groboclown.retval.RetNullable;
import net.groboclown.retval.monitor.MockProblemMonitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MonitoredFactoryTest {
    MockProblemMonitor monitor;

    @Test
    void createNullableOk_null_traceEnabled() {
        this.monitor.traceEnabled = true;
        final RetNullable<Object> res = MonitoredFactory.INSTANCE.createNullableOk(null);
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }


    @Test
    void createNullableOk_null_traceDisabled() {
        this.monitor.traceEnabled = false;
        final RetNullable<Object> res = MonitoredFactory.INSTANCE.createNullableOk(null);
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }


    @BeforeEach
    void beforeEach() {
        this.monitor = MockProblemMonitor.setup();
        this.monitor.traceEnabled = true;
    }

    @AfterEach
    void afterEach() {
        this.monitor.tearDown();
    }
}
