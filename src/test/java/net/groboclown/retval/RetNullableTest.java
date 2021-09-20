// Released under the MIT License. 
package net.groboclown.retval;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.groboclown.retval.function.NonnullReturnFunction;
import net.groboclown.retval.monitor.MockProblemMonitor;
import net.groboclown.retval.problems.LocalizedProblem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class RetNullableTest {
    MockProblemMonitor monitor;

    @Test
    void getValue_problem() {
        final RetNullable<Object> res = RetNullable.fromProblem(LocalizedProblem.from("x"));
        assertNull(res.getValue());
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    @Test
    void getValue_ok() {
        final RetNullable<String> res = RetNullable.ok("a");
        assertEquals("a", res.getValue());
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    @Test
    void asOptional_problem() {
        final RetNullable<Object> res = RetNullable.fromProblem(LocalizedProblem.from("x"));
        assertTrue(res.asOptional().isEmpty());
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    @Test
    void asOptional_ok() {
        final RetNullable<String> res = RetNullable.ok("a");
        assertTrue(res.asOptional().isPresent());
        assertEquals("a", res.asOptional().get());
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    @Test
    void asOptional_ok_null() {
        final RetNullable<String> res = RetNullable.ok(null);
        assertTrue(res.asOptional().isEmpty());
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    @Test
    void requireOptional_problem() {
        final RetNullable<Object> res = RetNullable.fromProblem(LocalizedProblem.from("x"));
        try {
            res.requireOptional();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // don't inspect the exception
        }
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    @Test
    void requireOptional_ok() {
        final RetNullable<String> res = RetNullable.ok("a");
        assertTrue(res.requireOptional().isPresent());
        assertEquals("a", res.asOptional().get());
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    @Test
    void requireOptional_ok_null() {
        final RetNullable<String> res = RetNullable.ok(null);
        assertTrue(res.requireOptional().isEmpty());
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    @Test
    void result_problem() {
        final RetNullable<Object> res = RetNullable.fromProblem(LocalizedProblem.from("x"));
        try {
            res.result();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
        // Ensure that, after being called, regardless of the ok/problem state, it is still
        // considered unobserved.
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    @Test
    void result_ok_not_checked() {
        // Test the explicit rules around observability with this call.
        final RetNullable<Long> res = RetNullable.ok(Long.MIN_VALUE);
        // Ensure it is not observed right after creation...
        assertEquals(List.of(res), this.monitor.getNeverObserved());

        // Call and check result.  Note this is done without the "isOk" wrapper.
        assertEquals(Long.MIN_VALUE, res.result());

        // Ensure that, after being called, it is still considered unobserved.
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    @Test
    void forwardProblems_ok() {
        final RetNullable<String> res = RetNullable.ok("a");
        try {
            res.forwardProblems();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    @Test
    void forwardProblems_problems_traceEnabled() {
        this.monitor.traceEnabled = true;

        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetNullable<String> res = RetNullable.fromProblem(problem);
        // Notice the implicit API usage check for altering the signature to an incompatible
        // type.
        final RetVal<Integer> forwarded = res.forwardProblems();

        // the "res" should be marked as checked, and the forwarded one as not.
        // This is due to the usecase of using this "forward" call to cause the
        // previous value to go out-of-scope and pass the problem management to the
        // new object.
        assertEquals(List.of(forwarded), this.monitor.getNeverObserved());

        assertEquals(List.of(problem), forwarded.anyProblems());
        assertTrue(forwarded.hasProblems());
    }

    @Test
    void forwardProblems_problems_traceDisabled() {
        this.monitor.traceEnabled = false;

        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetNullable<String> res = RetNullable.fromProblem(problem);
        // Notice the implicit API usage check for altering the signature to an incompatible
        // type.
        final RetVal<Integer> forwarded = res.forwardProblems();

        // because both "res" and "forwarded" are the same object, just one
        // of them should be in the never-checked list.
        assertEquals(List.of(forwarded), this.monitor.getNeverObserved());

        assertEquals(List.of(problem), forwarded.anyProblems());
        assertTrue(forwarded.hasProblems());
    }

    @Test
    void forwardNullableProblems_ok() {
        final RetNullable<String> res = RetNullable.ok("a");
        try {
            res.forwardNullableProblems();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    @Test
    void forwardNullableProblems_problems_traceEnabled() {
        // Even though the implementation does not have code that checks
        // for the trace-enabled state, it's added here to help ensure future
        // coding does the right thing.
        this.monitor.traceEnabled = true;

        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetNullable<String> res = RetNullable.fromProblem(problem);
        // Notice the implicit API usage check for altering the signature to an incompatible
        // type.
        final RetNullable<Integer> forwarded = res.forwardNullableProblems();
        assertNotSame(res, forwarded);

        // the "res" should be marked as checked, and the forwarded one as not.
        // This is due to the usecase of using this "forward" call to cause the
        // previous value to go out-of-scope and pass the problem management to the
        // new object.
        assertEquals(List.of(forwarded), this.monitor.getNeverObserved());

        assertEquals(List.of(problem), forwarded.anyProblems());
        assertTrue(forwarded.hasProblems());
    }

    @Test
    void forwardNullableProblems_problems_traceDisabled() {
        // Even though the implementation does not have code that checks
        // for the trace-enabled state, it's added here to help ensure future
        // coding does the right thing.
        this.monitor.traceEnabled = false;

        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetNullable<String> res = RetNullable.fromProblem(problem);
        // Notice the implicit API usage check for altering the signature to an incompatible
        // type.
        final RetNullable<Integer> forwarded = res.forwardNullableProblems();
        assertSame(res, forwarded);

        // the "res" should be marked as checked, and the forwarded one as not.
        // This is due to the usecase of using this "forward" call to cause the
        // previous value to go out-of-scope and pass the problem management to the
        // new object.
        assertEquals(List.of(forwarded), this.monitor.getNeverObserved());

        assertEquals(List.of(problem), forwarded.anyProblems());
        assertTrue(forwarded.hasProblems());
    }

    @Test
    void forwardVoidProblems_ok() {
        final RetNullable<String> res = RetNullable.ok("a");
        try {
            res.forwardVoidProblems();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    @Test
    void forwardVoidProblems_problems() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetNullable<String> res = RetNullable.fromProblem(problem);
        // Notice the implicit API usage check for altering the signature to an incompatible
        // type.
        final RetVoid forwarded = res.forwardVoidProblems();
        assertNotSame(res, forwarded);

        // the "res" should be marked as checked, and the forwarded one as not.
        // This is due to the usecase of using this "forward" call to cause the
        // previous value to go out-of-scope and pass the problem management to the
        // new object.
        assertEquals(List.of(forwarded), this.monitor.getNeverObserved());

        assertEquals(List.of(problem), forwarded.anyProblems());
        assertTrue(forwarded.hasProblems());
    }

    @Test
    void thenValidate_initialProblem() {
        final String[] acceptedValue = {"not set"};
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetNullable<String> res = RetNullable.fromProblem(problem);
        final RetNullable<String> val = res.thenValidate((v) -> {
            throw new IllegalStateException("unreachable code");
        });

        // Because there were problems in the original value, the original value was
        // returned.
        assertSame(res, val);
        // On top of this, a check should not have been made.
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        // And the original problem list should remain.
        assertEquals(List.of(problem), val.anyProblems());
        // And the callback should not have been called.
        assertArrayEquals(new String[] {"not set"}, acceptedValue);
    }

    @Test
    void thenValidate_ok_nullOk() {
        final String[] acceptedValue = {"not set"};
        final int[] callCount = {0};
        final RetNullable<String> res = RetNullable.ok("value");
        final RetNullable<String> val = res.thenValidate((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return null;
        });
        // Because the state of the original value didn't change, the original value
        // should have been returned.
        assertSame(res, val);
        // On top of this, a check should not have been made.  And the object should
        // not have been added again to the check list.
        assertEquals(List.of(res), this.monitor.getNeverObserved());
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
        final RetNullable<String> res = RetNullable.ok("value");
        final RetNullable<String> val = res.thenValidate((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return RetVoid.ok();  // non-null and no problems
        });
        // Because the state of the original value didn't change, the original value
        // should have been returned.
        assertSame(res, val);
        // On top of this, a check should not have been made.  And the object should
        // not have been added again to the check list.
        assertEquals(List.of(res), this.monitor.getNeverObserved());
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
        final RetNullable<String> res = RetNullable.ok("value");
        final RetNullable<String> val = res.thenValidate((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return RetVoid.fromProblem(problem);
        });
        // Because the problem state changed, these values must be different.
        assertNotSame(res, val);
        // The check must have passed from the first to the second.
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        // And the returned value has problems.
        assertEquals(List.of(problem), val.anyProblems());
        assertNull(val.getValue());
        // And the callback should have been called.
        assertEquals("value", acceptedValue[0]);
    }

    @Test
    void then_ok_ok() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final RetNullable<Integer> res = RetNullable.ok(3);
        final RetVal<Integer> val = res.then((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return RetVal.ok(v + 2);
        });
        assertNotSame(res, val);
        // The check must have passed from the first to the second.
        assertEquals(List.of(val), this.monitor.getNeverObserved());
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
        final RetNullable<Integer> res = RetNullable.ok(3);
        final RetVal<Integer> val = res.then((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return RetVal.fromProblem(problem);
        });
        assertNotSame(res, val);
        // The check must have passed from the first to the second.
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        // And the state should be different.
        assertEquals(List.of(problem), val.anyProblems());
        assertEquals(3, acceptedValue[0]);
        assertEquals(1, callCount[0]);
        assertTrue(val.hasProblems());
    }

    @Test
    void then_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetNullable<Integer> res = RetNullable.fromProblem(problem);
        final RetVal<Integer> val = res.then((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void map_ok() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final RetNullable<Integer> res = RetNullable.ok(3);
        final RetVal<Integer> val = res.map((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return v + 2;
        });
        assertNotSame(res, val);
        // The check must have passed from the first to the second.
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        // And the state should be different.
        assertEquals(List.of(), val.anyProblems());
        assertEquals(5, val.result());
        assertEquals(3, acceptedValue[0]);
        assertEquals(1, callCount[0]);
    }

    @Test
    void map_problem_noTracing() {
        // tracing enabled state shouldn't matter, but is explicitly called out for
        // future development enforcement.
        this.monitor.traceEnabled = false;
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetNullable<Integer> res = RetNullable.fromProblem(problem);
        final RetVal<Integer> val = res.map((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNotSame(res, val);
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void map_problem_tracing() {
        // With tracing enabled, a different value is returned.
        this.monitor.traceEnabled = true;
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetNullable<Integer> res = RetNullable.fromProblem(problem);
        final RetVal<Integer> val = res.map((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNotSame(res, val);
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void thenNullable_ok_ok() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final RetNullable<Integer> res = RetNullable.ok(3);
        final RetNullable<Integer> val = res.thenNullable((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return RetNullable.ok(v + 2);
        });
        assertNotSame(res, val);
        // The check must have passed from the first to the second.
        assertEquals(List.of(val), this.monitor.getNeverObserved());
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
        final RetNullable<Integer> res = RetNullable.ok(3);
        final RetNullable<Integer> val = res.thenNullable((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return RetNullable.ok(null);
        });
        assertNotSame(res, val);
        // The check must have passed from the first to the second.
        assertEquals(List.of(val), this.monitor.getNeverObserved());
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
        final RetNullable<Integer> res = RetNullable.ok(3);
        final RetNullable<Integer> val = res.thenNullable((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return RetNullable.fromProblem(problem);
        });
        assertNotSame(res, val);
        // The check must have passed from the first to the second.
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        // And the state should be different.
        assertEquals(List.of(problem), val.anyProblems());
        assertEquals(3, acceptedValue[0]);
        assertEquals(1, callCount[0]);
        assertTrue(val.hasProblems());
    }

    @Test
    void thenNullable_problem_tracing() {
        this.monitor.traceEnabled = true;
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetNullable<Integer> res = RetNullable.fromProblem(problem);
        final RetNullable<Integer> val = res.thenNullable((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNotSame(res, val);
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void thenNullable_problem_noTracing() {
        this.monitor.traceEnabled = false;
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetNullable<Integer> res = RetNullable.fromProblem(problem);
        final RetNullable<Integer> val = res.thenNullable((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertSame(res, val);
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void mapNullable_ok() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final RetNullable<Integer> res = RetNullable.ok(3);
        final RetNullable<Integer> val = res.mapNullable((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return v + 2;
        });
        assertNotSame(res, val);
        // The check must have passed from the first to the second.
        assertEquals(List.of(val), this.monitor.getNeverObserved());
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
        final RetNullable<Integer> res = RetNullable.ok(3);
        final RetNullable<Integer> val = res.mapNullable((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return null;
        });
        // The check must have passed from the first to the second.
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        // And the state should be different.
        assertEquals(List.of(), val.anyProblems());
        assertNull(val.result());
        assertEquals(3, acceptedValue[0]);
        assertEquals(1, callCount[0]);
    }

    @Test
    void mapNullable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetNullable<Integer> res = RetNullable.fromProblem(problem);
        final RetNullable<Integer> val = res.mapNullable((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void thenRun_runnable_ok() {
        final int[] callCount = {0};
        final RetNullable<String> res = RetNullable.ok("x");
        final RetNullable<String> val = res.thenRunNullable(() -> callCount[0]++);
        assertSame(res, val);
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        assertEquals(List.of(), val.anyProblems());
        assertEquals(1, callCount[0]);
    }

    @Test
    void thenRun_runnable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetNullable<Integer> res = RetNullable.fromProblem(problem);
        final RetNullable<Integer> val = res.thenRunNullable(() -> {
            throw new IllegalStateException("unreachable code");
        });
        assertSame(res, val);
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void thenRun_consumer_ok() {
        final int[] callCount = {0};
        final String[] value = {"not called"};
        final RetNullable<String> res = RetNullable.ok("x");
        final RetNullable<String> val = res.thenRunNullable((v) -> {
            value[0] = v;
            callCount[0]++;
        });
        assertSame(res, val);
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        assertEquals(List.of(), val.anyProblems());
        assertEquals("x", value[0]);
        assertEquals(1, callCount[0]);
    }

    @Test
    void thenRun_consumer_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetNullable<Integer> res = RetNullable.fromProblem(problem);
        final RetNullable<Integer> val = res.thenRunNullable((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertSame(res, val);
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void thenVoid_function_ok_ok() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final RetNullable<Integer> res = RetNullable.ok(3);
        final RetVoid val = res.thenVoid((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return RetVoid.ok();
        });
        assertNotSame(res, val);
        // The check must have passed from the first to the second.
        assertEquals(List.of(val), this.monitor.getNeverObserved());
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
        final RetNullable<Integer> res = RetNullable.ok(3);
        final RetVoid val = res.thenVoid((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return RetVoid.fromProblem(problem);
        });
        assertNotSame(res, val);
        // The check must have passed from the first to the second.
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        // And the state should be different.
        assertEquals(List.of(problem), val.anyProblems());
        assertEquals(3, acceptedValue[0]);
        assertEquals(1, callCount[0]);
        assertTrue(val.hasProblems());
    }

    @Test
    void thenVoid_function_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetNullable<Integer> res = RetNullable.fromProblem(problem);
        final RetVoid val = res.thenVoid((NonnullReturnFunction<Integer, RetVoid>) (v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void thenVoid_consumer_ok() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final RetNullable<Integer> res = RetNullable.ok(3);
        final RetVoid val = res.thenVoid((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
        });
        assertNotSame(res, val);
        // The check must have passed from the first to the second.
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        // And the state should be different.
        assertEquals(List.of(), val.anyProblems());
        assertEquals(3, acceptedValue[0]);
        assertEquals(1, callCount[0]);
    }

    @Test
    void thenVoid_consumer_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetNullable<Integer> res = RetNullable.fromProblem(problem);
        final RetVoid val = res.thenVoid((Consumer<Integer>) (v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void isOk_ok() {
        // Test the explicit rules around observability with this call.
        final RetNullable<Long> res = RetNullable.ok(Long.MAX_VALUE);
        // Ensure it is not observed right after creation...
        assertEquals(List.of(res), this.monitor.getNeverObserved());

        // Call and check result
        assertTrue(res.isOk());

        // Ensure that, after being called, it is marked as observed.
        assertEquals(List.of(), this.monitor.getNeverObserved());
    }

    @Test
    void isOk_problem() {
        // Test the explicit rules around observability with this call.
        final LocalizedProblem problem = LocalizedProblem.from("f");
        final RetNullable<Long> res = RetNullable.fromProblem(problem);
        // Ensure it is not observed right after creation...
        assertEquals(List.of(res), this.monitor.getNeverObserved());

        // Call and check result
        assertFalse(res.isOk());

        // Ensure that, after being called, it is marked as unobserved.
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    // hasProblems() is called by isProblem() directly, so just test isProblem.

    @Test
    void isProblem_ok() {
        // Test the explicit rules around observability with this call.
        final RetNullable<Long> res = RetNullable.ok(Long.MAX_VALUE);
        // Ensure it is not observed right after creation...
        assertEquals(List.of(res), this.monitor.getNeverObserved());

        // Call and check result
        assertFalse(res.isProblem());

        // Ensure that, after being called, it is marked as observed.
        assertEquals(List.of(), this.monitor.getNeverObserved());
    }

    @Test
    void isProblem_problem() {
        // Test the explicit rules around observability with this call.
        final LocalizedProblem problem = LocalizedProblem.from("f");
        final RetNullable<Long> res = RetNullable.fromProblem(problem);
        // Ensure it is not observed right after creation...
        assertEquals(List.of(res), this.monitor.getNeverObserved());

        // Call and check result
        assertTrue(res.isProblem());

        // Ensure that, after being called, it is marked as unobserved.
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    @Test
    void anyProblems_ok() {
        // Test the explicit rules around observability with this call.
        final RetNullable<Long> res = RetNullable.ok(Long.MAX_VALUE);
        // Ensure it is not observed right after creation...
        assertEquals(List.of(res), this.monitor.getNeverObserved());

        // Call and check result
        assertEquals(List.of(), res.anyProblems());

        // Ensure that, after being called, it is marked as unobserved.
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    @Test
    void anyProblems_problem() {
        // Test the explicit rules around observability with this call.
        final LocalizedProblem problem = LocalizedProblem.from("f");
        final RetNullable<Long> res = RetNullable.fromProblem(problem);
        // Ensure it is not observed right after creation...
        assertEquals(List.of(res), this.monitor.getNeverObserved());

        // Call and check result
        assertEquals(List.of(problem), res.anyProblems());

        // Ensure that, after being called, it is marked as observed.
        assertEquals(List.of(), this.monitor.getNeverObserved());
    }

    @Test
    void validProblems_ok() {
        // Test the explicit rules around observability with this call.
        final RetNullable<Long> res = RetNullable.ok(Long.MAX_VALUE);
        // Ensure it is not observed right after creation...
        assertEquals(List.of(res), this.monitor.getNeverObserved());

        // Call and check result
        try {
            assertEquals(List.of(), res.validProblems());
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // don't inspect the exception
        }

        // Ensure that, after being called, it is marked as unobserved.
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    @Test
    void validProblems_problem() {
        // Test the explicit rules around observability with this call.
        final LocalizedProblem problem = LocalizedProblem.from("f");
        final RetNullable<Long> res = RetNullable.fromProblem(problem);
        // Ensure it is not observed right after creation...
        assertEquals(List.of(res), this.monitor.getNeverObserved());

        // Call and check result
        assertEquals(List.of(problem), res.validProblems());

        // Ensure that, after being called, it is marked as unobserved.
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    @Test
    void joinProblemsWith_ok() {
        // Test the explicit rules around observability with this call.
        final RetNullable<Long> res = RetNullable.ok(Long.MAX_VALUE);
        // Ensure it is not observed right after creation...
        assertEquals(List.of(res), this.monitor.getNeverObserved());

        // Call and check result
        final List<Problem> probs = new ArrayList<>();
        res.joinProblemsWith(probs);

        // Ensure that, after being called, it is marked as observed.
        assertEquals(List.of(), this.monitor.getNeverObserved());

        assertEquals(List.of(), probs);
    }

    @Test
    void joinProblemsWith_problem() {
        // Test the explicit rules around observability with this call.
        final LocalizedProblem problem = LocalizedProblem.from("f");
        final RetNullable<Long> res = RetNullable.fromProblem(problem);
        // Ensure it is not observed right after creation...
        assertEquals(List.of(res), this.monitor.getNeverObserved());

        // Call and check result
        final List<Problem> probs = new ArrayList<>();
        res.joinProblemsWith(probs);

        // Ensure that, after being called, it is marked as observed.
        assertEquals(List.of(), this.monitor.getNeverObserved());

        assertEquals(List.of(problem), probs);
    }

    @Test
    void debugProblems_empty() {
        assertEquals(
                "",
                RetNullable.ok("x").debugProblems(";")
        );
    }

    @Test
    void debugProblems_one() {
        assertEquals(
                "a",
                RetNullable.fromProblem(LocalizedProblem.from("a"))
                        .debugProblems(";")
        );
    }

    @Test
    void debugProblems_two() {
        assertEquals(
                "a;bb",
                RetNullable.fromProblem(
                        LocalizedProblem.from("a"),
                        LocalizedProblem.from("bb")
                )
                .debugProblems(";")
        );
    }

    @Test
    void toString_ok() {
        assertEquals(
                "RetNullable(value: x)",
                RetNullable.ok("x").toString()
        );
    }

    @Test
    void toString_problems() {
        assertEquals(
                "RetNullable(2 problems: abc; def)",
                RetNullable.fromProblem(
                        LocalizedProblem.from("abc"),
                        LocalizedProblem.from("def")
                ).toString()
        );
    }


    @BeforeEach
    void beforeEach() {
        this.monitor = MockProblemMonitor.setup();
        // Ensure RetVoid.ok() returns separate values.
        this.monitor.traceEnabled = true;
    }

    @AfterEach
    void afterEach() {
        this.monitor.tearDown();
    }
}
