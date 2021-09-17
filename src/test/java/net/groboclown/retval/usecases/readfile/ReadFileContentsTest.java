// Released under the MIT License. 
package net.groboclown.retval.usecases.readfile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import net.groboclown.retval.monitor.MockCheckMonitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the use case for wrapping an exception without an error.
 */
class ReadFileContentsTest {
    MockCheckMonitor monitor;

    @Test
    void readFromClasspath() {
        final Reader reader = new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("ReadFileTest1.txt")),
                StandardCharsets.UTF_8);
        assertEquals(
                List.of(),
                ReadFileContents.readFully("ReadFileTest1.txt", reader).thenVoid((text) -> {
                    assertEquals("contents", text.trim());
                }).anyProblems()
        );
        assertEquals(
                List.of(),
                this.monitor.getNeverChecked()
        );
    }

    @BeforeEach
    void beforeEach() {
        this.monitor = MockCheckMonitor.setup();
    }

    @AfterEach
    void afterEach() {
        this.monitor.tearDown();
    }
}
