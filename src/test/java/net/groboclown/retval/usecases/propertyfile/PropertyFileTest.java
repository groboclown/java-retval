// Released under the MIT License.
package net.groboclown.retval.usecases.propertyfile;

import java.util.List;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.monitor.MockProblemMonitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Test the {@link PropertyFile} for different use cases.
 */
public class PropertyFileTest {
    MockProblemMonitor monitor;

    /**
     * Bog-standard load failure test.
     */
    @Test
    void loadFromClasspath_notExist() {
        RetVal<PropertyFile> res = PropertyFile.loadFromClasspath(getClass(), "does-not-exist");
        assertEquals(1, res.anyProblems().size());
    }

    /**
     * Get a key that doesn't exist.  In this case, it uses a map call to take the property file
     * and extract a {@literal null} value, then uses the default value.
     */
    @Test
    void nullStringDefault() {
        RetVal<String> res = PropertyFile
                .loadFromClasspath(getClass(), "values.properties")
                .mapNullable((p) -> p.get("not-a-key"))
                .defaultAs("my-value");
        assertEquals(List.of(), res.anyProblems());
        assertEquals("my-value", res.result());
    }

    /**
     * Get a key that exists, and throw away a default value.
     */
    @Test
    void existStringDefault() {
        RetVal<String> res = PropertyFile
                .loadFromClasspath(getClass(), "values.properties")
                .mapNullable((p) -> p.get("string-key"))
                .defaultAs("my-value");
        assertEquals(List.of(), res.anyProblems());
        assertEquals("Some String", res.result());
    }

    /**
     * Get a key that doesn't exist.  In this case, it uses a map call to take the property file
     * and extract a {@literal null} value, then uses the default value.
     */
    @Test
    void nullIntDefault() {
        RetVal<Integer> res = PropertyFile
                .loadFromClasspath(getClass(), "values.properties")
                .thenNullable((p) -> p.resolveInt("not-a-value"))
                .defaultAs(2);
        assertEquals(List.of(), res.anyProblems());
        assertEquals(2, res.result());
    }

    /**
     * Get a key that exists, and throw away a default value.
     */
    @Test
    void existIntDefault() {
        RetVal<Integer> res = PropertyFile
                .loadFromClasspath(getClass(), "values.properties")
                .thenNullable((p) -> p.resolveInt("int-key"))
                .defaultAs(30);
        assertEquals(List.of(), res.anyProblems());
        assertEquals(10, res.result());
    }

    @BeforeEach
    void beforeEach() {
        this.monitor = MockProblemMonitor.setup();
        this.monitor.traceEnabled = true;
    }

    @AfterEach
    void afterEach() {
        this.monitor.tearDown();
    }
}
