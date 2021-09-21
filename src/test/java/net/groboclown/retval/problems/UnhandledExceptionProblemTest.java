// Released under the MIT License.
package net.groboclown.retval.problems;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnhandledExceptionProblemTest {
    @Test
    void localMessage_noMessages() {
        final Throwable t = new LocalizedException(null, null);
        final UnhandledExceptionProblem wrapped = UnhandledExceptionProblem.wrap(t);
        assertEquals(t.getClass().getName(), wrapped.localMessage());
        assertEquals(t.getClass().getName(), wrapped.toString());
        assertSame(t, wrapped.getSourceException());
    }

    @Test
    void localMessage_normalMessage() {
        final Throwable t = new LocalizedException("blah", null);
        final UnhandledExceptionProblem wrapped = UnhandledExceptionProblem.wrap(t);
        assertEquals("blah", wrapped.localMessage());
        assertEquals("blah", wrapped.toString());
        assertSame(t, wrapped.getSourceException());
    }

    @Test
    void localMessage_localizedMessage() {
        final Throwable t = new LocalizedException(null, "foo");
        final UnhandledExceptionProblem wrapped = UnhandledExceptionProblem.wrap(t);
        assertEquals("foo", wrapped.localMessage());
        assertEquals("foo", wrapped.toString());
        assertSame(t, wrapped.getSourceException());
    }

    static class LocalizedException extends Exception {
        private final String localized;

        LocalizedException(final String message, final String localized) {
            super(message);
            this.localized = localized;
        }

        @Override
        public String getLocalizedMessage() {
            return this.localized;
        }
    }
}