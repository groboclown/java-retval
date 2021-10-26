// Released under the MIT License. 
package net.groboclown.retval.contract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.groboclown.retval.Problem;
import net.groboclown.retval.ProblemCollector;
import net.groboclown.retval.RetNullable;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.RetVoid;
import net.groboclown.retval.function.NonnullConsumer;
import net.groboclown.retval.function.NonnullFunction;
import net.groboclown.retval.problems.LocalizedProblem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * Contract tests for all implementations of {@link RetVal}.
 */
public abstract class RetValContract {
    @Nonnull
    protected abstract <T> RetVal<T> createForVal(T value);

    @Nonnull
    protected abstract <T> RetVal<T> createForValProblems(@Nonnull List<Problem> problems);



    @Test
    void getValue_problem() {
        final RetVal<Object> res = createForValProblems(List.of(LocalizedProblem.from("x")));
        assertNull(res.getValue());
    }

    @Test
    void getValue_ok() {
        final RetVal<String> res = createForVal("a");
        assertEquals("a", res.getValue());
    }

    @Test
    void asOptional_problem() {
        final RetVal<Object> res = createForValProblems(List.of(LocalizedProblem.from("x")));
        assertFalse(res.asOptional().isPresent());
    }

    @Test
    void asOptional_ok() {
        final RetVal<String> res = createForVal("a");
        final Optional<String> optional = res.asOptional();
        assertTrue(optional.isPresent());
        assertEquals("a", optional.get());
    }

    @Test
    void requireOptional_problem() {
        final RetVal<Object> res = createForValProblems(List.of(LocalizedProblem.from("x")));
        try {
            res.requireOptional();
            fail("Did not throw IAE");
        } catch (final IllegalStateException e) {
            // Not inspecting contents of exception
        }
    }

    @Test
    void requireOptional_ok() {
        final RetVal<String> res = createForVal("a");
        final Optional<String> optional = res.requireOptional();
        assertTrue(optional.isPresent());
        assertEquals("a", optional.get());
    }

    @Test
    void result_problem() {
        final RetVal<Object> res = createForValProblems(List.of(LocalizedProblem.from("x")));
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
        final RetVal<Long> res = createForVal(Long.MIN_VALUE);

        // Call and check result.  Note this is done without the "isOk" wrapper.
        assertEquals(Long.MIN_VALUE, res.result());
    }

    @Test
    void forwardProblems_ok() {
        final RetVal<String> res = createForVal("a");
        try {
            res.forwardProblems();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
    }

    @Test
    void forwardProblems_problems() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVal<String> res = createForValProblems(List.of(problem));
        // Notice the implicit API usage check for altering the signature to an incompatible
        // type.
        final RetVal<Integer> forwarded = res.forwardProblems();

        assertEquals(List.of(problem), forwarded.anyProblems());
        assertTrue(forwarded.hasProblems());
    }

    @Test
    void forwardNullableProblems_ok() {
        final RetVal<String> res = createForVal("a");
        try {
            res.forwardNullableProblems();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
    }

    @Test
    void forwardNullableProblems_problems() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVal<String> res = createForValProblems(List.of(problem));
        // Notice the implicit API usage check for altering the signature to an incompatible
        // type.
        final RetNullable<Integer> forwarded = res.forwardNullableProblems();

        assertEquals(List.of(problem), forwarded.anyProblems());
        assertTrue(forwarded.hasProblems());
    }

    @Test
    void forwardVoidProblems_ok() {
        final RetVal<String> res = createForVal("a");
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
        final RetVal<String> res = createForValProblems(List.of(problem));
        // Notice the implicit API usage check for altering the signature to an incompatible
        // type.
        final RetVoid forwarded = res.forwardVoidProblems();

        assertEquals(List.of(problem), forwarded.anyProblems());
        assertTrue(forwarded.hasProblems());
    }

    @Test
    void asNullable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        // Note: RetVal<Void> is impossible with a non-problem value.
        final RetVal<Void> res = createForValProblems(List.of(problem));
        final RetNullable<Void> val = res.asNullable();

        assertTrue(val.hasProblems());
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void asNullable_ok() {
        final RetVal<String> res = createForVal("a");
        final RetNullable<String> val = res.asNullable();

        assertEquals("a", val.result());
        assertEquals(List.of(), val.anyProblems());
    }

    @Test
    void thenValidate_initialProblem() {
        final String[] acceptedValue = {"not set"};
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVal<String> res = createForValProblems(List.of(problem));
        final RetVal<String> val = res.thenValidate((v) -> {
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
        final RetVal<String> res = createForVal("value");
        final RetVal<String> val = res.thenValidate((v) -> {
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
        final RetVal<String> res = createForVal("value");
        final RetVal<String> val = res.thenValidate((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return createForVal("ok");  // non-null and no problems
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
        final RetVal<String> res = createForVal("value");
        final RetVal<Object> problemRet = createForValProblems(List.of(problem));
        final RetVal<String> val = res.thenValidate((v) -> {
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
        final RetVal<String> res = createForVal("value");
        final ProblemCollector problemRet = ProblemCollector.from(problem);
        final RetVal<String> val = res.thenValidate((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return problemRet;
        });
        // And the returned value has problems.
        assertEquals(List.of(problem), val.anyProblems());
        assertNull(val.getValue());
        // And the callback should have been called.
        assertEquals("value", acceptedValue[0]);
        // And the callback should have been called once.
        assertEquals("value", acceptedValue[0]);
        assertEquals(1, callCount[0]);
    }

    @Test
    void then_ok_ok() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final RetVal<Integer> res = createForVal(3);
        final RetVal<Integer> val = res.then((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return createForVal(v + 2);
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
        final RetVal<Integer> res = createForVal(3);
        final RetVal<Integer> val = res.then((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return createForValProblems(List.of(problem));
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
        final RetVal<Integer> res = createForValProblems(List.of(problem));
        final RetVal<Integer> val = res.then((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void map_ok() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final RetVal<Integer> res = createForVal(3);
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
        // With tracing disabled, the same value is returned.
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetVal<Integer> res = createForValProblems(List.of(problem));
        final RetVal<Integer> val = res.map((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void map_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetVal<Integer> res = createForValProblems(List.of(problem));
        final RetVal<Integer> val = res.map((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void thenNullable_ok_okNull() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final RetVal<Integer> res = createForVal(3);
        final RetNullable<Integer> val = res.thenNullable((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return RetNullable.ok(null);
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
        final RetVal<Integer> res = createForVal(3);
        final RetNullable<Integer> val = res.thenNullable((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return RetNullable.fromProblem(problem);
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
        final RetVal<Integer> res = createForValProblems(List.of(problem));
        final RetNullable<Integer> val = res.thenNullable((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void mapNullable_ok() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final RetVal<Integer> res = createForVal(3);
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
        final RetVal<Integer> res = createForVal(3);
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
        final RetVal<Integer> res = createForValProblems(List.of(problem));
        final RetNullable<Integer> val = res.mapNullable((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void thenRun_runnable_ok() {
        final int[] callCount = {0};
        final RetVal<String> res = createForVal("x");
        final RetVal<String> val = res.thenRun(() -> callCount[0]++);
        assertEquals(List.of(), val.anyProblems());
        assertEquals(1, callCount[0]);
    }

    @Test
    void thenRun_runnable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetVal<Integer> res = createForValProblems(List.of(problem));
        final RetVal<Integer> val = res.thenRun(() -> {
            throw new IllegalStateException("unreachable code");
        });
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void thenRun_consumer_ok() {
        final int[] callCount = {0};
        final String[] value = {"not called"};
        final RetVal<String> res = createForVal("x");
        final RetVal<String> val = res.thenRun((v) -> {
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
        final RetVal<Integer> res = createForValProblems(List.of(problem));
        final RetVal<Integer> val = res.thenRun((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void thenVoid_function_ok_ok() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final RetVal<Integer> res = createForVal(3);
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
        final RetVal<Integer> res = createForVal(3);
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
        final RetVal<Integer> res = createForValProblems(List.of(problem));
        final RetVoid val = res.thenVoid((NonnullFunction<Integer, RetVoid>) (v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void thenVoid_consumer_ok() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final RetVal<Integer> res = createForVal(3);
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
        final RetVal<Integer> res = createForValProblems(List.of(problem));
        final RetVoid val = res.thenVoid((NonnullConsumer<Integer>) (v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void consume_ok() {
        final List<Character> visited = new ArrayList<>();
        final RetVal<Character> res = createForVal('c');
        final RetVoid val = res.consume(visited::add);
        assertEquals(List.of(), val.anyProblems());
        assertEquals(List.of('c'), visited);
    }

    @Test
    void consume_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("6");
        final RetVal<Character> res = createForValProblems(List.of(problem));
        final RetVoid val = res.consume((c) -> {
            throw new IllegalStateException("not reachable");
        });
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void produceVoid_ok() {
        final List<Character> visited = new ArrayList<>();
        final RetVal<Character> res = createForVal('c');
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
        final RetVal<Character> res = createForVal('c');
        final RetVoid val = res.produceVoid((c) -> {
            visited.add(c);
            return RetVoid.fromProblem(problem);
        });
        assertEquals(List.of(problem), val.anyProblems());
        assertEquals(List.of('c'), visited);
    }

    @Test
    void produceVoid_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("6");
        final RetVal<Character> res = createForValProblems(List.of(problem));
        final RetVoid val = res.produceVoid((c) -> {
            throw new IllegalStateException("not reachable");
        });
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void isOk_ok() {
        final RetVal<Long> res = createForVal(Long.MAX_VALUE);

        // Call and check result
        assertTrue(res.isOk());
    }

    @Test
    void isOk_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("f");
        final RetVal<Long> res = createForValProblems(List.of(problem));

        // Call and check result
        assertFalse(res.isOk());
    }

    // hasProblems() is called by isProblem() directly, so just test isProblem.

    @Test
    void isProblem_ok() {
        final RetVal<Long> res = createForVal(Long.MAX_VALUE);

        // Call and check result
        assertFalse(res.isProblem());
    }

    @Test
    void isProblem_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("f");
        final RetVal<Long> res = createForValProblems(List.of(problem));

        // Call and check result
        assertTrue(res.isProblem());
    }

    @Test
    void anyProblems_ok() {
        final RetVal<Long> res = createForVal(Long.MAX_VALUE);

        // Call and check result
        final Collection<Problem> problems = res.anyProblems();
        assertEquals(List.of(), problems);
        assertUnmodifiable(problems);
    }

    @Test
    void anyProblems_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("f");
        final RetVal<Long> res = createForValProblems(List.of(problem));

        // Call and check result
        final Collection<Problem> problems = res.anyProblems();
        assertEquals(List.of(problem), problems);
        assertUnmodifiable(problems);
    }

    @Test
    void validProblems_ok() {
        final RetVal<Long> res = createForVal(Long.MAX_VALUE);

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
        final LocalizedProblem problem = LocalizedProblem.from("f");
        final RetVal<Long> res = createForValProblems(List.of(problem));

        // Call and check result
        final Collection<Problem> problems = res.validProblems();
        assertEquals(List.of(problem), problems);
        assertUnmodifiable(problems);
    }

    @Test
    void joinProblemsWith_ok() {
        final RetVal<Long> res = createForVal(Long.MAX_VALUE);

        // Call and check result
        final List<Problem> probs = new ArrayList<>();
        res.joinProblemsWith(probs);

        assertEquals(List.of(), probs);
    }

    @Test
    void joinProblemsWith_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("f");
        final RetVal<Long> res = createForValProblems(List.of(problem));

        // Call and check result
        final List<Problem> probs = new ArrayList<>();
        res.joinProblemsWith(probs);

        assertEquals(List.of(problem), probs);
    }

    @Test
    void debugProblems_empty() {
        assertEquals(
                "",
                createForVal("x").debugProblems(";")
        );
    }

    @Test
    void debugProblems_one() {
        assertEquals(
                "a",
                createForValProblems(List.of(LocalizedProblem.from("a")))
                        .debugProblems(";")
        );
    }

    @Test
    void debugProblems_two() {
        assertEquals(
                "a;bb",
                createForValProblems(List.of(
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
                createForVal("x").toString()
        );
    }

    @Test
    void toString_problems() {
        assertEquals(
                "Ret(2 problems: abc; def)",
                createForValProblems(List.of(
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
