// Released under the MIT License. 
package net.groboclown.retval.impl;

import java.lang.ref.Cleaner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import net.groboclown.retval.CheckMonitor;
import net.groboclown.retval.ProblemContainer;


/**
 * Maintains information about objects that need to be checked in a phantom reference cleaner.
 */
public class DebugCheckMonitor extends CheckMonitor {
    private final Logger logger = Logger.getLogger(DebugCheckMonitor.class.getName());
    private final Cleaner cleaner = Cleaner.create();

    @Nonnull
    @Override
    public CloseableListener registerCloseableInstance(@Nonnull final AutoCloseable instance) {
        return new LocalCloseableListener(instance, this.cleaner, this.logger);
    }

    @Nonnull
    @Override
    public CheckableListener registerErrorInstance(@Nonnull final ProblemContainer instance) {
        return new LocalCheckableListener(instance, this.cleaner, this.logger);
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


    private static class LocalCloseableListener implements CloseableListener {
        private final NotCompleted state;
        private final Cleaner.Cleanable cleanable;

        LocalCloseableListener(
                @Nonnull final AutoCloseable object,
                @Nonnull final Cleaner cleaner, @Nonnull final Logger logger
        ) {
            this.state = new NotCompleted(logger, "Did not close " + object);
            this.cleanable = cleaner.register(object, this.state);
        }

        @Override
        public void onClosed() {
            this.state.close();
            this.cleanable.clean();
        }
    }

    private static class LocalCheckableListener implements CheckableListener {
        private final NotCompleted state;
        private final Cleaner.Cleanable cleanable;

        LocalCheckableListener(
                @Nonnull final ProblemContainer object,
                @Nonnull final Cleaner cleaner, @Nonnull final Logger logger
        ) {
            this.state = new NotCompleted(logger, "Did not check for errors: " + object);
            this.cleanable = cleaner.register(object, this.state);
        }

        @Override
        public void onChecked() {
            this.state.close();
            this.cleanable.clean();
        }
    }
}
