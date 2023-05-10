// Released under the MIT License.
package net.groboclown.retval.usecases.collected;

import java.util.List;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.RetVoid;
import net.groboclown.retval.monitor.MockProblemMonitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the UrlReference class.
 */
public class UrlReferenceTest {
    MockProblemMonitor monitor;

    @Test
    void partialSetupNotFound() {
        final UrlReference ref = new UrlReference(
                null, "hostref", "portref", null);
        final ResourceStore store = new ResourceStore();
        final RetVoid hostRes = store.addResource("hostref", "localhost");
        assertEquals(
                List.of(),
                hostRes.anyProblems()
        );
        final RetVoid portRes = store.addResource("portref", 80);
        assertEquals(
                List.of(),
                portRes.anyProblems()
        );

        RetVal<String> res = ref.toUrl(store);

        assertEquals(
                List.of(),
                res.anyProblems()
        );
        assertTrue(res.isOk());
        assertEquals("http://localhost:80/", res.result());
        assertEquals(
                List.of(),
                this.monitor.getNeverObserved()
        );
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
