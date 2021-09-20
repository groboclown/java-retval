// Released under the MIT License.

package net.groboclown.retval;

import java.util.List;
import net.groboclown.retval.monitor.MockProblemMonitor;
import net.groboclown.retval.problems.LocalizedProblem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RetVoidTest {
    MockProblemMonitor monitor;

    // isProblem is an alias for hasProblems, so only test out isProblem.
    @Test
    void isProblem_ok() {
        final RetVoid res = RetVoid.ok();
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertFalse(res.isProblem());
        // for ok state, this acts as an observation
        assertEquals(List.of(), this.monitor.getNeverObserved());
    }

    @Test
    void isProblem_problem() {
        final RetVoid res = RetVoid.fromProblem(LocalizedProblem.from("x"));
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertTrue(res.isProblem());
        // for problem state, this does not act as an observation
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    @Test
    void isOk_ok() {
        final RetVoid res = RetVoid.ok();
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertTrue(res.isOk());
        // for ok state, this acts as an observation
        assertEquals(List.of(), this.monitor.getNeverObserved());
    }

    @Test
    void isOk_problem() {
        final RetVoid res = RetVoid.fromProblem(LocalizedProblem.from("x"));
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertFalse(res.isOk());
        // for problem state, this does not act as an observation
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    @Test
    void anyProblems_ok() {
        final RetVoid res = RetVoid.ok();
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(), res.anyProblems());
        // this acts as an observation
        assertEquals(List.of(), this.monitor.getNeverObserved());
    }

    @Test
    void anyProblems_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVoid res = RetVoid.fromProblem(problem);
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), res.anyProblems());
        // this acts as an observation
        assertEquals(List.of(), this.monitor.getNeverObserved());
    }

    @Test
    void validProblems_ok() {
        final RetVoid res = RetVoid.ok();
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        try {
            res.validProblems();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // Don't inspect exception
        }
        // this acts as an observation, even in the error state.
        assertEquals(List.of(), this.monitor.getNeverObserved());
    }

    @Test
    void validProblems_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVoid res = RetVoid.fromProblem(problem);
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), res.validProblems());
        // this acts as an observation
        assertEquals(List.of(), this.monitor.getNeverObserved());
    }

    @Test
    void debugProblems_ok() {
        final RetVoid res = RetVoid.ok();
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(
                "",
                res.debugProblems(";")
        );
        // this does not act as an observation
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    @Test
    void debugProblems_problem() {
        final RetVoid res = RetVoid.fromProblem(LocalizedProblem.from("x"));
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(
                "x",
                res.debugProblems(";")
        );
        // this does not act as an observation
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    @Test
    void then_ok() {
        final RetVoid orig = RetVoid.ok();
        assertEquals(List.of(orig), this.monitor.getNeverObserved());
        final RetVal<String> expected = RetVal.ok("x");
        final RetVal<String> ret = orig.then(() -> expected);
        // observation passes to the ret value.
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertSame(expected, ret);
    }

    @Test
    void then_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVoid orig = RetVoid.fromProblem(problem);
        assertEquals(List.of(orig), this.monitor.getNeverObserved());
        final RetVal<Object> ret = orig.then(() -> {
            throw new IllegalStateException("should never be called");
        });
        // observation state passes to the returned value
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), ret.anyProblems());
    }

    @Test
    void map_ok() {
        final RetVoid orig = RetVoid.ok();
        assertEquals(List.of(orig), this.monitor.getNeverObserved());
        final RetVal<String> ret = orig.map(() -> "x");
        // observation passes to the ret value.
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals("x", ret.getValue());
    }

    @Test
    void map_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVoid orig = RetVoid.fromProblem(problem);
        assertEquals(List.of(orig), this.monitor.getNeverObserved());
        final RetVal<Object> ret = orig.map(() -> {
            throw new IllegalStateException("should never be called");
        });
        // observation state passes to the returned value
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), ret.anyProblems());
    }

    @Test
    void thenNullable_ok() {
        final RetVoid orig = RetVoid.ok();
        assertEquals(List.of(orig), this.monitor.getNeverObserved());
        final RetNullable<String> expected = RetNullable.ok("x");
        final RetNullable<String> ret = orig.thenNullable(() -> expected);
        // observation passes to the ret value.
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertSame(expected, ret);
    }

    @Test
    void thenNullable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVoid orig = RetVoid.fromProblem(problem);
        assertEquals(List.of(orig), this.monitor.getNeverObserved());
        final RetNullable<Object> ret = orig.thenNullable(() -> {
            throw new IllegalStateException("should never be called");
        });
        // observation state passes to the returned value
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), ret.anyProblems());
    }

    @Test
    void mapNullable_ok() {
        final RetVoid orig = RetVoid.ok();
        assertEquals(List.of(orig), this.monitor.getNeverObserved());
        final RetNullable<String> ret = orig.mapNullable(() -> "x");
        // observation passes to the ret value.
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals("x", ret.getValue());
    }

    @Test
    void mapNullable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVoid orig = RetVoid.fromProblem(problem);
        assertEquals(List.of(orig), this.monitor.getNeverObserved());
        final RetNullable<Object> ret = orig.mapNullable(() -> {
            throw new IllegalStateException("should never be called");
        });
        // observation state passes to the returned value
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), ret.anyProblems());
    }

    @Test
    void thenVoid_ok() {
        final RetVoid orig = RetVoid.ok();
        assertEquals(List.of(orig), this.monitor.getNeverObserved());
        final RetVoid expected = RetVoid.fromProblem(LocalizedProblem.from("x"));
        final RetVoid ret = orig.thenVoid(() -> expected);
        // observation passes to the ret value.
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertSame(expected, ret);
    }

    @Test
    void thenVoid_problem() {
        final RetVoid orig = RetVoid.fromProblem(LocalizedProblem.from("x"));
        assertEquals(List.of(orig), this.monitor.getNeverObserved());
        final RetVoid ret = orig.thenVoid(() -> {
            throw new IllegalStateException("should never be called");
        });
        // observation state stays with the original value
        assertEquals(List.of(orig), this.monitor.getNeverObserved());
        // indeed, the values are the same.
        assertSame(orig, ret);
    }

    @Test
    void toString_ok() {
        final RetVoid res = RetVoid.ok();
        assertEquals(
                "RetVoid(ok)",
                res.toString()
        );
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    @Test
    void toString_1problem() {
        final RetVoid res = RetVoid.fromProblem(LocalizedProblem.from("p1"));
        assertEquals(
                "RetVoid(1 problems: p1)",
                res.toString()
        );
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    @Test
    void toString_2problems() {
        final RetVoid res = RetVoid.fromProblem(
                LocalizedProblem.from("p1"), LocalizedProblem.from("p2")
        );
        assertEquals(
                "RetVoid(2 problems: p1; p2)",
                res.toString()
        );
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }


    @BeforeEach
    void beforeEach() {
        this.monitor = MockProblemMonitor.setup();
        // In all cases, we want tracing enabled to ensure we track observation status.
        this.monitor.traceEnabled = true;
    }

    @AfterEach
    void afterEach() {
        this.monitor.tearDown();
    }
}
