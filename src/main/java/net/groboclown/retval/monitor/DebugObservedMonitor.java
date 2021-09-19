// Released under the MIT License. 
package net.groboclown.retval.monitor;

import java.lang.ref.Cleaner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import net.groboclown.retval.ProblemContainer;


/**
 * Maintains information about objects that need to be checked in a phantom reference cleaner.
 */
public class DebugObservedMonitor<T> extends ObservedMonitor<T> {
    private final Logger logger = Logger.getLogger(DebugObservedMonitor.class.getName());
    private final Cleaner cleaner = Cleaner.create();
    private final String name;

    public DebugObservedMonitor(@Nonnull final String name) {
        this.name = name;
    }

    @Nonnull
    @Override
    public Listener registerInstance(@Nonnull final T instance) {
        return new LocalListener(this.name, instance, this.cleaner, this.logger);
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }


    // A weird way of maintaining a state to use in the report generation, and to
    // trigger the generation of the report.
    private static class NotCompleted extends Exception implements Runnable {
        private final Logger logger;
        private volatile boolean stillOpen = true;

        private NotCompleted(final Logger logger, final String message) {
            super(message);
            this.logger = logger;
        }

        void close() {
            this.stillOpen = false;
        }

        @Override
        public void run() {
            if (this.stillOpen) {
                this.logger.log(Level.WARNING, getMessage(), this);

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
                @Nonnull final Cleaner cleaner, @Nonnull final Logger logger
        ) {
            this.state = new NotCompleted(logger, name + " not called: " + object);
            this.cleanable = cleaner.register(object, this.state);
        }

        @Override
        public void onObserved() {
            this.state.close();
            this.cleanable.clean();
        }
    }
}
