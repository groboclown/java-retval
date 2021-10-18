// Released under the MIT License.
package net.groboclown.retval.env;

import java.util.HashMap;
import java.util.Map;
import net.groboclown.retval.impl.MonitoredFactory;
import net.groboclown.retval.impl.ReturnTypeFactory;
import net.groboclown.retval.impl.SimpleFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReturnTypeFactoryDetectionTest {
    Map<String, String> origEnv;

    @Test
    void discoverReturnTypeFactory_production() {
        SystemEnvUtil.getSettings().put("RETVAL_PRODUCTION", "true");
        final ReturnTypeFactory factory =
                ReturnTypeFactoryDetection.discoverReturnTypeFactory();
        assertSame(SimpleFactory.INSTANCE, factory);
    }

    @Test
    void discoverReturnTypeFactory_standard_set() {
        SystemEnvUtil.getSettings().put("RETVAL_PRODUCTION", "false");
        final ReturnTypeFactory factory =
                ReturnTypeFactoryDetection.discoverReturnTypeFactory();
        assertSame(MonitoredFactory.INSTANCE, factory);

    }

    @Test
    void discoverReturnTypeFactory_standard_unset() {
        SystemEnvUtil.getSettings().remove("RETVAL_PRODUCTION");
        final ReturnTypeFactory factory =
                ReturnTypeFactoryDetection.discoverReturnTypeFactory();
        assertSame(MonitoredFactory.INSTANCE, factory);

    }


    @BeforeEach
    void beforeEach() {
        this.origEnv = new HashMap<>(SystemEnvUtil.getSettings());
    }

    @AfterEach
    void afterEach() {
        SystemEnvUtil.getSettings().clear();
        SystemEnvUtil.getSettings().putAll(this.origEnv);
    }
}