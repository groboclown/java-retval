// Released under the MIT License. 
package net.groboclown.retval;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import net.groboclown.retval.impl.MockCheckMonitor;
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

class RetValTest {
    MockCheckMonitor monitor;

    @Test
    void ok_nonnull() {
        final RetVal<String> res = RetVal.ok("value");
        assertEquals(List.of(res), this.monitor.getNeverChecked());
        assertEquals("value", res.result());
        assertEquals("value", res.getValue());
        assertTrue(res.isOk());
        assertFalse(res.hasProblems());
        assertFalse(res.isProblem());
        assertEquals(List.of(), res.anyProblems());
    }

    @Test
    void ok_null() {
        try {
            RetVal.ok(null);
            fail("Did not throw NPE");
        } catch (final NullPointerException e) {
            // skip exception inspection
        }
    }

    @Test
    void fromProblems_Problem_notNull1() {
        final LocalizedProblem p1 = LocalizedProblem.from("p1");
        final RetVal<Object> res = RetVal.fromProblem(p1);
        assertEquals(List.of(res), this.monitor.getNeverChecked());
        assertFalse(res.isOk());
        assertTrue(res.isProblem());
        assertTrue(res.hasProblems());
        assertEquals(List.of(p1), res.anyProblems());
        assertEquals(List.of(p1), res.validProblems());
    }

    @Test
    void fromProblem_Problem_notNull2() {
        final LocalizedProblem p1 = LocalizedProblem.from("p1");
        final LocalizedProblem p2 = LocalizedProblem.from("p2");
        final RetVal<Object> res = RetVal.fromProblem(p1, p2);
        assertEquals(List.of(res), this.monitor.getNeverChecked());
        assertFalse(res.isOk());
        assertTrue(res.isProblem());
        assertTrue(res.hasProblems());
        assertEquals(List.of(p1, p2), res.anyProblems());
        assertEquals(List.of(p1, p2), res.validProblems());
    }

    @Test
    void fromProblem_Problem_null() {
        try {
            // This shows why the API makes the poor choice of passing null values
            // difficult, even though it's a runtime error instead of a compile error.
            // But this is explicit null values instead of possibly null values...
            RetVal.fromProblem((Problem) null);
            fail("Did not throw IAE");
        } catch (final IllegalArgumentException e) {
            // skip exception inspection
            // Should perform empty check before getting the monitor callback.
            assertEquals(List.of(), this.monitor.getNeverChecked());
        }
    }

    @Test
    void fromProblem_Problem_someNull() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        // Again, explicit null values make the API tricky to work with.
        final RetVal<Object> res = RetVal.fromProblem(problem, null, null);
        assertEquals(List.of(res), this.monitor.getNeverChecked());
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void fromProblem_CollectionProblem_notNull1() {
        final LocalizedProblem p1 = LocalizedProblem.from("p1");
        final LocalizedProblem p2 = LocalizedProblem.from("p2");
        final RetVal<Object> res = RetVal.fromProblem(List.of(p1, p2));
        assertEquals(List.of(res), this.monitor.getNeverChecked());
        assertFalse(res.isOk());
        assertTrue(res.isProblem());
        assertTrue(res.hasProblems());
        assertEquals(List.of(p1, p2), res.anyProblems());
        assertEquals(List.of(p1, p2), res.validProblems());
    }

    @Test
    void fromProblem_CollectionProblem_notNull2() {
        final LocalizedProblem p1 = LocalizedProblem.from("p1");
        final LocalizedProblem p2 = LocalizedProblem.from("p2");
        final RetVal<Object> res = RetVal.fromProblem(List.of(p1), List.of(p2));
        assertEquals(List.of(res), this.monitor.getNeverChecked());
        assertFalse(res.isOk());
        assertTrue(res.isProblem());
        assertTrue(res.hasProblems());
        assertEquals(List.of(p1, p2), res.anyProblems());
        assertEquals(List.of(p1, p2), res.validProblems());
    }

    @Test
    void fromProblem_CollectionProblem_notNull3() {
        final LocalizedProblem p1 = LocalizedProblem.from("p1");
        final LocalizedProblem p2 = LocalizedProblem.from("p2");
        final RetVal<Object> res = RetVal.fromProblem(List.of(p1, p2), List.of());
        assertEquals(List.of(res), this.monitor.getNeverChecked());
        assertFalse(res.isOk());
        assertTrue(res.isProblem());
        assertTrue(res.hasProblems());
        assertEquals(List.of(p1, p2), res.anyProblems());
        assertEquals(List.of(p1, p2), res.validProblems());
    }

    @Test
    void fromProblem_CollectionProblem_empty() {
        try {
            RetVal.fromProblem(List.of());
            fail("Did not throw IAE");
        } catch (final IllegalArgumentException e) {
            // skip exception inspection
            // Should perform empty check before getting the monitor callback.
            assertEquals(List.of(), this.monitor.getNeverChecked());
        }
    }

    @Test
    void fromProblem_CollectionProblem_null1() {
        try {
            // This shows why the API makes the poor choice of passing null values
            // difficult, even though it's a runtime error instead of a compile error.
            // But this is explicit null values instead of possibly null values...
            RetVal.fromProblem((Collection<Problem>) null);
            fail("Did not throw IAE");
        } catch (final IllegalArgumentException e) {
            // skip exception inspection
            // Should perform empty check before getting the monitor callback.
            assertEquals(List.of(), this.monitor.getNeverChecked());
        }
    }

    @Test
    void fromProblem_CollectionProblem_null2() {
        try {
            // This shows why the API makes the poor choice of passing null values
            // difficult, even though it's a runtime error instead of a compile error.
            // But this is explicit null values instead of possibly null values...
            RetVal.fromProblem(Arrays.asList(null, null));
            fail("Did not throw IAE");
        } catch (final IllegalArgumentException e) {
            // skip exception inspection
            // Should perform empty check before getting the monitor callback.
            assertEquals(List.of(), this.monitor.getNeverChecked());
        }
    }

    @Test
    void fromProblem_CollectionProblem_someNull_someEmpty() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVal<Object> res = RetVal.fromProblem(List.of(problem), null, List.of());
        assertEquals(List.of(res), this.monitor.getNeverChecked());
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void fromProblems_ProblemContainer_notNull1() {
        final LocalizedProblem p1 = LocalizedProblem.from("p1");
        final RetVal<Object> res = RetVal.fromProblems(RetVal.fromProblem(p1));
        assertEquals(List.of(res), this.monitor.getNeverChecked());
        assertFalse(res.isOk());
        assertTrue(res.isProblem());
        assertTrue(res.hasProblems());
        assertEquals(List.of(p1), res.anyProblems());
        assertEquals(List.of(p1), res.validProblems());
    }

    @Test
    void fromProblems_ProblemContainer_notNull2() {
        final LocalizedProblem p1 = LocalizedProblem.from("p1");
        final LocalizedProblem p2 = LocalizedProblem.from("p2");
        final RetVal<Object> res = RetVal.fromProblems(
                RetVal.fromProblem(p1), RetVal.fromProblem(p2)
        );
        assertEquals(List.of(res), this.monitor.getNeverChecked());
        assertFalse(res.isOk());
        assertTrue(res.isProblem());
        assertTrue(res.hasProblems());
        assertEquals(List.of(p1, p2), res.anyProblems());
        assertEquals(List.of(p1, p2), res.validProblems());
    }

    @Test
    void fromProblems_ProblemContainer_notNull3() {
        final LocalizedProblem p1 = LocalizedProblem.from("p1");
        final LocalizedProblem p2 = LocalizedProblem.from("p2");
        final LocalizedProblem p3 = LocalizedProblem.from("p3");
        final LocalizedProblem p4 = LocalizedProblem.from("p4");
        final RetVal<Object> res = RetVal.fromProblems(
                RetVal.fromProblem(p1, p2), RetVal.fromProblem(p3, p4)
        );
        assertEquals(List.of(res), this.monitor.getNeverChecked());
        assertFalse(res.isOk());
        assertTrue(res.isProblem());
        assertTrue(res.hasProblems());
        assertEquals(List.of(p1, p2, p3, p4), res.anyProblems());
        assertEquals(List.of(p1, p2, p3, p4), res.validProblems());
    }

    @Test
    void fromProblems_ProblemContainer_null() {
        try {
            // Again, this shows invalid API usage difficulties.
            RetVal.fromProblems((ProblemContainer) null);
            fail("Did not throw IAE");
        } catch (final IllegalArgumentException e) {
            // skip exception inspection
            // Should perform empty check before getting the monitor callback.
            assertEquals(List.of(), this.monitor.getNeverChecked());
        }
    }

    @Test
    void fromProblems_ProblemContainer_someNull_someOk() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVal<Object> res = RetVal.fromProblems(
                RetVal.fromProblem(problem), RetVal.ok("x"), null
        );
        assertEquals(List.of(res), this.monitor.getNeverChecked());
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void fromProblems_CollectionProblemContainer_notNull1() {
        final LocalizedProblem p1 = LocalizedProblem.from("p1");
        final LocalizedProblem p2 = LocalizedProblem.from("p2");
        final RetVal<Object> res = RetVal.fromProblems(List.of(
                RetVal.fromProblem(p1),
                RetVal.fromProblem(p2),
                RetVal.ok("x"),
                RetVal.ok(1)
        ));
        assertEquals(List.of(res), this.monitor.getNeverChecked());
        assertFalse(res.isOk());
        assertTrue(res.isProblem());
        assertTrue(res.hasProblems());
        assertEquals(List.of(p1, p2), res.anyProblems());
        assertEquals(List.of(p1, p2), res.validProblems());
    }

    @Test
    void fromProblems_CollectionProblemContainer_notNull2() {
        final LocalizedProblem p1 = LocalizedProblem.from("p1");
        final LocalizedProblem p2 = LocalizedProblem.from("p2");
        final RetVal<Object> res = RetVal.fromProblems(
                List.of(RetVal.fromProblem(p1)),
                List.of(RetVal.fromProblem(p2))
        );
        assertEquals(List.of(res), this.monitor.getNeverChecked());
        assertFalse(res.isOk());
        assertTrue(res.isProblem());
        assertTrue(res.hasProblems());
        assertEquals(List.of(p1, p2), res.anyProblems());
        assertEquals(List.of(p1, p2), res.validProblems());
    }

    @Test
    void fromProblems_CollectionProblemContainer_notNull3() {
        final LocalizedProblem p1 = LocalizedProblem.from("p1");
        final LocalizedProblem p2 = LocalizedProblem.from("p2");
        final LocalizedProblem p3 = LocalizedProblem.from("p3");
        final RetVal<Object> res = RetVal.fromProblems(
                List.of(RetVal.fromProblem(p1), RetVal.fromProblem(p2, p3)),
                List.of()
        );
        assertEquals(List.of(res), this.monitor.getNeverChecked());
        assertFalse(res.isOk());
        assertTrue(res.isProblem());
        assertTrue(res.hasProblems());
        assertEquals(List.of(p1, p2, p3), res.anyProblems());
        assertEquals(List.of(p1, p2, p3), res.validProblems());
    }

    @Test
    void fromProblems_CollectionProblemContainer_empty() {
        try {
            RetVal.fromProblems(List.of());
            fail("Did not throw IAE");
        } catch (final IllegalArgumentException e) {
            // skip exception inspection
            // Should perform empty check before getting the monitor callback.
            assertEquals(List.of(), this.monitor.getNeverChecked());
        }
    }

    @Test
    void fromProblems_CollectionProblemContainer_null1() {
        try {
            // Again, api usage difficulty
            RetVal.fromProblems((Collection<ProblemContainer>) null);
            fail("Did not throw IAE");
        } catch (final IllegalArgumentException e) {
            // skip exception inspection
            // Should perform empty check before getting the monitor callback.
            assertEquals(List.of(), this.monitor.getNeverChecked());
        }
    }

    @Test
    void fromProblems_CollectionProblemContainer_null2() {
        try {
            // Again, api usage difficulty
            RetVal.fromProblems(Arrays.asList(null, null));
            fail("Did not throw IAE");
        } catch (final IllegalArgumentException e) {
            // skip exception inspection
            // Should perform empty check before getting the monitor callback.
            assertEquals(List.of(), this.monitor.getNeverChecked());
        }
    }

    @Test
    void fromProblems_CollectionProblemContainer_someNull_someEmpty() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVal<Object> res = RetVal.fromProblems(
                List.of(RetVal.fromProblem(problem)),
                null, List.of()
        );
        assertEquals(List.of(res), this.monitor.getNeverChecked());
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void getValue_problem() {
        final RetVal<Object> res = RetVal.fromProblem(LocalizedProblem.from("x"));
        assertNull(res.getValue());
        assertEquals(List.of(res), this.monitor.getNeverChecked());
    }

    @Test
    void getValue_ok() {
        final RetVal<String> res = RetVal.ok("a");
        assertEquals("a", res.getValue());
        assertEquals(List.of(res), this.monitor.getNeverChecked());
    }

    @Test
    void asOptional_problem() {
        final RetVal<Object> res = RetVal.fromProblem(LocalizedProblem.from("x"));
        assertTrue(res.asOptional().isEmpty());
        assertEquals(List.of(res), this.monitor.getNeverChecked());
    }

    @Test
    void asOptional_ok() {
        final RetVal<String> res = RetVal.ok("a");
        assertTrue(res.asOptional().isPresent());
        assertEquals(List.of(res), this.monitor.getNeverChecked());
    }

    @Test
    void result_problem() {
        final RetVal<Object> res = RetVal.fromProblem(LocalizedProblem.from("x"));
        try {
            res.result();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
        assertEquals(List.of(res), this.monitor.getNeverChecked());
    }

    @Test
    void result_ok() {
        final RetVal<String> res = RetVal.ok("a");
        assertEquals("a", res.result());
        assertEquals(List.of(res), this.monitor.getNeverChecked());
    }

    @Test
    void forwardProblems_ok() {
        final RetVal<String> res = RetVal.ok("a");
        try {
            res.forwardProblems();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
        assertEquals(List.of(res), this.monitor.getNeverChecked());
    }

    @Test
    void forwardProblems_problems_traceEnabled() {
        this.monitor.traceEnabled = true;

        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVal<String> res = RetVal.fromProblem(problem);
        // Notice the implicit API usage check for altering the signature to an incompatible
        // type.
        final RetVal<Integer> forwarded = res.forwardProblems();
        assertNotSame(res, forwarded);

        // the "res" should be marked as checked, and the forwarded one as not.
        // This is due to the usecase of using this "forward" call to cause the
        // previous value to go out-of-scope and pass the problem management to the
        // new object.
        assertEquals(List.of(forwarded), this.monitor.getNeverChecked());

        assertEquals(List.of(problem), forwarded.anyProblems());
        assertTrue(forwarded.hasProblems());
    }

    @Test
    void forwardProblems_problems_traceDisabled() {
        this.monitor.traceEnabled = false;

        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVal<String> res = RetVal.fromProblem(problem);
        // Notice the implicit API usage check for altering the signature to an incompatible
        // type.
        final RetVal<Integer> forwarded = res.forwardProblems();
        assertSame(res, forwarded);

        // because both "res" and "forwarded" are the same object, just one
        // of them should be in the never-checked list.
        assertEquals(List.of(res), this.monitor.getNeverChecked());

        assertEquals(List.of(problem), forwarded.anyProblems());
        assertTrue(forwarded.hasProblems());
    }

    @Test
    void forwardNullableProblems_ok() {
        final RetVal<String> res = RetVal.ok("a");
        try {
            res.forwardNullableProblems();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
        assertEquals(List.of(res), this.monitor.getNeverChecked());
    }

    @Test
    void forwardNullableProblems_problems_traceEnabled() {
        // Even though the implementation does not have code that checks
        // for the trace-enabled state, it's added here to help ensure future
        // coding does the right thing.
        this.monitor.traceEnabled = false;

        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVal<String> res = RetVal.fromProblem(problem);
        // Notice the implicit API usage check for altering the signature to an incompatible
        // type.
        final RetNullable<Integer> forwarded = res.forwardNullableProblems();
        assertNotSame(res, forwarded);

        // the "res" should be marked as checked, and the forwarded one as not.
        // This is due to the usecase of using this "forward" call to cause the
        // previous value to go out-of-scope and pass the problem management to the
        // new object.
        assertEquals(List.of(forwarded), this.monitor.getNeverChecked());

        assertEquals(List.of(problem), forwarded.anyProblems());
        assertTrue(forwarded.hasProblems());
    }

    @Test
    void asNullable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        // Note: RetVal<Void> is impossible with a non-problem value.
        final RetVal<Void> res = RetVal.fromProblem(problem);
        final RetNullable<Void> val = res.asNullable();
        // should have passed checking from res to val
        assertEquals(List.of(val), this.monitor.getNeverChecked());

        assertTrue(val.hasProblems());
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void asNullable_ok() {
        final RetVal<String> res = RetVal.ok("a");
        final RetNullable<String> val = res.asNullable();
        // should have transferred check ownership to val
        assertEquals(List.of(val), this.monitor.getNeverChecked());

        assertEquals("a", val.result());
        assertEquals(List.of(), val.anyProblems());
    }

    @Test
    void thenValidate_initialProblem() {
        final String[] acceptedValue = {"not set"};
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVal<String> res = RetVal.fromProblem(problem);
        final RetVal<String> val = res.thenValidate((v) -> {
            throw new IllegalStateException("unreachable code");
        });

        // Because there were problems in the original value, the original value was
        // returned.
        assertSame(res, val);
        // On top of this, a check should not have been made.
        assertEquals(List.of(res), this.monitor.getNeverChecked());
        // And the original problem list should remain.
        assertEquals(List.of(problem), val.anyProblems());
        // And the callback should not have been called.
        assertArrayEquals(new String[] {"not set"}, acceptedValue);
    }

    @Test
    void thenValidate_ok_nullOk() {
        final String[] acceptedValue = {"not set"};
        final int[] callCount = {0};
        final RetVal<String> res = RetVal.ok("value");
        final RetVal<String> val = res.thenValidate((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return null;
        });
        // Because the state of the original value didn't change, the original value
        // should have been returned.
        assertSame(res, val);
        // On top of this, a check should not have been made.  And the object should
        // not have been added again to the check list.
        assertEquals(List.of(res), this.monitor.getNeverChecked());
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
        final RetVal<String> res = RetVal.ok("value");
        final RetVal<String> val = res.thenValidate((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return RetVoid.ok();  // non-null and no problems
        });
        // Because the state of the original value didn't change, the original value
        // should have been returned.
        assertSame(res, val);
        // On top of this, a check should not have been made.  And the object should
        // not have been added again to the check list.
        assertEquals(List.of(res), this.monitor.getNeverChecked());
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
        final RetVal<String> res = RetVal.ok("value");
        final RetVal<String> val = res.thenValidate((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return RetVoid.fromProblem(problem);
        });
        // Because the problem state changed, these values must be different.
        assertNotSame(res, val);
        // The check must have passed from the first to the second.
        assertEquals(List.of(val), this.monitor.getNeverChecked());
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
        final RetVal<Integer> res = RetVal.ok(3);
        final RetVal<Integer> val = res.then((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return RetVal.ok(v + 2);
        });
        assertNotSame(res, val);
        // The check must have passed from the first to the second.
        assertEquals(List.of(val), this.monitor.getNeverChecked());
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
        final RetVal<Integer> res = RetVal.ok(3);
        final RetVal<Integer> val = res.then((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return RetVal.fromProblem(problem);
        });
        assertNotSame(res, val);
        // The check must have passed from the first to the second.
        assertEquals(List.of(val), this.monitor.getNeverChecked());
        // And the state should be different.
        assertEquals(List.of(problem), val.anyProblems());
        assertEquals(3, acceptedValue[0]);
        assertEquals(1, callCount[0]);
        assertTrue(val.hasProblems());
    }

    @Test
    void then_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetVal<Integer> res = RetVal.fromProblem(problem);
        final RetVal<Integer> val = res.then((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertSame(res, val);
        assertEquals(List.of(val), this.monitor.getNeverChecked());
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void map_ok() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final RetVal<Integer> res = RetVal.ok(3);
        final RetVal<Integer> val = res.map((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return v + 2;
        });
        assertNotSame(res, val);
        // The check must have passed from the first to the second.
        assertEquals(List.of(val), this.monitor.getNeverChecked());
        // And the state should be different.
        assertEquals(List.of(), val.anyProblems());
        assertEquals(5, val.result());
        assertEquals(3, acceptedValue[0]);
        assertEquals(1, callCount[0]);
    }

    @Test
    void map_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetVal<Integer> res = RetVal.fromProblem(problem);
        final RetVal<Integer> val = res.map((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertSame(res, val);
        assertEquals(List.of(val), this.monitor.getNeverChecked());
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void thenNullable_ok_ok() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final RetVal<Integer> res = RetVal.ok(3);
        final RetNullable<Integer> val = res.thenNullable((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return RetNullable.ok(v + 2);
        });
        assertNotSame(res, val);
        // The check must have passed from the first to the second.
        assertEquals(List.of(val), this.monitor.getNeverChecked());
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
        final RetVal<Integer> res = RetVal.ok(3);
        final RetNullable<Integer> val = res.thenNullable((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return RetNullable.ok(null);
        });
        assertNotSame(res, val);
        // The check must have passed from the first to the second.
        assertEquals(List.of(val), this.monitor.getNeverChecked());
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
        final RetVal<Integer> res = RetVal.ok(3);
        final RetNullable<Integer> val = res.thenNullable((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return RetNullable.fromProblem(problem);
        });
        assertNotSame(res, val);
        // The check must have passed from the first to the second.
        assertEquals(List.of(val), this.monitor.getNeverChecked());
        // And the state should be different.
        assertEquals(List.of(problem), val.anyProblems());
        assertEquals(3, acceptedValue[0]);
        assertEquals(1, callCount[0]);
        assertTrue(val.hasProblems());
    }

    @Test
    void thenNullable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetVal<Integer> res = RetVal.fromProblem(problem);
        final RetNullable<Integer> val = res.thenNullable((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertEquals(List.of(val), this.monitor.getNeverChecked());
        assertEquals(List.of(problem), val.anyProblems());
    }

    @Test
    void mapNullable_ok() {
        final int[] acceptedValue = {0};
        final int[] callCount = {0};
        final RetVal<Integer> res = RetVal.ok(3);
        final RetNullable<Integer> val = res.mapNullable((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return v + 2;
        });
        assertNotSame(res, val);
        // The check must have passed from the first to the second.
        assertEquals(List.of(val), this.monitor.getNeverChecked());
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
        final RetVal<Integer> res = RetVal.ok(3);
        final RetNullable<Integer> val = res.mapNullable((v) -> {
            acceptedValue[0] = v;
            callCount[0]++;
            return null;
        });
        // The check must have passed from the first to the second.
        assertEquals(List.of(val), this.monitor.getNeverChecked());
        // And the state should be different.
        assertEquals(List.of(), val.anyProblems());
        assertNull(val.result());
        assertEquals(3, acceptedValue[0]);
        assertEquals(1, callCount[0]);
    }

    @Test
    void mapNullable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final RetVal<Integer> res = RetVal.fromProblem(problem);
        final RetNullable<Integer> val = res.mapNullable((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertEquals(List.of(val), this.monitor.getNeverChecked());
        assertEquals(List.of(problem), val.anyProblems());
    }

    // TODO test thenRun(Runnable)
    // TODO test thenRun(NonnullConsumer)
    // TODO explicitly test isOk(), hasProblems(), isProblem(), anyProblems(), validateProblems()

    @Test
    void debugProblems_empty() {
        assertEquals(
                "",
                RetVal.ok("x").debugProblems(";")
        );
    }

    @Test
    void debugProblems_one() {
        assertEquals(
                "a",
                RetVal.fromProblem(LocalizedProblem.from("a"))
                        .debugProblems(";")
        );
    }

    @Test
    void debugProblems_two() {
        assertEquals(
                "a;bb",
                RetVal.fromProblem(
                        LocalizedProblem.from("a"),
                        LocalizedProblem.from("bb")
                )
                .debugProblems(";")
        );
    }


    @BeforeEach
    void beforeEach() {
        this.monitor = MockCheckMonitor.setup();
    }

    @AfterEach
    void afterEach() {
        this.monitor.tearDown();
    }
}
