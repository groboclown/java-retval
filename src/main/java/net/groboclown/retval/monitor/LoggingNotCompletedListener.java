// Released under the MIT License. 
package net.groboclown.retval.monitor;

import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

/**
 * A listener that uses java.util.logging for the reporting.
 */
public class LoggingNotCompletedListener implements NotCompletedListener {
    public static final LoggingNotCompletedListener INSTANCE = new LoggingNotCompletedListener();

    private Logger logger = Logger.getLogger(ObservedMonitor.class.getName());

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
                "{0}: did not complete {1}; created at: {2}",
                new Object[]{
                    monitorName,
                    instanceName,
                    Arrays.asList(creationPoint)
                });
    }

    /**
     * Made for testing purposes.
     *
     * @param newLogger logger to set as the new one.
     * @return original logger.
     */
    Logger changeLogger(@Nonnull Logger newLogger) {
        final Logger ret = this.logger;
        this.logger = Objects.requireNonNull(newLogger, "logger argument cannot be null");
        return ret;
    }
}
