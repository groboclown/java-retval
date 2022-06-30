// Released under the MIT License. 
package net.groboclown.retval;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import net.groboclown.retval.monitor.MockProblemMonitor;
import net.groboclown.retval.problems.LocalizedProblem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class RetNullableStaticTest {
    MockProblemMonitor monitor;

    @Test
    void ok_nonnull() {
        final RetNullable<String> res = RetNullable.ok("value");
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals("value", res.result());
        assertEquals("value", res.getValue());
        assertTrue(res.isOk());
        assertFalse(res.hasProblems());
        assertFalse(res.isProblem());
        assertEquals(List.of(), res.anyProblems());
    }

    @Test
    void ok_null() {
        final RetNullable<String> res = RetNullable.ok(null);
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertNull(res.result());
        assertNull(res.getValue());
        assertTrue(res.isOk());
        assertFalse(res.hasProblems());
        assertFalse(res.isProblem());
        assertEquals(List.of(), res.anyProblems());
    }

    @Test
    void fromProblems_Problem_notNull1() {
        final LocalizedProblem p1 = LocalizedProblem.from("p1");
        final RetNullable<Object> res = RetNullable.fromProblem(p1);
        assertEquals(List.of(res), this.monitor.getNeverObserved());
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
        final RetNullable<Object> res = RetNullable.fromProblem(p1, p2);
        assertEquals(List.of(res), this.monitor.getNeverObserved());
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
            RetNullable.fromProblem((Problem) null);
            fail("Did not throw IAE");
        } catch (final IllegalArgumentException e) {
            // skip exception inspection
            // Should perform empty check before getting the monitor callback.
            assertEquals(List.of(), this.monitor.getNeverObserved());
        }
    }

    @Test
    void fromProblem_Problem_someNull() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        // Again, explicit null values make the API tricky to work with.
        final RetNullable<Object> res = RetNullable.fromProblem(problem, null, null);
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void fromProblem_CollectionProblem_notNull1() {
        final LocalizedProblem p1 = LocalizedProblem.from("p1");
        final LocalizedProblem p2 = LocalizedProblem.from("p2");
        final RetNullable<Object> res = RetNullable.fromProblem(List.of(p1, p2));
        assertEquals(List.of(res), this.monitor.getNeverObserved());
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
        final RetNullable<Object> res = RetNullable.fromProblem(List.of(p1), List.of(p2));
        assertEquals(List.of(res), this.monitor.getNeverObserved());
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
        final RetNullable<Object> res = RetNullable.fromProblem(List.of(p1, p2), List.of());
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertFalse(res.isOk());
        assertTrue(res.isProblem());
        assertTrue(res.hasProblems());
        assertEquals(List.of(p1, p2), res.anyProblems());
        assertEquals(List.of(p1, p2), res.validProblems());
    }

    @Test
    void fromProblem_CollectionProblem_empty() {
        try {
            RetNullable.fromProblem(List.of());
            fail("Did not throw IAE");
        } catch (final IllegalArgumentException e) {
            // skip exception inspection
            // Should perform empty check before getting the monitor callback.
            assertEquals(List.of(), this.monitor.getNeverObserved());
        }
    }

    @Test
    void fromProblem_CollectionProblem_null1() {
        try {
            // This shows why the API makes the poor choice of passing null values
            // difficult, even though it's a runtime error instead of a compile error.
            // But this is explicit null values instead of possibly null values...
            RetNullable.fromProblem((Collection<Problem>) null);
            fail("Did not throw IAE");
        } catch (final IllegalArgumentException e) {
            // skip exception inspection
            // Should perform empty check before getting the monitor callback.
            assertEquals(List.of(), this.monitor.getNeverObserved());
        }
    }

    @Test
    void fromProblem_CollectionProblem_null2() {
        try {
            // This shows why the API makes the poor choice of passing null values
            // difficult, even though it's a runtime error instead of a compile error.
            // But this is explicit null values instead of possibly null values...
            RetNullable.fromProblem(Arrays.asList(null, null));
            fail("Did not throw IAE");
        } catch (final IllegalArgumentException e) {
            // skip exception inspection
            // Should perform empty check before getting the monitor callback.
            assertEquals(List.of(), this.monitor.getNeverObserved());
        }
    }

    @Test
    void fromProblem_CollectionProblem_someNull_someEmpty() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetNullable<Object> res = RetNullable.fromProblem(List.of(problem), null, List.of());
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void fromProblems_ProblemContainer_notNull1() {
        final LocalizedProblem p1 = LocalizedProblem.from("p1");
        final RetNullable<Object> res = RetNullable.fromProblems(RetVal.fromProblem(p1));
        assertEquals(List.of(res), this.monitor.getNeverObserved());
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
        final RetNullable<Object> res = RetNullable.fromProblems(
                RetVal.fromProblem(p1), RetVal.fromProblem(p2)
        );
        assertEquals(List.of(res), this.monitor.getNeverObserved());
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
        final RetNullable<Object> res = RetNullable.fromProblems(
                RetVal.fromProblem(p1, p2), RetVal.fromProblem(p3, p4)
        );
        assertEquals(List.of(res), this.monitor.getNeverObserved());
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
            RetNullable.fromProblems((ProblemContainer) null);
            fail("Did not throw IAE");
        } catch (final IllegalArgumentException e) {
            // skip exception inspection
            // Should perform empty check before getting the monitor callback.
            assertEquals(List.of(), this.monitor.getNeverObserved());
        }
    }

    @Test
    void fromProblems_ProblemContainer_someNull_someOk() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetNullable<Object> res = RetNullable.fromProblems(
                RetVal.fromProblem(problem), RetVal.ok("x"), null
        );
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void fromProblems_CollectionProblemContainer_notNull1() {
        final LocalizedProblem p1 = LocalizedProblem.from("p1");
        final LocalizedProblem p2 = LocalizedProblem.from("p2");
        final RetNullable<Object> res = RetNullable.fromProblems(List.of(
                RetVal.fromProblem(p1),
                RetVal.fromProblem(p2),
                RetVal.ok("x"),
                RetVal.ok(1)
        ));
        assertEquals(List.of(res), this.monitor.getNeverObserved());
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
        final RetNullable<Object> res = RetNullable.fromProblems(
                List.of(RetVal.fromProblem(p1)),
                List.of(RetVal.fromProblem(p2))
        );
        assertEquals(List.of(res), this.monitor.getNeverObserved());
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
        final RetNullable<Object> res = RetNullable.fromProblems(
                List.of(RetVal.fromProblem(p1), RetVal.fromProblem(p2, p3)),
                List.of()
        );
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertFalse(res.isOk());
        assertTrue(res.isProblem());
        assertTrue(res.hasProblems());
        assertEquals(List.of(p1, p2, p3), res.anyProblems());
        assertEquals(List.of(p1, p2, p3), res.validProblems());
    }

    @Test
    void fromProblems_CollectionProblemContainer_empty() {
        try {
            RetNullable.fromProblems(List.of());
            fail("Did not throw IAE");
        } catch (final IllegalArgumentException e) {
            // skip exception inspection
            // Should perform empty check before getting the monitor callback.
            assertEquals(List.of(), this.monitor.getNeverObserved());
        }
    }

    @Test
    void fromProblems_CollectionProblemContainer_null1() {
        try {
            // Again, api usage difficulty
            RetNullable.fromProblems((Collection<ProblemContainer>) null);
            fail("Did not throw IAE");
        } catch (final IllegalArgumentException e) {
            // skip exception inspection
            // Should perform empty check before getting the monitor callback.
            assertEquals(List.of(), this.monitor.getNeverObserved());
        }
    }

    @Test
    void fromProblems_CollectionProblemContainer_null2() {
        try {
            // Again, api usage difficulty
            RetNullable.fromProblems(Arrays.asList(null, null));
            fail("Did not throw IAE");
        } catch (final IllegalArgumentException e) {
            // skip exception inspection
            // Should perform empty check before getting the monitor callback.
            assertEquals(List.of(), this.monitor.getNeverObserved());
        }
    }

    @Test
    void fromProblems_CollectionProblemContainer_someNull_someEmpty() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetNullable<Object> res = RetNullable.fromProblems(
                List.of(RetVal.fromProblem(problem)),
                null, List.of()
        );
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void result_default_nonnullValue() {
        // Default method testing.

        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        src.value = new Object();
        assertEquals(src.value, src.result("foo"));
    }

    @Test
    void result_default_nullValue() {
        // Default method testing.

        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        src.value = null;
        assertEquals("foo", src.result("foo"));
    }

    @Test
    void result_default_problem() {
        // Default method testing.

        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        src.value = null;
        assertEquals("foo", src.result("foo"));
    }

    @Test
    void requireNonNull_nonnullValue() {
        // Default method testing.

        final LocalizedProblem problem = LocalizedProblem.from("x");
        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        src.value = new Object();

        final RetVal<Object> res = src.requireNonNull(problem);
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(), res.anyProblems());
        assertEquals(src.value, res.getValue());
    }

    @Test
    void requireNonNull_nullValue() {
        // Default method testing.

        final LocalizedProblem problem = LocalizedProblem.from("x");
        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        src.value = null;

        final RetVal<Object> res = src.requireNonNull(problem);
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void requireNonNull_withProblems() {
        // Default method testing.

        final LocalizedProblem problem = LocalizedProblem.from("x");
        final LocalizedProblem ignored = LocalizedProblem.from("y");
        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        src.problems.add(problem);

        final RetVal<Object> res = src.requireNonNull(ignored);
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void consumeIfNonnull_nonnullValue() {
        // Default method testing.

        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        src.value = new Object();
        final Object[] consumed = new Object[] { null };

        final RetVoid res = src.consumeIfNonnull((v) -> consumed[0] = v);
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(), res.anyProblems());
        assertSame(src.value, consumed[0]);
    }

    @Test
    void consumeIfNonnull_nullValue() {
        // Default method testing.

        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        src.value = null;
        final RetVoid res = src.consumeIfNonnull((v) -> {
            throw new RuntimeException("Should never be called");
        });
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(), res.anyProblems());
    }

    @Test
    void consumeIfNonnull_problems() {
        // Default method testing.

        final LocalizedProblem problem = LocalizedProblem.from("x");
        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        src.problems.add(problem);
        final RetVoid res = src.consumeIfNonnull((v) -> {
            throw new RuntimeException("Should never be called");
        });
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void defaultAs_nonnullValue() {
        // Default method testing.

        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        src.value = new Object();

        RetVal<Object> res = src.defaultAs(new Object());
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(), res.anyProblems());
        assertSame(src.value, res.result());
    }

    @Test
    void defaultAs_nullValue() {
        // Default method testing.

        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        final Object expected = new Object();
        src.value = null;

        RetVal<Object> res = src.defaultAs(expected);
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(), res.anyProblems());
        assertSame(expected, res.result());
    }

    @Test
    void defaultAs_problems() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        src.problems.add(problem);
        RetVal<Object> res = src.defaultAs(new Object());
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void defaultOrThen_nonnullValue() {
        // Default method testing.

        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        src.value = new Object();
        final Object[] consumed = new Object[] { null };

        RetVal<String> res = src.defaultOrThen("other", (v) -> {
            consumed[0] = v;
            return RetVal.ok("value");
        });
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(), res.anyProblems());
        assertSame(src.value, consumed[0]);
        assertEquals("value", res.result());
    }

    @Test
    void defaultOrThen_nullValue() {
        // Default method testing.

        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        src.value = null;
        RetVal<String> res = src.defaultOrThen("other", (v) -> {
            throw new RuntimeException("Should never be called");
        });
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(), res.anyProblems());
        assertEquals("other", res.result());
    }

    @Test
    void defaultOrThen_problems() {
        // Default method testing.

        final LocalizedProblem problem = LocalizedProblem.from("x");
        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        src.problems.add(problem);
        final RetVal<String> res = src.defaultOrThen("other", (v) -> {
            throw new RuntimeException("Should never be called");
        });

        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void defaultOrMap_nonnullValue() {
        // Default method testing.

        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        src.value = new Object();
        final Object[] consumed = new Object[] { null };

        RetVal<String> res = src.defaultOrMap("other", (v) -> {
            consumed[0] = v;
            return "value";
        });
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(), res.anyProblems());
        assertSame(src.value, consumed[0]);
        assertEquals("value", res.result());
    }

    @Test
    void defaultOrMap_nullValue() {
        // Default method testing.

        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        src.value = null;
        RetVal<String> res = src.defaultOrMap("other", (v) -> {
            throw new RuntimeException("Should never be called");
        });
        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(), res.anyProblems());
        assertEquals("other", res.result());
    }

    @Test
    void defaultOrMap_problems() {
        // Default method testing.

        final LocalizedProblem problem = LocalizedProblem.from("x");
        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        src.problems.add(problem);
        final RetVal<String> res = src.defaultOrMap("other", (v) -> {
            throw new RuntimeException("Should never be called");
        });

        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void nullOrMap_nonnullValue() {
        // Default method testing.

        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        src.value = new Object();
        final Object[] consumed = new Object[] { null };
        final RetNullable<String> res = src.nullOrMap((v) -> {
            consumed[0] = v;
            return "value";
        });

        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(), res.anyProblems());
        assertSame(src.value, consumed[0]);
        assertEquals("value", res.result());
    }

    @Test
    void nullOrMap_nonnullValueNullRet() {
        // Default method testing.

        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        src.value = new Object();
        final Object[] consumed = new Object[] { null };
        final RetNullable<String> res = src.nullOrMap((v) -> {
            consumed[0] = v;
            return null;
        });

        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(), res.anyProblems());
        assertSame(src.value, consumed[0]);
        assertNull(res.result());
    }

    @Test
    void nullOrMap_nullValue() {
        // Default method testing.

        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        src.value = null;
        final RetNullable<String> res = src.nullOrMap((v) -> {
            throw new RuntimeException("Should never be called");
        });

        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(), res.anyProblems());
        assertNull(res.result());
    }

    @Test
    void nullOrMap_problem() {
        // Default method testing.

        final LocalizedProblem problem = LocalizedProblem.from("x");
        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        src.problems.add(problem);
        final RetNullable<String> res = src.nullOrMap((v) -> {
            throw new RuntimeException("Should never be called");
        });

        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void nullOrNullable_nonnullValue() {
        // Default method testing.

        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        src.value = new Object();
        RetNullable<String> expected = RetNullable.ok("value");
        final Object[] consumed = new Object[] { null };
        final RetNullable<String> res = src.nullOrThenNullable((v) -> {
            consumed[0] = v;
            return expected;
        });

        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(), res.anyProblems());
        assertSame(src.value, consumed[0]);
        assertSame(expected, res);
    }

    @Test
    void nullOrNullable_nullValue() {
        // Default method testing.

        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        src.value = null;
        final RetNullable<String> res = src.nullOrThenNullable((v) -> {
            throw new RuntimeException("Should never be called");
        });

        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(), res.anyProblems());
        assertNull(res.result());
    }

    @Test
    void nullOrNullable_problem() {
        // Default method testing.

        final LocalizedProblem problem = LocalizedProblem.from("x");
        final TestableRetNullable<Object> src = new TestableRetNullable<>();
        src.problems.add(problem);
        final RetNullable<String> res = src.nullOrThenNullable((v) -> {
            throw new RuntimeException("Should never be called");
        });

        assertEquals(List.of(res), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), res.anyProblems());
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
