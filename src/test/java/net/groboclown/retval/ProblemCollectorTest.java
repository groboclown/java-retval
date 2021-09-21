// Released under the MIT License.
package net.groboclown.retval;

import java.util.ArrayList;
import java.util.List;
import net.groboclown.retval.monitor.MockProblemMonitor;
import net.groboclown.retval.problems.LocalizedProblem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProblemCollectorTest {
    MockProblemMonitor monitor;

    @Test
    void from() {
        final ProblemCollector pc = ProblemCollector.from();
        assertEquals(List.of(), pc.anyProblems());
        assertTrue(pc.isOk());
        assertFalse(pc.isProblem());
        assertFalse(pc.hasProblems());
    }

    @Test
    void from_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ProblemCollector pc = ProblemCollector.from(problem);
        assertEquals(List.of(problem), pc.anyProblems());
        assertFalse(pc.isOk());
        assertTrue(pc.isProblem());
        assertTrue(pc.hasProblems());
    }

    @Test
    void from_collectionProblem_empty() {
        final ProblemCollector pc = ProblemCollector.from(new ArrayList<>());
        assertEquals(List.of(), pc.anyProblems());
        assertTrue(pc.isOk());
        assertFalse(pc.isProblem());
        assertFalse(pc.hasProblems());
    }

    @Test
    void from_collectionProblem_some1() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ProblemCollector pc = ProblemCollector.from(List.of(problem));
        assertEquals(List.of(problem), pc.anyProblems());
        assertFalse(pc.isOk());
        assertTrue(pc.isProblem());
        assertTrue(pc.hasProblems());
    }

    @Test
    void from_collectionProblem_some2() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ProblemCollector pc = ProblemCollector.from(List.of(), List.of(problem));
        assertEquals(List.of(problem), pc.anyProblems());
        assertFalse(pc.isOk());
        assertTrue(pc.isProblem());
        assertTrue(pc.hasProblems());
    }

    @Test
    void from_RetVoid() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ProblemCollector pc = ProblemCollector.from(
                RetVoid.fromProblem(problem), RetVoid.ok()
        );
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), pc.anyProblems());
        assertFalse(pc.isOk());
        assertTrue(pc.isProblem());
        assertTrue(pc.hasProblems());
    }

    @Test
    void from_RetVal_ok() {
        final List<String> calledWith = new ArrayList<>();
        final ProblemCollector pc = ProblemCollector.from(RetVal.ok("a"), calledWith::add);
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of("a"), calledWith);
        assertEquals(List.of(), pc.anyProblems());
        assertTrue(pc.isOk());
        assertFalse(pc.isProblem());
        assertFalse(pc.hasProblems());
    }

    @Test
    void from_RetVal_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ProblemCollector pc = ProblemCollector.from(RetVal.fromProblem(problem), (v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), pc.anyProblems());
        assertFalse(pc.isOk());
        assertTrue(pc.isProblem());
        assertTrue(pc.hasProblems());
    }

    @Test
    void from_RetNullable_ok() {
        final List<String> calledWith = new ArrayList<>();
        final ProblemCollector pc = ProblemCollector.from(RetNullable.ok("a"), calledWith::add);
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of("a"), calledWith);
        assertEquals(List.of(), pc.anyProblems());
        assertTrue(pc.isOk());
        assertFalse(pc.isProblem());
        assertFalse(pc.hasProblems());
    }

    @Test
    void from_RetNullable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ProblemCollector pc = ProblemCollector.from(RetNullable.fromProblem(problem), (v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), pc.anyProblems());
        assertFalse(pc.isOk());
        assertTrue(pc.isProblem());
        assertTrue(pc.hasProblems());
    }

    @Test
    void with_RetVal_ok() {
        final List<String> calledWith = new ArrayList<>();
        final ProblemCollector pc = ProblemCollector.from();
        final ProblemCollector val = pc.with(RetVal.ok("a"), calledWith::add);
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertSame(val, pc);
        assertEquals(List.of("a"), calledWith);
        assertEquals(List.of(), pc.anyProblems());
        assertTrue(pc.isOk());
        assertFalse(pc.isProblem());
        assertFalse(pc.hasProblems());
    }

    @Test
    void with_RetVal_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ProblemCollector pc = ProblemCollector.from();
        final ProblemCollector val = pc.with(RetVal.fromProblem(problem), (v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertSame(val, pc);
        assertEquals(List.of(problem), pc.anyProblems());
        assertFalse(pc.isOk());
        assertTrue(pc.isProblem());
        assertTrue(pc.hasProblems());
    }

    @Test
    void with_RetNullable_ok() {
        final List<String> calledWith = new ArrayList<>();
        final ProblemCollector pc = ProblemCollector.from();
        final ProblemCollector val = pc.with(RetNullable.ok("a"), calledWith::add);
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertSame(pc, val);
        assertEquals(List.of("a"), calledWith);
        assertEquals(List.of(), pc.anyProblems());
        assertTrue(pc.isOk());
        assertFalse(pc.isProblem());
        assertFalse(pc.hasProblems());
    }

    @Test
    void with_RetNullable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ProblemCollector pc = ProblemCollector.from();
        final ProblemCollector val = pc.with(RetNullable.fromProblem(problem), (v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertSame(pc, val);
        assertEquals(List.of(problem), pc.anyProblems());
        assertFalse(pc.isOk());
        assertTrue(pc.isProblem());
        assertTrue(pc.hasProblems());
    }

    @Test
    void withProblem() {
    }

    @Test
    void withProblems() {
    }

    @Test
    void withValue() {
    }

    @Test
    void validateEach() {

    }

    @Test
    void then_ok() {
        final RetVal<String> res = RetVal.ok("x");
        final ProblemCollector pc = ProblemCollector.from();
        final RetVal<String> ret = pc.then(() -> res);
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertSame(ret, res);
    }

    @Test
    void then_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ProblemCollector pc = ProblemCollector.from(problem);
        final RetVal<String> ret = pc.then(() -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), ret.anyProblems());
    }

    @Test
    void thenValue_ok() {
        final int[] callCount = {0};
        final RetVal<String> ret = ProblemCollector.from().thenValue(() -> {
            callCount[0]++;
            return "x";
        });
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(1, callCount[0]);
        assertEquals(List.of(), ret.anyProblems());
        assertEquals("x", ret.getValue());
    }

    @Test
    void thenValue_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVal<String> ret = ProblemCollector.from(problem).thenValue(() -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), ret.anyProblems());
    }

    @Test
    void thenNullable_ok() {
        final RetNullable<String> res = RetNullable.ok("x");
        final ProblemCollector pc = ProblemCollector.from();
        final RetNullable<String> ret = pc.thenNullable(() -> res);
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertSame(ret, res);
    }

    @Test
    void thenNullable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ProblemCollector pc = ProblemCollector.from(problem);
        final RetNullable<String> ret = pc.thenNullable(() -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), ret.anyProblems());
    }

    @Test
    void thenNullableValue_ok() {
        final int[] callCount = {0};
        final RetNullable<String> ret = ProblemCollector.from().thenNullableValue(() -> {
            callCount[0]++;
            return "x";
        });
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(1, callCount[0]);
        assertEquals(List.of(), ret.anyProblems());
        assertEquals("x", ret.getValue());
    }

    @Test
    void thenNullableValue_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetNullable<String> ret = ProblemCollector
            .from(problem)
            .thenNullableValue(() -> {
                throw new IllegalStateException("should not be called");
            });
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), ret.anyProblems());
    }

    @Test
    void thenRun_ok() {
        final int[] callCount = {0};
        final RetVoid ret = ProblemCollector.from().thenRun(() -> {
            callCount[0]++;
        });
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(1, callCount[0]);
        assertEquals(List.of(), ret.anyProblems());
        assertTrue(ret.isOk());
    }

    @Test
    void thenRun_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVoid ret = ProblemCollector.from(problem).thenRun(() -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), ret.anyProblems());
    }

    @Test
    void complete_ok() {
        final RetVal<String> ret = ProblemCollector.from().complete("x");
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(List.of(), ret.anyProblems());
        assertEquals("x", ret.result());
    }

    @Test
    void complete_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVal<String> ret = ProblemCollector.from(problem).complete("x");
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), ret.anyProblems());
    }

    @Test
    void completeNullable_ok() {
        final RetNullable<String> ret = ProblemCollector.from().completeNullable("x");
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(List.of(), ret.anyProblems());
        assertEquals("x", ret.result());
    }

    @Test
    void completeNullable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetNullable<String> ret = ProblemCollector.from(problem).completeNullable("x");
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), ret.anyProblems());
    }

    @Test
    void asWarning() {
    }

    @Test
    void isProblem() {
    }

    @Test
    void hasProblems() {
    }

    @Test
    void isOk() {
    }

    @Test
    void anyProblems() {
    }

    @Test
    void validProblems() {
    }

    @Test
    void debugProblems() {
    }

    @Test
    void joinProblemsWith() {
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