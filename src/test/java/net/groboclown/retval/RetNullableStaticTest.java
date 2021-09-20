// Released under the MIT License. 
package net.groboclown.retval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
