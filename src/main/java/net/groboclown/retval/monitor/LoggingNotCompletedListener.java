// Released under the MIT License. 
package net.groboclown.retval.monitor;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

/**
 * A listener that uses java.util.logging for the reporting.
 */
public class LoggingNotCompletedListener implements NotCompletedListener {
    public static final LoggingNotCompletedListener INSTANCE = new LoggingNotCompletedListener();

    private final Logger logger = Logger.getLogger(ObservedMonitor.class.getName());

    private LoggingNotCompletedListener() {
        // Prevent creating
    }

    @Override
    public void instanceNotCompleted(
            @Nonnull final String monitorName,
            @Nonnull final String instanceName,
            @Nonnull final StackTraceElement[] creationPoint) {
        this.logger.log(
                Level.WARNING,
                monitorName + ": did not complete " + instanceName + "; created at: {0}",
                Arrays.asList(creationPoint));
    }
}
