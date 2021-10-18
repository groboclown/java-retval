// Released under the MIT License. 
package net.groboclown.retval.monitor;

import java.lang.ref.Cleaner;
import javax.annotation.Nonnull;

/**
 * Maintains information about objects that need to be checked in a phantom reference cleaner.
 */
public class DebugObservedMonitor<T> implements ObservedMonitor<T> {
    private final NotCompletedListener listener;
    private final Cleaner cleaner = Cleaner.create();
    private final String name;

    /**
     * Create a new monitor instance.
     *
     * <p>Provided for eventual migration to generic monitor creation.
     *
     * @param listener listener for when not-completed states are detected.
     * @param name name of the type being monitored
     */
    public DebugObservedMonitor(
            @Nonnull final String name,
            @Nonnull final NotCompletedListener listener) {
        this.name = name;
        this.listener = listener;
    }

    @Nonnull
    @Override
    public Listener registerInstance(@Nonnull final T instance) {
        return new LocalListener(this.name, instance, this.cleaner, this.listener);
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }


    // A weird way of maintaining a state to use in the report generation, and to
    // trigger the generation of the report.
    private static class NotCompleted implements Runnable {
        private final NotCompletedListener logger;
        private final String monitorName;
        private final String objectStr;
        private final StackTraceElement[] stack;
        private volatile boolean stillOpen = true;

        private NotCompleted(
                @Nonnull final NotCompletedListener logger,
                @Nonnull final String monitorName,
                @Nonnull final String objectStr) {
            this.logger = logger;
            this.monitorName = monitorName;
            this.objectStr = objectStr;
            this.stack = mungedStack();
        }

        void close() {
            this.stillOpen = false;
        }

        @Override
        public void run() {
            if (this.stillOpen) {
                this.logger.instanceNotCompleted(this.monitorName, this.objectStr, this.stack);

                // Ensure this isn't called a second time.  The API doc for Cleaner says this
                // will be called at most once, but this is just to be sure.
                this.stillOpen = false;
            }
        }
    }

    private static class LocalListener implements Listener {
        private final NotCompleted state;
        private final Cleaner.Cleanable cleanable;

        LocalListener(
                @Nonnull final String name, @Nonnull final Object object,
                @Nonnull final Cleaner cleaner, @Nonnull final NotCompletedListener logger
        ) {
            this.state = new NotCompleted(logger, name, object.toString());
            this.cleanable = cleaner.register(object, this.state);
        }

        @Override
        public void onObserved() {
            this.state.close();
            this.cleanable.clean();
        }
    }


    private static final String REMOVE_AFTER_CLASSNAME = DebugObservedMonitor.class.getName();
    private static final String REMOVE_AFTER_METHOD_NAME = "registerInstance";

    private static StackTraceElement[] mungedStack() {
        final StackTraceElement[] currentStack = new Exception().fillInStackTrace().getStackTrace();

        int remove = 0;
        // item [0] is the call stack place that created the exception, which is this
        // method.
        // Note: "- 1" here because the remove index will be the position in the stack after
        // the value found.
        for (int i = 0; i < currentStack.length - 1; i++) {
            if (REMOVE_AFTER_CLASSNAME.equals(currentStack[i].getClassName())
                    && REMOVE_AFTER_METHOD_NAME.equals(currentStack[i].getMethodName())) {
                remove = i + 1;
                break;
            }
        }

        final StackTraceElement[] ret = new StackTraceElement[currentStack.length - remove];
        System.arraycopy(currentStack, remove, ret, 0, ret.length);
        return ret;
    }
}
