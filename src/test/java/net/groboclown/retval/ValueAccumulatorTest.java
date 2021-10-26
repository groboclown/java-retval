// Released under the MIT License.
package net.groboclown.retval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.groboclown.retval.monitor.MockProblemMonitor;
import net.groboclown.retval.problems.LocalizedProblem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ValueAccumulatorTest {
    MockProblemMonitor monitor;

    @Test
    void from_empty() {
        final ValueAccumulator<Object> accumulator = ValueAccumulator.from();
        assertEquals(List.of(), accumulator.anyProblems());
        assertEquals(List.of(), new ArrayList<>(accumulator.getValues()));
    }

    @Test
    void from_RetVal_ok() {
        final ValueAccumulator<String> accumulator = ValueAccumulator.from(RetVal.ok("x"));
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(), accumulator.anyProblems());
        assertEquals(List.of("x"), new ArrayList<>(accumulator.getValues()));
    }

    @Test
    void from_RetVal_problems() {
        final LocalizedProblem problem = LocalizedProblem.from("a");
        final ValueAccumulator<String> accumulator =
                ValueAccumulator.from(RetVal.fromProblem(problem));
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), accumulator.anyProblems());
        assertEquals(List.of(), new ArrayList<>(accumulator.getValues()));
    }

    @Test
    void from_RetNullable_ok() {
        final ValueAccumulator<String> accumulator = ValueAccumulator.from(RetNullable.ok("x"));
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(), accumulator.anyProblems());
        assertEquals(List.of("x"), new ArrayList<>(accumulator.getValues()));
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
        assertEquals(List.of(), new ArrayList<>(accumulator.getValues()));
    }

    @Test
    void from_problems() {
        final LocalizedProblem problem1 = LocalizedProblem.from("a");
        final LocalizedProblem problem2 = LocalizedProblem.from("b");
        final ValueAccumulator<String> accumulator =
                ValueAccumulator.from(problem1, problem2);
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(problem1, problem2), accumulator.anyProblems());
        assertEquals(List.of(), new ArrayList<>(accumulator.getValues()));
    }

    @Test
    void with_problems() {
        final LocalizedProblem problem1 = LocalizedProblem.from("p1");
        final LocalizedProblem problem2 = LocalizedProblem.from("p2");
        final ValueAccumulator<String> accumulator = ValueAccumulator.from(RetVal.ok("a"));
        assertEquals(List.of(), this.monitor.getNeverObserved());
        accumulator.with(problem1, problem2);
        assertEquals(List.of(problem1, problem2), accumulator.anyProblems());
        assertEquals(List.of("a"), new ArrayList<>(accumulator.getValues()));
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
        assertEquals(List.of("a"), new ArrayList<>(accumulator.getValues()));
    }

    @Test
    void withAllValues_vararg_1() {
        final ValueAccumulator<Character> accumulator = ValueAccumulator.from(RetVal.ok('z'));
        final ValueAccumulator<Character> ret = accumulator.withAllValues('c');

        assertSame(accumulator, ret);
        assertEquals(List.of('z', 'c'), accumulator.asRetValList().getValue());
    }

    @Test
    void withAllValues_vararg_3() {
        final ValueAccumulator<Character> accumulator = ValueAccumulator.from(RetVal.ok('x'));
        final ValueAccumulator<Character> ret = accumulator.withAllValues('c', 'i', 'd');

        assertSame(accumulator, ret);
        assertEquals(List.of('x', 'c', 'i', 'd'), accumulator.asRetValList().getValue());
    }

    @Test
    void withAllValues_iterators_1_empty() {
        final ValueAccumulator<Character> accumulator = ValueAccumulator.from(RetVal.ok('u'));
        final ValueAccumulator<Character> ret = accumulator.withAllValues(List.of());

        assertSame(accumulator, ret);
        assertEquals(List.of('u'), accumulator.asRetValList().getValue());
    }

    @Test
    void withAllValues_iterators_1() {
        final ValueAccumulator<Character> accumulator = ValueAccumulator.from(RetVal.ok('t'));
        final ValueAccumulator<Character> ret = accumulator.withAllValues(List.of('a', 'x'));

        assertSame(accumulator, ret);
        assertEquals(List.of('t', 'a', 'x'), accumulator.asRetValList().getValue());
    }

    @Test
    void withAllValues_iterators_3() {
        final ValueAccumulator<Character> accumulator = ValueAccumulator.from(RetVal.ok('w'));
        final ValueAccumulator<Character> ret = accumulator.withAllValues(
                List.of('a', 'x'),
                List.of('e', 'm'));

        assertSame(accumulator, ret);
        assertEquals(List.of('w', 'a', 'x', 'e', 'm'), accumulator.asRetValList().getValue());
    }

    @Test
    void addValue() {
        final ValueAccumulator<Character> accumulator = ValueAccumulator.from(RetVal.ok('p'));
        accumulator.addValue('a');
        assertEquals(List.of('p', 'a'), accumulator.asRetValList().getValue());
    }

    @Test
    void addValue_null() {
        final ValueAccumulator<Character> accumulator = ValueAccumulator.from();
        accumulator.addValue(null);
        assertEquals(Collections.singletonList(null), accumulator.asRetValList().getValue());
    }

    @Test
    void addAllValues() {
        final ValueAccumulator<Character> accumulator = ValueAccumulator.from(RetVal.ok('b'));
        accumulator.addAllValues(List.of('a', 'l', 'b', 'a'));
        assertEquals(List.of('b', 'a', 'l', 'b', 'a'), accumulator.asRetValList().getValue());
    }

    @Test
    void addAllValues_null() {
        final ValueAccumulator<Character> accumulator = ValueAccumulator.from();
        accumulator.addAllValues(Arrays.asList('a', null));
        assertEquals(Arrays.asList('a', null), accumulator.asRetValList().getValue());
    }

    @Test
    void withAll_retval_ok() {
        final ValueAccumulator<Character> accumulator = ValueAccumulator.from(RetVal.ok('v'));
        accumulator.withAll(RetVal.ok(List.of('!')));
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(Arrays.asList('v', '!'), accumulator.asRetValList().getValue());
    }

    @Test
    void withAll_retval_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("1");
        final ValueAccumulator<Character> accumulator = ValueAccumulator.from(RetVal.ok('v'));
        accumulator.withAll(RetVal.fromProblem(problem));
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), accumulator.asRetVal().anyProblems());
    }

    @Test
    void withAll_retnullable_ok() {
        final ValueAccumulator<Character> accumulator = ValueAccumulator.from(RetVal.ok('v'));
        accumulator.withAll(RetNullable.ok(List.of('!')));
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(Arrays.asList('v', '!'), accumulator.asRetValList().getValue());
    }

    @Test
    void withAll_retnullable_null() {
        final ValueAccumulator<Character> accumulator = ValueAccumulator.from(RetVal.ok('v'));
        accumulator.withAll(RetNullable.ok(null));
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of('v'), accumulator.asRetValList().getValue());
    }

    @Test
    void withAll_retnullable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("1");
        final ValueAccumulator<Character> accumulator = ValueAccumulator.from(RetVal.ok('v'));
        accumulator.withAll(RetNullable.fromProblem(problem));
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), accumulator.asRetVal().anyProblems());
    }

    @Test
    void add_retval_ok() {
        final ValueAccumulator<Character> accumulator = ValueAccumulator.from(RetVal.ok('u'));
        accumulator.add(RetVal.ok('p'));
        assertEquals(List.of('u', 'p'), new ArrayList<>(accumulator.getValues()));
    }

    @Test
    void add_retval_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("1");
        final ValueAccumulator<Character> accumulator = ValueAccumulator.from(RetVal.ok('u'));
        accumulator.add(RetVal.fromProblem(problem));
        assertEquals(List.of(problem), accumulator.anyProblems());
        assertEquals(List.of('u'), new ArrayList<>(accumulator.getValues()));
    }

    @Test
    void add_retnullable_ok() {
        final ValueAccumulator<Character> accumulator = ValueAccumulator.from(RetVal.ok('u'));
        accumulator.add(RetNullable.ok('p'));
        assertEquals(List.of('u', 'p'), new ArrayList<>(accumulator.getValues()));
    }

    @Test
    void add_retnullable_ok_null() {
        final ValueAccumulator<Character> accumulator = ValueAccumulator.from(RetVal.ok('u'));
        accumulator.add(RetNullable.ok(null));
        assertEquals(Arrays.asList('u', null), new ArrayList<>(accumulator.getValues()));
    }

    @Test
    void add_retnullable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("1");
        final ValueAccumulator<Character> accumulator = ValueAccumulator.from(RetVal.ok('u'));
        accumulator.add(RetNullable.fromProblem(problem));
        assertEquals(List.of(problem), accumulator.anyProblems());
        assertEquals(List.of('u'), new ArrayList<>(accumulator.getValues()));
    }

    @Test
    void add_vpc_ok() {
        final ValueAccumulator<Character> accumulator = ValueAccumulator.from(RetVal.ok('u'));
        accumulator.add(new TestableValuedProblemContainer<>('p'));
        assertEquals(List.of('u', 'p'), new ArrayList<>(accumulator.getValues()));
    }

    @Test
    void add_vpc_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("1");
        final ValueAccumulator<Character> accumulator = ValueAccumulator.from(RetVal.ok('u'));
        accumulator.add(new TestableValuedProblemContainer<Character>(problem));
        assertEquals(List.of(problem), accumulator.anyProblems());
        assertEquals(List.of('u'), new ArrayList<>(accumulator.getValues()));
    }

    @Test
    void addAll_ok() {
        final ValueAccumulator<Character> accumulator = ValueAccumulator.from(RetVal.ok('u'));
        accumulator.addAll(Set.of(
                RetVal.ok('v'),
                RetNullable.ok(null),
                new TestableValuedProblemContainer<>('p')));
        assertEquals(List.of(), this.monitor.getNeverObserved());
        final Set<Character> expected = new HashSet<>();
        expected.add('u');
        expected.add('v');
        expected.add('p');
        expected.add(null);
        assertEquals(expected, new HashSet<>(accumulator.getValues()));
        assertEquals(List.of(), accumulator.anyProblems());
    }

    @Test
    void addAll_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("1");
        final ValueAccumulator<Character> accumulator = ValueAccumulator.from(RetVal.ok('u'));
        accumulator.addAll(List.of(
                RetVal.ok('v'),
                RetNullable.fromProblem(problem),
                new TestableValuedProblemContainer<>('p')));
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of('u', 'v', 'p'), new ArrayList<>(accumulator.getValues()));
        assertEquals(List.of(problem), accumulator.anyProblems());
    }

    @Test
    void getCollector() {
        final LocalizedProblem problem = LocalizedProblem.from("1");
        final ValueAccumulator<Character> accumulator = ValueAccumulator.from(RetVal.ok('v'));
        assertEquals(List.of(), accumulator.getCollector().anyProblems());
        accumulator.with(RetVal.fromProblem(problem));
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), accumulator.getCollector().anyProblems());
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
        assertEquals(calledWith, new ArrayList<>(accumulator.getValues()));
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
        assertEquals(List.of(), new ArrayList<>(accumulator.getValues()));
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
        assertEquals(calledWith, new ArrayList<>(accumulator.getValues()));
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
        assertEquals(List.of(), new ArrayList<>(accumulator.getValues()));
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
        assertEquals(List.of(), new ArrayList<>(accumulator.getValues()));
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
        assertEquals(List.of("x", "y"), new ArrayList<>(res.result()));
    }

    @Test
    void asRetVal_problems() {
        final LocalizedProblem problem = LocalizedProblem.from("1");
        final ValueAccumulator<String> accumulator = ValueAccumulator.from();
        final RetVal<Collection<String>> res = accumulator
                // Simulate a use case flow.  "asRetVal" implies end-of-execution.
                .with(RetVal.fromProblem(problem))
                .with(RetVal.ok("y"))
                .asRetVal();
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void asRetValList_ok() {
        final ValueAccumulator<String> accumulator = ValueAccumulator.from();
        final RetVal<List<String>> res = accumulator
                // Simulate a use case flow.  "asRetVal" implies end-of-execution.
                .with(RetVal.ok("x"))
                .with(RetVal.ok("y"))
                .asRetValList();
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(), res.anyProblems());
        assertEquals(List.of("x", "y"), new ArrayList<>(res.result()));
    }

    @Test
    void asRetValList_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("1");
        final ValueAccumulator<String> accumulator = ValueAccumulator.from();
        final RetVal<List<String>> res = accumulator
                // Simulate a use case flow.  "asRetVal" implies end-of-execution.
                .with(RetVal.fromProblem(problem))
                .with(RetVal.ok("y"))
                .asRetValList();
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), res.anyProblems());
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
        assertEquals(List.of("x", "y"), new ArrayList<>(res.getValue()));
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
        final List<Problem> joined = new ArrayList<>();
        final LocalizedProblem problem1 = LocalizedProblem.from("1");
        joined.add(problem1);
        final LocalizedProblem problem2 = LocalizedProblem.from("2");
        final ValueAccumulator<String> accumulator = ValueAccumulator.from();
        accumulator.with(RetVal.fromProblem(problem2));
        accumulator.joinProblemsWith(joined);
        assertEquals(List.of(problem1, problem2), joined);
        assertEquals(List.of(problem2), accumulator.anyProblems());
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