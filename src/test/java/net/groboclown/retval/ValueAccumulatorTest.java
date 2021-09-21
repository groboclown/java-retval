// Released under the MIT License.
package net.groboclown.retval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import net.groboclown.retval.monitor.MockProblemMonitor;
import net.groboclown.retval.problems.LocalizedProblem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValueAccumulatorTest {
    MockProblemMonitor monitor;

    @Test
    void from_empty() {
        final ValueAccumulator<Object> accumulator = ValueAccumulator.from();
        assertEquals(List.of(), accumulator.anyProblems());
        assertEquals(List.of(), List.copyOf(accumulator.getValues()));
    }

    @Test
    void from_RetVal_ok() {
        final ValueAccumulator<String> accumulator = ValueAccumulator.from(RetVal.ok("x"));
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(), accumulator.anyProblems());
        assertEquals(List.of("x"), List.copyOf(accumulator.getValues()));
    }

    @Test
    void from_RetVal_problems() {
        final LocalizedProblem problem = LocalizedProblem.from("a");
        final ValueAccumulator<String> accumulator =
                ValueAccumulator.from(RetVal.fromProblem(problem));
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), accumulator.anyProblems());
        assertEquals(List.of(), List.copyOf(accumulator.getValues()));
    }

    @Test
    void from_RetNullable_ok() {
        final ValueAccumulator<String> accumulator = ValueAccumulator.from(RetNullable.ok("x"));
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(), accumulator.anyProblems());
        assertEquals(List.of("x"), List.copyOf(accumulator.getValues()));
    }

    @Test
    void from_RetNullable_ok_null() {
        final ValueAccumulator<String> accumulator = ValueAccumulator.from(RetNullable.ok(null));
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(), accumulator.anyProblems());
        final Collection<String> values = accumulator.getValues();
        assertEquals(1, values.size());
        assertTrue(values.contains(null));
    }

    @Test
    void from_RetNullable_problems() {
        final LocalizedProblem problem = LocalizedProblem.from("a");
        final ValueAccumulator<String> accumulator =
                ValueAccumulator.from(RetNullable.fromProblem(problem));
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), accumulator.anyProblems());
        assertEquals(List.of(), List.copyOf(accumulator.getValues()));
    }

    @Test
    void from_problems() {
        final LocalizedProblem problem1 = LocalizedProblem.from("a");
        final LocalizedProblem problem2 = LocalizedProblem.from("b");
        final ValueAccumulator<String> accumulator =
                ValueAccumulator.from(problem1, problem2);
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(problem1, problem2), accumulator.anyProblems());
        assertEquals(List.of(), List.copyOf(accumulator.getValues()));
    }

    @Test
    void with_problems() {
        final LocalizedProblem problem1 = LocalizedProblem.from("p1");
        final LocalizedProblem problem2 = LocalizedProblem.from("p2");
        final ValueAccumulator<String> accumulator = ValueAccumulator.from(RetVal.ok("a"));
        assertEquals(List.of(), this.monitor.getNeverObserved());
        accumulator.with(problem1, problem2);
        assertEquals(List.of(problem1, problem2), accumulator.anyProblems());
        assertEquals(List.of("a"), List.copyOf(accumulator.getValues()));
    }

    @Test
    void with_problemCollections() {
        final LocalizedProblem problem1 = LocalizedProblem.from("p1");
        final LocalizedProblem problem2 = LocalizedProblem.from("p2");
        final ValueAccumulator<String> accumulator = ValueAccumulator.from(RetVal.ok("a"));
        assertEquals(List.of(), this.monitor.getNeverObserved());
        final ValueAccumulator<String> ret =
                accumulator.with(List.of(problem1), Arrays.asList(problem2, null));
        assertSame(ret, accumulator);
        assertEquals(List.of(problem1, problem2), accumulator.anyProblems());
        assertEquals(List.of("a"), List.copyOf(accumulator.getValues()));
    }

    @Test
    void withEach_Collection_ok() {
        final List<String> calledWith = new ArrayList<>();
        final ValueAccumulator<String> accumulator = ValueAccumulator.from();
        final ValueAccumulator<String> ret = accumulator
                .withEach(List.of("a", "b", "c"), (v) -> {
                    calledWith.add(v);
                    return RetVal.ok(Objects.requireNonNull(v));
                });
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertSame(ret, accumulator);
        assertEquals(List.of("a", "b", "c"), calledWith);
        assertEquals(calledWith, List.copyOf(accumulator.getValues()));
        assertEquals(List.of(), accumulator.anyProblems());
    }

    @Test
    void withEach_Collection_problem() {
        final List<String> calledWith = new ArrayList<>();
        final ValueAccumulator<String> accumulator = ValueAccumulator.from(
                // Ensure with a starting problem, the withEach still calls.
                LocalizedProblem.from("x")
        );
        final ValueAccumulator<String> ret = accumulator
                .withEach(List.of("a", "b", "c"), (v) -> {
                    calledWith.add(v);
                    return RetVal.fromProblem(LocalizedProblem.from(Objects.requireNonNull(v)));
                });
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertSame(ret, accumulator);
        assertEquals(List.of("a", "b", "c"), calledWith);
        assertEquals(List.of(), List.copyOf(accumulator.getValues()));
        assertEquals("x;a;b;c", accumulator.debugProblems(";"));
    }

    @Test
    void withEach_RetVal_ok() {
        final List<String> calledWith = new ArrayList<>();
        final ValueAccumulator<String> accumulator = ValueAccumulator.from();
        final ValueAccumulator<String> ret = accumulator
                .withEach(RetVal.ok(List.of("a", "b", "c")), (v) -> {
                    calledWith.add(v);
                    return RetVal.ok(Objects.requireNonNull(v));
                });
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertSame(ret, accumulator);
        assertEquals(List.of("a", "b", "c"), calledWith);
        assertEquals(calledWith, List.copyOf(accumulator.getValues()));
        assertEquals(List.of(), accumulator.anyProblems());
    }

    @Test
    void withEach_RetVal_problem() {
        final List<String> calledWith = new ArrayList<>();
        final ValueAccumulator<String> accumulator = ValueAccumulator.from(
                // Ensure with a starting problem, the withEach still calls.
                LocalizedProblem.from("x")
        );
        final ValueAccumulator<String> ret = accumulator
                .withEach(RetVal.ok(List.of("a", "b", "c")), (v) -> {
                    calledWith.add(v);
                    return RetVal.fromProblem(LocalizedProblem.from(Objects.requireNonNull(v)));
                });
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertSame(ret, accumulator);
        assertEquals(List.of("a", "b", "c"), calledWith);
        assertEquals(List.of(), List.copyOf(accumulator.getValues()));
        assertEquals("x;a;b;c", accumulator.debugProblems(";"));
    }

    @Test
    void withEach_RetVal_withProblem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ValueAccumulator<String> accumulator = ValueAccumulator.from();
        final ValueAccumulator<String> ret = accumulator
                .withEach(RetVal.fromProblem(problem), (v) -> {
                    throw new IllegalStateException("should not be called");
                });
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertSame(ret, accumulator);
        assertEquals(List.of(), List.copyOf(accumulator.getValues()));
        assertEquals(List.of(problem), accumulator.anyProblems());
    }

    @Test
    void then_ok() {
        final ValueAccumulator<String> accumulator = ValueAccumulator.from();
        final RetVal<String> res = accumulator
                // Simulate a use case flow.  "then" implies continued
                // execution.
                .with(RetVal.ok("x"))
                .with(RetVal.ok("y"))
                .then()
                .then((v) -> RetVal.ok(String.join(",", v) + "-z"));
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(), res.anyProblems());
        assertEquals("x,y-z", res.result());
    }

    @Test
    void then_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final ValueAccumulator<String> accumulator = ValueAccumulator.from();
        final RetVal<String> res = accumulator
                .with(RetVal.ok("x"))
                .with(problem)
                .then()
                .then((v) -> RetVal.ok(v + "y"));
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void asRetVal_ok() {
        final ValueAccumulator<String> accumulator = ValueAccumulator.from();
        final RetVal<Collection<String>> res = accumulator
                // Simulate a use case flow.  "asRetVal" implies end-of-execution.
                .with(RetVal.ok("x"))
                .with(RetVal.ok("y"))
                .asRetVal();
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(), res.anyProblems());
        assertEquals(List.of("x", "y"), List.copyOf(res.result()));
    }

    @Test
    void asWarning() {
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final ValueAccumulator<String> accumulator = ValueAccumulator.from();
        final WarningVal<Collection<String>> res = accumulator
                .with(problem)
                .with(RetVal.ok("x"))
                .with(RetVal.ok("y"))
                .asWarning();
        // Warnings are not observed
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), res.anyProblems());
        assertEquals(List.of("x", "y"), List.copyOf(res.getValue()));
    }

    @Test
    void isProblem_ok() {
        final ValueAccumulator<String> accumulator = ValueAccumulator.from();
        assertFalse(accumulator.isProblem());
        assertFalse(accumulator.hasProblems());
        assertTrue(accumulator.isOk());

        accumulator.with(LocalizedProblem.from("x"));
        assertTrue(accumulator.isProblem());
        assertTrue(accumulator.hasProblems());
        assertFalse(accumulator.isOk());

        accumulator.with(RetVal.ok("x"));
        assertTrue(accumulator.isProblem());
        assertTrue(accumulator.hasProblems());
        assertFalse(accumulator.isOk());
    }

    @Test
    void validProblems_ok() {
        final ValueAccumulator<String> accumulator = ValueAccumulator.from();
        try {
            accumulator.validProblems();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // do not probe the exception
        }
    }

    @Test
    void debugProblems() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ValueAccumulator<String> accumulator = ValueAccumulator.from(problem);
        assertEquals(List.of(problem), accumulator.validProblems());
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