// Released under the MIT License.
package net.groboclown.retval;

import java.util.List;
import net.groboclown.retval.monitor.MockProblemMonitor;
import net.groboclown.retval.problems.LocalizedProblem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RetVoidStaticTest {
    MockProblemMonitor monitor;

    @Test
    void fromProblem_collection_empty_tracing() {
        this.monitor.traceEnabled = true;
        final RetVoid ret = RetVoid.fromProblem(List.of());
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(
                List.of(),
                ret.anyProblems()
        );
    }

    @Test
    void fromProblem_collection_empty_noTracing() {
        this.monitor.traceEnabled = false;
        final RetVoid ret = RetVoid.fromProblem(List.of());
        // Should have used constant OK value.
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(
                List.of(),
                ret.anyProblems()
        );
    }

    @Test
    void fromProblem_collection_some_tracing() {
        this.monitor.traceEnabled = true;
        final Problem p1 = LocalizedProblem.from("1");
        final Problem p2 = LocalizedProblem.from("2");
        final RetVoid ret = RetVoid.fromProblem(List.of(p1), List.of(p2));
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(
                List.of(p1, p2),
                ret.anyProblems()
        );
    }

    @Test
    void fromProblem_collection_some_noTracing() {
        this.monitor.traceEnabled = false;
        final Problem p1 = LocalizedProblem.from("1");
        final Problem p2 = LocalizedProblem.from("2");
        final RetVoid ret = RetVoid.fromProblem(List.of(p1), List.of(p2));
        // with problems means new object
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(
                List.of(p1, p2),
                ret.anyProblems()
        );
    }

    @Test
    void fromProblem_null_tracing() {
        this.monitor.traceEnabled = true;
        final RetVoid ret = RetVoid.fromProblem((Problem) null);
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(
                List.of(),
                ret.anyProblems()
        );
    }

    @Test
    void fromProblem_null_noTracing() {
        this.monitor.traceEnabled = true;
        final RetVoid ret = RetVoid.fromProblem((Problem) null);
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(
                List.of(),
                ret.anyProblems()
        );
    }

    @Test
    void fromProblems_collection_none_tracing() {
        this.monitor.traceEnabled = true;
        final RetVoid ret = RetVoid.fromProblems(List.of());
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(
                List.of(),
                ret.anyProblems()
        );
    }

    @Test
    void fromProblems_collection_none_noTracing() {
        this.monitor.traceEnabled = false;
        final RetVoid ret = RetVoid.fromProblems(List.of());
        // Should have used constant OK.
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(
                List.of(),
                ret.anyProblems()
        );
    }

    @Test
    void fromProblems_collection_empty_tracing() {
        this.monitor.traceEnabled = true;
        final RetVoid ret = RetVoid.fromProblems(List.of(), List.of());
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(
                List.of(),
                ret.anyProblems()
        );
    }

    @Test
    void fromProblems_collection_empty_noTracing() {
        this.monitor.traceEnabled = false;
        final RetVoid ret = RetVoid.fromProblems(List.of(), List.of());
        // Should have used constant OK.
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(
                List.of(),
                ret.anyProblems()
        );
    }

    @Test
    void fromProblems_collection_some_tracing() {
        this.monitor.traceEnabled = true;
        final LocalizedProblem problem = LocalizedProblem.from("1");
        final RetVoid ret = RetVoid.fromProblems(List.of(), List.of(
                new TestableProblemContainer(problem)));
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(
                List.of(problem),
                ret.anyProblems()
        );
    }

    @Test
    void fromProblems_collection_some_noTracing() {
        this.monitor.traceEnabled = false;
        final LocalizedProblem problem = LocalizedProblem.from("1");
        final RetVoid ret = RetVoid.fromProblems(List.of(), List.of(
                new TestableProblemContainer(problem)));
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(
                List.of(problem),
                ret.anyProblems()
        );
    }

    @Test
    void fromProblems_empty_tracing() {
        this.monitor.traceEnabled = true;
        final RetVoid ret = RetVoid.fromProblems(new TestableProblemContainer());
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(
                List.of(),
                ret.anyProblems()
        );
    }

    @Test
    void fromProblems_empty_noTracing() {
        this.monitor.traceEnabled = false;
        final RetVoid ret = RetVoid.fromProblems(new TestableProblemContainer());
        // Should have used constant OK.
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(
                List.of(),
                ret.anyProblems()
        );
    }

    @Test
    void fromProblems_some_tracing() {
        this.monitor.traceEnabled = true;
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVoid ret = RetVoid.fromProblems(new TestableProblemContainer(problem));
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(
                List.of(problem),
                ret.anyProblems()
        );
    }

    @Test
    void fromProblems_some_noTracing() {
        this.monitor.traceEnabled = false;
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVoid ret = RetVoid.fromProblems(new TestableProblemContainer(problem));
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(
                List.of(problem),
                ret.anyProblems()
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
