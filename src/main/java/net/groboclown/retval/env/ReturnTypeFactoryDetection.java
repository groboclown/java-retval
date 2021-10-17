// Released under the MIT License. 
package net.groboclown.retval.env;

import javax.annotation.Nonnull;
import net.groboclown.retval.impl.MonitoredFactory;
import net.groboclown.retval.impl.ReturnTypeFactory;
import net.groboclown.retval.impl.SimpleFactory;

/**
 * Detects the return type factory implementation to use.
 */
public class ReturnTypeFactoryDetection {
    private ReturnTypeFactoryDetection() {
        // utility class.
    }

    /**
     * Checks system settings for the correct factory to load.
     *
     * @return the startup version of the Ret* factory.
     */
    @Nonnull
    public static ReturnTypeFactory discoverReturnTypeFactory() {
        // This is a placeholder for an eventual, possible more robust
        // dynamic implementation.
        if (SystemEnvUtil.isValueEqual("RETVAL_PRODUCTION", "true")) {
            return SimpleFactory.INSTANCE;
        }
        return MonitoredFactory.INSTANCE;
    }
}
