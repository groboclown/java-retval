// Released under the MIT License. 
package net.groboclown.retval.monitor;

import javax.annotation.Nonnull;

/**
 * A pluggable way to change out listening for notifications when a monitor
 * detects that an object was not completed.
 */
public interface NotCompletedListener {
    /**
     * Called when a monitor detects that a registered instance went out of
     * scope from the JVM but was never marked as completed.
     *
     * <p>The stack trace records the moment when the instance was last
     * registered with the monitor.  In cases where the monitor check is passed to
     * multiple objects, this should reflect the most recent stack.
     *
     * @param monitorName name of the monitor, usually indicating the type of instances stored.
     * @param instanceName the toString for this registered instance (it can't be the instance
     *                     itself due to memory references at GC time).
     * @param creationPoint stack trace for when the instance was registered.
     */
    void instanceNotCompleted(
            @Nonnull String monitorName,
            @Nonnull String instanceName,
            @Nonnull StackTraceElement[] creationPoint
    );
}
