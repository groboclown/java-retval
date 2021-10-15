// Released under the MIT License. 
package net.groboclown.retval.usecases.readfile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import net.groboclown.retval.monitor.MockProblemMonitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the use case for wrapping an exception without an error.
 */
class ReadFileContentsTest {
    MockProblemMonitor monitor;

    @Test
    void readFromClasspath() {
        final Reader reader = new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("ReadFileTest1.txt")),
                StandardCharsets.UTF_8);
        assertEquals(
                // Validate no problems
                List.of(),
                ReadFileContents
                        .readFully("ReadFileTest1.txt", reader)
                    .thenVoid((text) -> {
                        // Validate the contents read
                        assertEquals("contents", text.trim());
                    })
                .anyProblems()
        );
        // Validate it was all observed.
        assertEquals(
                List.of(),
                this.monitor.getNeverObserved()
        );
    }

    @BeforeEach
    void beforeEach() {
        this.monitor = MockProblemMonitor.setup();
    }

    @AfterEach
    void afterEach() {
        this.monitor.tearDown();
    }
}
