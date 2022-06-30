// Released under the MIT License.
package net.groboclown.retval.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoggingNotCompletedListenerTest {
    Logger originalLogger;
    TestableLogger currentLogger;


    @Test
    void instanceNotCompleted() {
        // This calls into the java.util.logging, which is neigh impossible to
        // make testable.  So we'll do the best that we can.
        // By default, logging is sent to system.err, but we can't guarantee that
        // the test environment is setup that way.
        final StackTraceElement ste = new StackTraceElement("loader", "mx", "fs", 1);
        LoggingNotCompletedListener.INSTANCE.instanceNotCompleted(
                "m-o",
                "instance-x",
                new StackTraceElement[] { ste }
        );
        assertEquals(1, this.currentLogger.logRecords.size());
        final LogRecord record = this.currentLogger.logRecords.get(0);
        assertEquals(Level.WARNING, record.getLevel());
        assertEquals("{0}: did not complete {1}; created at: {2}", record.getMessage());
        final Object[] params = record.getParameters();
        assertNotNull(params);
        assertEquals(3, params.length);
        assertEquals("m-o", params[0]);
        assertEquals("instance-x", params[1]);
        assertEquals(List.of(ste), params[2]);
    }

    @Test
    void changeLoggerNull() {
        try {
            //noinspection ConstantConditions
            LoggingNotCompletedListener.INSTANCE.changeLogger(null);
            fail("Did not throw NullPointerException");
        } catch (NullPointerException e) {
            assertTrue(e.getMessage().contains("null"), e.getMessage());
        }

        // Ensure that logging still happens with the logger that was set before the null call.
        instanceNotCompleted();
    }

    @BeforeEach
    void setUp() {
        this.currentLogger = new TestableLogger();
        this.originalLogger = LoggingNotCompletedListener.INSTANCE.changeLogger(currentLogger);
    }

    @AfterEach
    void tearDown() {
        LoggingNotCompletedListener.INSTANCE.changeLogger(this.originalLogger);
    }


    static class TestableLogger extends Logger {
        final List<LogRecord> logRecords = new ArrayList<>();

        TestableLogger() {
            super("TestableLogger", null);
        }

        @Override
        public void log(Level level, String msg) {
            this.logRecords.add(new LogRecord(level, msg));
        }

        @Override
        public void log(Level level, String msg, Object param) {
            final LogRecord lr = new LogRecord(level, msg);
            lr.setParameters(new Object[]{param});
            this.logRecords.add(lr);
        }

        @Override
        public void log(Level level, String msg, Object[] params) {
            final LogRecord lr = new LogRecord(level, msg);
            lr.setParameters(params);
            this.logRecords.add(lr);
        }

        @Override
        public void log(Level level, String msg, Throwable thrown) {
            final LogRecord lr = new LogRecord(level, msg);
            lr.setThrown(thrown);
            this.logRecords.add(lr);
        }

        @Override
        public void log(Level level, Throwable thrown, Supplier<String> msgSupplier) {
            final LogRecord lr = new LogRecord(level, msgSupplier.get());
            lr.setThrown(thrown);
            this.logRecords.add(lr);
        }
    }
}
