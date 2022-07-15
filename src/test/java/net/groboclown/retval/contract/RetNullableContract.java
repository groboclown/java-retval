// Released under the MIT License. 
package net.groboclown.retval.contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.groboclown.retval.Problem;
import net.groboclown.retval.ProblemCollector;
import net.groboclown.retval.RetNullable;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.RetVoid;
import net.groboclown.retval.function.NonnullReturnFunction;
import net.groboclown.retval.problems.LocalizedProblem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Contract test for the {@link RetNullable} interface.  All implementations of this interface
 * must pass these tests.
 */
public abstract class RetNullableContract {
    @Nonnull
    protected abstract <T> RetNullable<T> createForNullable(@Nullable T value);

    /**
     * This should be the same underlying API usage as the factory requires.  The contract test
     * will ensure the correct invocation such that it conforms to the factory input requirements.
     *
     * @param problems list of problems, which is guaranteed to conform to the factory requirements.
     * @param <T> type of the nullable
     * @return the nullable type under contract test
     */
    @Nonnull
    protected abstract <T> RetNullable<T> createForNullableProblems(
            @Nonnull List<Problem> problems);

    // ----------------------------------------------------------------------
    // getValue()

    @Test
    public void getValue_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetNullable<Object> res = createForNullableProblems(List.of(problem));
        assertNull(res.getValue());
    }

    @Test
    public void getValue_ok() {
        final RetNullable<String> res = createForNullable("a");
        assertEquals("a", res.getValue());
    }

    // ----------------------------------------------------------------------
    // asOptional()

    @Test
    public void asOptional_problem() {
        final RetNullable<Object> res = createForNullableProblems(
                List.of(LocalizedProblem.from("x")));
        assertFalse(res.asOptional().isPresent());
    }

    @Test
    void asOptional_ok() {
        final RetNullable<String> res = createForNullable("ab");
        assertTrue(res.asOptional().isPresent());
        assertEquals("ab", res.asOptional().get());
    }

    @Test
    void asOptional_ok_null() {
        final RetNullable<String> res = createForNullable(null);
        assertFalse(res.asOptional().isPresent());
    }

    @Test
    void requireOptional_problem() {
        final RetNullable<Object> res = createForNullableProblems(
                List.of(LocalizedProblem.from("x")));
        try {
            res.requireOptional();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // don't inspect the exception
        }
    }

    @Test
    void requireOptional_ok() {
        final RetNullable<String> res = createForNullable("a");
        final Optional<String> optional = res.requireOptional();
        assertTrue(optional.isPresent());
        assertEquals("a", optional.get());
    }

    @Test
    void requireOptional_ok_null() {
        final RetNullable<String> res = createForNullable(null);
        assertFalse(res.requireOptional().isPresent());
    }

    @Test
    void result_problem() {
        final RetNullable<Object> res = createForNullableProblems(
                List.of(LocalizedProblem.from("x")));
        try {
            res.result();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
    }

    @Test
    void result_ok_not_checked() {
        // Test the explicit rules around observability with this call.
        final RetNullable<Long> res = createForNullable(Long.MIN_VALUE);

        // Call and check result.  Note this is done without the "isOk" wrapper.
        assertEquals(Long.MIN_VALUE, res.result());
    }

    @Test
    void result_default_problem() {
        final RetNullable<Object> res = createForNullableProblems(
                List.of(LocalizedProblem.from("x")));
        try {
            res.result(new Object());
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
    }

    @Test
    void result_default_nonnull_ok_not_checked() {
        // Test the explicit rules around observability with this call.
        final RetNullable<Long> res = createForNullable(Long.MIN_VALUE);

        // Call and check result.  Note this is done without the "isOk" wrapper.
        assertEquals(Long.MIN_VALUE, res.result(10L));
    }

    @Test
    void result_default_null_ok_not_checked() {
        // Test the explicit rules around observability with this call.
        final RetNullable<String> res = createForNullable(null);

        // Call and check result.  Note this is done without the "isOk" wrapper.
        assertEquals("foo", res.result("foo"));
    }

    @Test
    void forwardProblems_ok() {
        final RetNullable<String> res = createForNullable("a");
        try {
            res.forwardProblems();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
    }

    @Test
    void forwardProblems_problems_traceEnabled() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetNullable<String> res = createForNullableProblems(List.of(problem));
        // Notice the implicit API usage check for altering the signature to an incompatible
        // type.
        final RetVal<Integer> forwarded = res.forwardProblems();

        assertEquals(List.of(problem), forwarded.anyProblems());
        assertTrue(forwarded.hasProblems());
    }

    @Test
    void forwardProblems_problems_traceDisabled() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetNullable<String> res = createForNullableProblems(List.of(problem));
        // Notice the implicit API usage check for altering the signature to an incompatible
        // type.
        final RetVal<Integer> forwarded = res.forwardProblems();

        assertEquals(List.of(problem), forwarded.anyProblems());
        assertTrue(forwarded.hasProblems());
    }

    @Test
    void forwardNullableProblems_ok() {
        final RetNullable<String> res = createForNullable("a");
        try {
            res.forwardNullableProblems();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
    }

    @Test
    void forwardNullableProblems_problems_traceEnabled() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetNullable<String> res = createForNullableProblems(List.of(problem));
        // Notice the implicit API usage check for altering the signature to an incompatible
        // type.
        final RetNullable<Integer> forwarded = res.forwardNullableProblems();
        assertEquals(List.of(problem), forwarded.anyProblems());
        assertTrue(forwarded.hasProblems());
    }

    @Test
    void forwardNullableProblems_problems_traceDisabled() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetNullable<String> res = createForNullableProblems(List.of(problem));
        // Notice the implicit API usage check for altering the signature to an incompatible
        // type.
        final RetNullable<Integer> forwarded = res.forwardNullableProblems();

        assertEquals(List.of(problem), forwarded.anyProblems());
        assertTrue(forwarded.hasProblems());
    }

    @Test
    void forwardVoidProblems_ok() {
        final RetNullable<String> res = createForNullable("a");
        try {
            res.forwardVoidProblems();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
    }

    @Test
    void forwardVoidProblems_problems() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetNullable<String> res = createForNullableProblems(List.of(problem));
        // Notice the implicit API usage check for altering the signature to an incompatible
        // type.
        final RetVoid forwarded = res.forwardVoidProblems();

        assertEquals(List.of(problem), forwarded.anyProblems());
        assertTrue(forwarded.hasProblems());
    }

    @Test
    void thenValidate_initialProblem() {
        final String[] acceptedValue = {"not set"};
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetNullable<String> res = createForNullableProblems(List.of(problem));
        final RetNullable<String> val = res.thenValidate((v) -> {
            throw new IllegalStateException("unreachable code");
        });

        // And the original problem list should remain.
        assertEquals(List.of(problem), val.anyProblems());
        // And the callback should not have been called.
        assertArrayEquals(new String[] {"not set"}, acceptedValue);
    }

    @Test
    void thenValidate_ok_nullOk() {
        final String[] acceptedValue = {"not set"};
        final int[] callCount = {0};
        final RetNullable<String> res = createForNullable("value");
        final RetNullable<String> val = res.thenValidate((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return null;
        });
        // And the state should not have been changed.  It's immutable, so, yeah.
        assertEquals(List.of(), val.anyProblems());
        assertEquals("value", val.result());
        // And the callback should have been called.
        assertEquals("value", acceptedValue[0]);
        assertEquals(1, callCount[0]);
    }

    @Test
    void thenValidate_ok_emptyOk() {
        final String[] acceptedValue = {"not set"};
        final int[] callCount = {0};
        final RetNullable<String> res = createForNullable("value");
        final RetNullable<String> val = res.thenValidate((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return createForNullable("x");  // non-null and no problems
        });
        // And the state should not have been changed.  It's immutable, so, yeah.
        assertEquals(List.of(), val.anyProblems());
        assertEquals("value", val.result());
        // And the callback should have been called.
        assertEquals("value", acceptedValue[0]);
        assertEquals(1, callCount[0]);
    }

    @Test
    void thenValidate_ok_problem() {
        final String[] acceptedValue = {"not set"};
        final int[] callCount = {0};
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetNullable<String> res = createForNullable("value");
        final RetNullable<Object> problemRet = createForNullableProblems(List.of(problem));
        final RetNullable<String> val = res.thenValidate((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return problemRet;
        });
        // And the returned value has problems.
        assertEquals(List.of(problem), val.anyProblems());
        assertNull(val.getValue());
        // And the callback should have been called once.
        assertEquals("value", acceptedValue[0]);
        assertEquals(1, callCount[0]);
    }

    @Test
    void thenValidate_ok_generalProblem() {
        final String[] acceptedValue = {"not set"};
        final int[] callCount = {0};
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetNullable<String> res = createForNullable("value");
        final ProblemCollector problemRet = ProblemCollector.from(problem);
        final RetNullable<String> val = res.thenValidate((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return problemRet;
        });
        // And the returned value has problems.
        assertEquals(List.of(problem), val.anyProblems());
        assertNull(val.getValue());
        // And the callback should have been called once.
        assertEquals("value", acceptedValue[0]);
        assertEquals(1, callCount[0]);
    }

    @Test
    void then_ok_ok() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final RetNullable<Integer> res = createForNullable(3);
        final RetVal<Integer> val = res.then((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return RetVal.ok(v + 2);
        });
        // And the state should be different.
        assertEquals(List.of(), val.anyProblems());
        assertEquals(5, val.result());
        assertEquals(3, acceptedValue[0]);
        assertEquals(1, callCount[0]);
    }

    @Test
    void then_ok_problem() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetNullable<Integer> res = createForNullable(3);
        final RetVal<Integer> val = res.then((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return RetVal.fromProblem(problem);
        });
        // And the state should be different.
        assertEquals(List.of(problem), val.anyProblems());
        assertEquals(3, acceptedValue[0]);
        assertEquals(1, callCount[0]);
        assertTrue(val.hasProblems());
    }

    @Test
    void then_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetNullable<Integer> res = createForNullableProblems(List.of(problem));
        final RetVal<Integer> val = res.then((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void map_ok() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final RetNullable<Integer> res = createForNullable(3);
        final RetVal<Integer> val = res.map((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return v + 2;
        });
        // And the state should be different.
        assertEquals(List.of(), val.anyProblems());
        assertEquals(5, val.result());
        assertEquals(3, acceptedValue[0]);
        assertEquals(1, callCount[0]);
    }

    @Test
    void map_problem_noTracing() {
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetNullable<Integer> res = createForNullableProblems(List.of(problem));
        final RetVal<Integer> val = res.map((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void map_problem_tracing() {
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetNullable<Integer> res = createForNullableProblems(List.of(problem));
        final RetVal<Integer> val = res.map((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void thenNullable_ok_ok() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final RetNullable<Integer> res = createForNullable(3);
        final RetNullable<Integer> val = res.thenNullable((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return createForNullable(v + 2);
        });
        // And the state should be different.
        assertEquals(List.of(), val.anyProblems());
        assertEquals(5, val.result());
        assertEquals(3, acceptedValue[0]);
        assertEquals(1, callCount[0]);
    }

    @Test
    void thenNullable_ok_okNull() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final RetNullable<Integer> res = createForNullable(3);
        final RetNullable<Integer> val = res.thenNullable((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return createForNullable(null);
        });
        // And the state should be different.
        assertEquals(List.of(), val.anyProblems());
        assertNull(val.result());
        assertEquals(3, acceptedValue[0]);
        assertEquals(1, callCount[0]);
    }

    @Test
    void thenNullable_ok_problem() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetNullable<Integer> res = createForNullable(3);
        final RetNullable<Integer> val = res.thenNullable((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return createForNullableProblems(List.of(problem));
        });
        // And the state should be different.
        assertEquals(List.of(problem), val.anyProblems());
        assertEquals(3, acceptedValue[0]);
        assertEquals(1, callCount[0]);
        assertTrue(val.hasProblems());
    }

    @Test
    void thenNullable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetNullable<Integer> res = createForNullableProblems(List.of(problem));
        final RetNullable<Integer> val = res.thenNullable((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void mapNullable_ok() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final RetNullable<Integer> res = createForNullable(3);
        final RetNullable<Integer> val = res.mapNullable((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return v + 2;
        });
        // And the state should be different.
        assertEquals(List.of(), val.anyProblems());
        assertEquals(5, val.result());
        assertEquals(3, acceptedValue[0]);
        assertEquals(1, callCount[0]);
    }

    @Test
    void mapNullable_okNull() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final RetNullable<Integer> res = createForNullable(3);
        final RetNullable<Integer> val = res.mapNullable((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return null;
        });
        // And the state should be different.
        assertEquals(List.of(), val.anyProblems());
        assertNull(val.result());
        assertEquals(3, acceptedValue[0]);
        assertEquals(1, callCount[0]);
    }

    @Test
    void mapNullable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetNullable<Integer> res = createForNullableProblems(List.of(problem));
        final RetNullable<Integer> val = res.mapNullable((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void thenRun_runnable_ok() {
        final int[] callCount = {0};
        final RetNullable<String> res = createForNullable("x");
        final RetNullable<String> val = res.thenRunNullable(() -> callCount[0]++);
        assertEquals(List.of(), val.anyProblems());
        assertEquals(1, callCount[0]);
    }

    @Test
    void thenRun_runnable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetNullable<Integer> res = createForNullableProblems(List.of(problem));
        final RetNullable<Integer> val = res.thenRunNullable(() -> {
            throw new IllegalStateException("unreachable code");
        });
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void thenRun_consumer_ok() {
        final int[] callCount = {0};
        final String[] value = {"not called"};
        final RetNullable<String> res = createForNullable("x");
        final RetNullable<String> val = res.thenRunNullable((v) -> {
            value[0] = v;
            callCount[0]++;
        });
        assertEquals(List.of(), val.anyProblems());
        assertEquals("x", value[0]);
        assertEquals(1, callCount[0]);
    }

    @Test
    void thenRun_consumer_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetNullable<Integer> res = createForNullableProblems(List.of(problem));
        final RetNullable<Integer> val = res.thenRunNullable((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void thenVoid_function_ok_ok() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final RetNullable<Integer> res = createForNullable(3);
        final RetVoid val = res.thenVoid((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return RetVoid.ok();
        });
        // And the state should be different.
        assertEquals(List.of(), val.anyProblems());
        assertEquals(3, acceptedValue[0]);
        assertEquals(1, callCount[0]);
    }

    @Test
    void thenVoid_function_ok_problem() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetNullable<Integer> res = createForNullable(3);
        final RetVoid val = res.thenVoid((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return RetVoid.fromProblem(problem);
        });
        // And the state should be different.
        assertEquals(List.of(problem), val.anyProblems());
        assertEquals(3, acceptedValue[0]);
        assertEquals(1, callCount[0]);
        assertTrue(val.hasProblems());
    }

    @Test
    void thenVoid_function_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetNullable<Integer> res = createForNullableProblems(List.of(problem));
        final RetVoid val = res.thenVoid((NonnullReturnFunction<Integer, RetVoid>) (v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void thenVoid_consumer_ok() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final RetNullable<Integer> res = createForNullable(3);
        final RetVoid val = res.thenVoid((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
        });
        // And the state should be different.
        assertEquals(List.of(), val.anyProblems());
        assertEquals(3, acceptedValue[0]);
        assertEquals(1, callCount[0]);
    }

    @Test
    void thenVoid_consumer_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetNullable<Integer> res = createForNullableProblems(List.of(problem));
        final RetVoid val = res.thenVoid((Consumer<Integer>) (v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void consume_ok() {
        final List<Character> visited = new ArrayList<>();
        final RetNullable<Character> res = createForNullable('c');
        final RetVoid val = res.consume(visited::add);
        assertEquals(List.of(), val.anyProblems());
        assertEquals(List.of('c'), visited);
    }

    @Test
    void consume_ok_null() {
        final List<Character> visited = new ArrayList<>();
        final RetNullable<Character> res = createForNullable(null);
        final RetVoid val = res.consume(visited::add);
        assertEquals(List.of(), val.anyProblems());
        assertEquals(Arrays.asList(new Object[] {null}), visited);
    }

    @Test
    void consume_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("6");
        final RetNullable<Character> res = createForNullableProblems(List.of(problem));
        final RetVoid val = res.consume((c) -> {
            throw new IllegalStateException("not reachable");
        });
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void produceVoid_ok() {
        final List<Character> visited = new ArrayList<>();
        final RetNullable<Character> res = createForNullable('c');
        final RetVoid val = res.produceVoid((v) -> {
            visited.add(v);
            // Implementations can translate this returned value to any RetVoid
            // desired.
            return RetVoid.ok();
        });
        assertEquals(List.of(), val.anyProblems());
        assertEquals(List.of('c'), visited);
    }

    @Test
    void produceVoid_ok_problem() {
        final List<Character> visited = new ArrayList<>();
        final LocalizedProblem problem = LocalizedProblem.from("6");
        final RetNullable<Character> res = createForNullable('c');
        final RetVoid val = res.produceVoid((c) -> {
            visited.add(c);
            return RetVoid.fromProblem(problem);
        });
        assertEquals(List.of(problem), val.anyProblems());
        assertEquals(List.of('c'), visited);
    }

    @Test
    void produceVoid_ok_null() {
        final List<Character> visited = new ArrayList<>();
        final RetNullable<Character> res = createForNullable(null);
        final RetVoid val = res.produceVoid((v) -> {
            visited.add(v);
            // Implementations can translate this returned value to any RetVoid
            // desired.
            return RetVoid.ok();
        });
        assertEquals(List.of(), val.anyProblems());
        assertEquals(Arrays.asList(new Object[] {null}), visited);
    }

    @Test
    void produceVoid_ok_null_problem() {
        final List<Character> visited = new ArrayList<>();
        final LocalizedProblem problem = LocalizedProblem.from("6");
        final RetNullable<Character> res = createForNullable(null);
        final RetVoid val = res.produceVoid((c) -> {
            visited.add(c);
            return RetVoid.fromProblem(problem);
        });
        assertEquals(List.of(problem), val.anyProblems());
        assertEquals(Arrays.asList(new Object[] {null}), visited);
    }

    @Test
    void produceVoid_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("6");
        final RetNullable<Character> res = createForNullableProblems(List.of(problem));
        final RetVoid val = res.produceVoid((c) -> {
            throw new IllegalStateException("not reachable");
        });
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void requireNonNull_nonnull() {
        final RetNullable<String> src = createForNullable("x");
        final LocalizedProblem problem = LocalizedProblem.from("7a");
        final RetVal<String> res = src.requireNonNull(problem);
        assertEquals(List.of(), res.anyProblems());
        assertEquals("x", res.result());
    }

    @Test
    void requireNonNull_null_oneProblem() {
        final RetNullable<String> src = createForNullable(null);
        final LocalizedProblem problem = LocalizedProblem.from("qq");
        final RetVal<String> res = src.requireNonNull(problem);
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void requireNonNull_null_twoProblems() {
        final RetNullable<String> src = createForNullable(null);
        final LocalizedProblem problem1 = LocalizedProblem.from("7a");
        final LocalizedProblem problem2 = LocalizedProblem.from("7b");
        final RetVal<String> res = src.requireNonNull(problem1, problem2);
        assertEquals(List.of(problem1, problem2), res.anyProblems());
    }

    @Test
    void requireNonNull_null_threeProblems() {
        final RetNullable<String> src = createForNullable(null);
        final LocalizedProblem problem1 = LocalizedProblem.from("7a");
        final LocalizedProblem problem2 = LocalizedProblem.from("7b");
        final LocalizedProblem problem3 = LocalizedProblem.from("7c");
        final RetVal<String> res = src.requireNonNull(problem1, problem2, problem3);
        assertEquals(List.of(problem1, problem2, problem3), res.anyProblems());
    }

    @Test
    void requireNonNull_problem() {
        final LocalizedProblem problem1 = LocalizedProblem.from("7a");
        final LocalizedProblem problem2 = LocalizedProblem.from("7b");
        final RetNullable<Character> src = createForNullableProblems(List.of(problem1));
        final RetVal<Character> res = src.requireNonNull(problem2);
        assertEquals(List.of(problem1), res.anyProblems());
    }

    @Test
    void defaultAs_nonnull() {
        final Object val = new Object();
        final RetNullable<Object> src = createForNullable(val);
        final RetVal<Object> res = src.defaultAs(new Object());
        assertEquals(List.of(), res.anyProblems());
        assertSame(val, res.result());
    }

    @Test
    void defaultAs_null() {
        final RetNullable<Object> src = createForNullable(null);
        final Object val = new Object();
        final RetVal<Object> res = src.defaultAs(val);
        assertEquals(List.of(), res.anyProblems());
        assertSame(val, res.result());
    }

    @Test
    void defaultAs_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("fg");
        final RetNullable<Character> src = createForNullableProblems(List.of(problem));
        final RetVal<Character> res = src.defaultAs('x');
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void consumeIfNonnull_nonnull() {
        final RetNullable<String> src = createForNullable("v1");
        final String[] observed = new String[] { null };
        final RetVoid res = src.consumeIfNonnull((v) -> observed[0] = v);
        assertEquals(List.of(), res.anyProblems());
        assertEquals("v1", observed[0]);
    }

    @Test
    void consumeIfNonnull_null() {
        final RetNullable<Object> src = createForNullable(null);
        final RetVoid res = src.consumeIfNonnull((v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(), res.anyProblems());
    }

    @Test
    void consumeIfNonnull_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("bd");
        final RetNullable<Character> src = createForNullableProblems(List.of(problem));
        final RetVoid res = src.consumeIfNonnull((v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void produceVoidIfNonnull_nonnull() {
        final LocalizedProblem problem = LocalizedProblem.from("bd");
        final RetVoid expected = RetVoid.fromProblem(problem);
        final RetNullable<String> src = createForNullable("v1");
        final String[] observed = new String[] { null };
        final RetVoid res = src.produceVoidIfNonnull((v) -> {
            observed[0] = v;
            return expected;
        });
        assertEquals(List.of(problem), res.anyProblems());
        assertEquals("v1", observed[0]);
        assertSame(expected, res);
    }

    @Test
    void produceVoidIfNonnull_null() {
        final RetNullable<Object> src = createForNullable(null);
        final RetVoid res = src.produceVoidIfNonnull((v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(), res.anyProblems());
    }

    @Test
    void produceVoidIfNonnull_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("bd");
        final RetNullable<Character> src = createForNullableProblems(List.of(problem));
        final RetVoid res = src.produceVoidIfNonnull((v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void defaultOrMap_nonnull() {
        final Object val = new Object();
        final RetNullable<Object> src = createForNullable(val);
        final Object[] observed = new Object[] { null };
        final RetVal<String> res = src.defaultOrMap("foo", (v) -> {
            observed[0] = v;
            return "bar";
        });
        assertEquals(List.of(), res.anyProblems());
        assertEquals(val, observed[0]);
        assertEquals("bar", res.result());
    }

    @Test
    void defaultOrMap_null() {
        final RetNullable<Object> src = createForNullable(null);
        final RetVal<String> res = src.defaultOrMap("foo", (v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(), res.anyProblems());
        assertEquals("foo", res.result());
    }

    @Test
    void defaultOrMap_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("tt4");
        final RetNullable<Character> src = createForNullableProblems(List.of(problem));
        final RetVal<String> res = src.defaultOrMap("foo", (v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void defaultOrThen_nonnull() {
        final Object val = new Object();
        final RetNullable<Object> src = createForNullable(val);
        final Object[] observed = new Object[] { null };
        final RetVal<String> res = src.defaultOrThen("foo", (v) -> {
            observed[0] = v;
            return RetVal.ok("bar");
        });
        assertEquals(List.of(), res.anyProblems());
        assertEquals(val, observed[0]);
        assertEquals("bar", res.result());
    }

    @Test
    void defaultOrThen_null() {
        final RetNullable<Object> src = createForNullable(null);
        final RetVal<String> res = src.defaultOrThen("foo", (v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(), res.anyProblems());
        assertEquals("foo", res.result());
    }

    @Test
    void defaultOrThen_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("tt5");
        final RetNullable<Character> src = createForNullableProblems(List.of(problem));
        final RetVal<String> res = src.defaultOrThen("foo", (v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void nullOrMap_nonnull() {
        final Object val = new Object();
        final RetNullable<Object> src = createForNullable(val);
        final Object[] observed = new Object[] { null };
        final RetNullable<String> res = src.nullOrMap((v) -> {
            observed[0] = v;
            return "bar";
        });
        assertEquals(List.of(), res.anyProblems());
        assertEquals(val, observed[0]);
        assertEquals("bar", res.result());
    }

    @Test
    void nullOrMap_nonnull_nullRet() {
        final Object val = new Object();
        final RetNullable<Object> src = createForNullable(val);
        final Object[] observed = new Object[] { null };
        final RetNullable<String> res = src.nullOrMap((v) -> {
            observed[0] = v;
            return null;
        });
        assertEquals(List.of(), res.anyProblems());
        assertEquals(val, observed[0]);
        assertNull(res.result());
    }

    @Test
    void nullOrMap_null() {
        final RetNullable<Object> src = createForNullable(null);
        final RetNullable<String> res = src.nullOrMap((v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(), res.anyProblems());
        assertNull(res.result());
    }

    @Test
    void nullOrMap_problem() {
        final LocalizedProblem problem = LocalizedProblem.from(",");
        final RetNullable<Character> src = createForNullableProblems(List.of(problem));
        final RetNullable<String> res = src.nullOrMap((v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void nullOrThenNullable_nonnull() {
        final Object val = new Object();
        final RetNullable<Object> src = createForNullable(val);
        final Object[] observed = new Object[] { null };
        final RetNullable<String> res = src.nullOrThenNullable((v) -> {
            observed[0] = v;
            return RetNullable.ok("bar");
        });
        assertEquals(List.of(), res.anyProblems());
        assertEquals(val, observed[0]);
        assertEquals("bar", res.result());
    }

    @Test
    void nullOrThenNullable_nonnull_nullRet() {
        final Object val = new Object();
        final RetNullable<Object> src = createForNullable(val);
        final Object[] observed = new Object[] { null };
        final RetNullable<String> res = src.nullOrThenNullable((v) -> {
            observed[0] = v;
            return RetNullable.ok(null);
        });
        assertEquals(List.of(), res.anyProblems());
        assertEquals(val, observed[0]);
        assertNull(res.result());
    }

    @Test
    void nullOrThenNullable_nullSrc() {
        final RetNullable<Object> src = createForNullable(null);
        final RetNullable<String> res = src.nullOrThenNullable((v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(), res.anyProblems());
        assertNull(res.result());
    }

    @Test
    void nullOrThenNullable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from(",");
        final RetNullable<Character> src = createForNullableProblems(List.of(problem));
        final RetNullable<String> res = src.nullOrThenNullable((v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void isOk_ok() {
        // Test the explicit rules around observability with this call.
        final RetNullable<Long> res = createForNullable(Long.MAX_VALUE);

        // Call and check result
        assertTrue(res.isOk());
    }

    @Test
    void isOk_problem() {
        // Test the explicit rules around observability with this call.
        final LocalizedProblem problem = LocalizedProblem.from("f");
        final RetNullable<Long> res = createForNullableProblems(List.of(problem));

        // Call and check result
        assertFalse(res.isOk());
    }

    // hasProblems() is called by isProblem() directly, so just test isProblem.

    @Test
    void isProblem_ok() {
        // Test the explicit rules around observability with this call.
        final RetNullable<Long> res = createForNullable(Long.MAX_VALUE);

        // Call and check result
        assertFalse(res.isProblem());
    }

    @Test
    void isProblem_problem() {
        // Test the explicit rules around observability with this call.
        final LocalizedProblem problem = LocalizedProblem.from("f");
        final RetNullable<Long> res = createForNullableProblems(List.of(problem));

        // Call and check result
        assertTrue(res.isProblem());
    }

    @Test
    void anyProblems_ok() {
        // Test the explicit rules around observability with this call.
        final RetNullable<Long> res = createForNullable(Long.MAX_VALUE);

        // Call and check result
        final Collection<Problem> problems = res.anyProblems();
        assertEquals(List.of(), problems);
        assertUnmodifiable(problems);

    }

    @Test
    void anyProblems_problem() {
        // Test the explicit rules around observability with this call.
        final LocalizedProblem problem = LocalizedProblem.from("f");
        final RetNullable<Long> res = createForNullableProblems(List.of(problem));

        // Call and check result
        final Collection<Problem> problems = res.anyProblems();
        assertEquals(List.of(problem), problems);
        assertUnmodifiable(problems);
    }

    @Test
    void validProblems_ok() {
        // Test the explicit rules around observability with this call.
        final RetNullable<Long> res = createForNullable(Long.MAX_VALUE);

        // Call and check result
        try {
            assertEquals(List.of(), res.validProblems());
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // don't inspect the exception
        }
    }

    @Test
    void validProblems_problem() {
        // Test the explicit rules around observability with this call.
        final LocalizedProblem problem = LocalizedProblem.from("f");
        final RetNullable<Long> res = createForNullableProblems(List.of(problem));

        // Call and check result
        final Collection<Problem> problems = res.validProblems();
        assertEquals(List.of(problem), problems);

        assertUnmodifiable(problems);
    }

    @Test
    void joinProblemsWith_ok() {
        // Test the explicit rules around observability with this call.
        final RetNullable<Long> res = createForNullable(Long.MAX_VALUE);

        // Call and check result
        final List<Problem> probs = new ArrayList<>();
        res.joinProblemsWith(probs);

        assertEquals(List.of(), probs);
    }

    @Test
    void joinProblemsWith_problem() {
        // Test the explicit rules around observability with this call.
        final LocalizedProblem problem = LocalizedProblem.from("f");
        final RetNullable<Long> res = createForNullableProblems(List.of(problem));

        // Call and check result
        final List<Problem> probs = new ArrayList<>();
        res.joinProblemsWith(probs);

        assertEquals(List.of(problem), probs);
    }

    @Test
    void debugProblems_empty() {
        assertEquals(
                "",
                createForNullable("x").debugProblems(";")
        );
    }

    @Test
    void debugProblems_one() {
        assertEquals(
                "a",
                createForNullableProblems(List.of(LocalizedProblem.from("a")))
                        .debugProblems(";")
        );
    }

    @Test
    void debugProblems_two() {
        assertEquals(
                "a;bb",
                createForNullableProblems(List.of(
                        LocalizedProblem.from("a"),
                        LocalizedProblem.from("bb")
                ))
                .debugProblems(";")
        );
    }

    @Test
    void toString_ok() {
        assertEquals(
                "Ret(value: x)",
                createForNullable("x").toString()
        );
    }

    @Test
    void toString_problems() {
        assertEquals(
                "Ret(2 problems: abc; def)",
                createForNullableProblems(List.of(
                        LocalizedProblem.from("abc"),
                        LocalizedProblem.from("def")
                )).toString()
        );
    }


    void assertUnmodifiable(@Nonnull final Collection<Problem> problems) {
        try {
            problems.add(LocalizedProblem.from("no add allowed"));
            fail("collection allows add");
        } catch (final UnsupportedOperationException e) {
            // pass
        }
        if (! problems.isEmpty()) {
            // Remove operations will sometimes not throw the exception if there's nothing to
            // remove.
            try {
                problems.retainAll(Collections.emptyList());
                fail("collection allows remove");
            } catch (final UnsupportedOperationException e) {
                // pass
            }
        }
    }
}
