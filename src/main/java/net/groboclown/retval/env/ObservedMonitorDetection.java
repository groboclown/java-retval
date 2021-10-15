// Released under the MIT License. 
package net.groboclown.retval.env;

import javax.annotation.Nonnull;
import net.groboclown.retval.ProblemContainer;
import net.groboclown.retval.monitor.DebugObservedMonitor;
import net.groboclown.retval.monitor.LoggingNotCompletedListener;
import net.groboclown.retval.monitor.NoOpObservedMonitor;
import net.groboclown.retval.monitor.ObservedMonitor;

/**
 * Detects the startup implementation for the observation monitors.
 */
public class ObservedMonitorDetection {
    private ObservedMonitorDetection() {
        // utility class.
    }

    /**
     * Checks system settings for the correct instance to load.
     *
     * @return the startup version of the observed monitor for problem containers.
     */
    @Nonnull
    public static ObservedMonitor<ProblemContainer> discoverCheckedInstance() {
        // This is a placeholder for an eventual, possible more robust
        // dynamic implementation.
        if (SystemEnvUtil.isValueEqual("RETVAL_MONITOR_DEBUG", "true")) {
            return new DebugObservedMonitor<>(
                    "problem state", LoggingNotCompletedListener.INSTANCE);
        }
        return NoOpObservedMonitor.getInstance();
    }
}
