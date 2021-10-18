// Released under the MIT License.

package net.groboclown.retval.contract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import net.groboclown.retval.Problem;
import net.groboclown.retval.RetNullable;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.RetVoid;
import net.groboclown.retval.problems.LocalizedProblem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * Contract test for all {@link RetVoid} implementations.
 */
public abstract class RetVoidContract {
    @Nonnull
    protected abstract RetVoid createForVoid();

    @Nonnull
    protected abstract RetVoid createForVoidProblems(@Nonnull List<Problem> problems);


    @Test
    void isProblem_ok() {
        final RetVoid res = createForVoid();
        assertFalse(res.isProblem());
    }

    @Test
    void isProblem_problem() {
        final RetVoid res = createForVoidProblems(List.of(LocalizedProblem.from("x")));
        assertTrue(res.isProblem());
    }

    @Test
    void hasProblems_ok() {
        final RetVoid res = createForVoid();
        assertFalse(res.hasProblems());
    }

    @Test
    void hasProblems_problem() {
        final RetVoid res = createForVoidProblems(List.of(LocalizedProblem.from("x")));
        assertTrue(res.hasProblems());
    }

    @Test
    void isOk_ok() {
        final RetVoid res = createForVoid();
        assertTrue(res.isOk());
    }

    @Test
    void isOk_problem() {
        final RetVoid res = createForVoidProblems(List.of(LocalizedProblem.from("x")));
        assertFalse(res.isOk());
    }

    @Test
    void anyProblems_ok() {
        final RetVoid res = createForVoid();

        final Collection<Problem> problems = res.anyProblems();
        assertEquals(List.of(), problems);
        assertUnmodifiable(problems);
    }

    @Test
    void anyProblems_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVoid res = createForVoidProblems(List.of(problem));

        final Collection<Problem> problems = res.anyProblems();
        assertEquals(List.of(problem), problems);
        assertUnmodifiable(problems);
    }

    @Test
    void validProblems_ok() {
        final RetVoid res = createForVoid();
        try {
            res.validProblems();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // Don't inspect exception
        }
    }

    @Test
    void validProblems_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVoid res = createForVoidProblems(List.of(problem));

        final Collection<Problem> problems = res.validProblems();
        assertEquals(List.of(problem), problems);
        assertUnmodifiable(problems);
    }

    @Test
    void debugProblems_ok() {
        final RetVoid res = createForVoid();
        assertEquals(
                "",
                res.debugProblems(";")
        );
    }

    @Test
    void debugProblems_problem() {
        final RetVoid res = createForVoidProblems(List.of(LocalizedProblem.from("x")));
        assertEquals(
                "x",
                res.debugProblems(";")
        );
    }

    @Test
    void then_ok() {
        final RetVoid orig = createForVoid();
        final RetVal<String> expected = RetVal.ok("x");
        final RetVal<String> ret = orig.then(() -> expected);
        assertSame(expected, ret);
    }

    @Test
    void then_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVoid orig = createForVoidProblems(List.of(problem));
        final RetVal<Object> ret = orig.then(() -> {
            throw new IllegalStateException("should never be called");
        });
        assertSame(ret, orig);
    }

    @Test
    void map_ok() {
        final RetVoid orig = createForVoid();
        final RetVal<String> ret = orig.map(() -> "x");
        assertEquals("x", ret.getValue());
    }

    @Test
    void map_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVoid orig = createForVoidProblems(List.of(problem));
        final RetVal<Object> ret = orig.map(() -> {
            throw new IllegalStateException("should never be called");
        });
        assertEquals(List.of(problem), ret.anyProblems());
    }

    @Test
    void thenNullable_ok() {
        final RetVoid orig = createForVoid();
        final RetNullable<String> expected = RetNullable.ok("x");
        final RetNullable<String> ret = orig.thenNullable(() -> expected);
        assertSame(expected, ret);
    }

    @Test
    void thenNullable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVoid orig = createForVoidProblems(List.of(problem));
        final RetNullable<Object> ret = orig.thenNullable(() -> {
            throw new IllegalStateException("should never be called");
        });
        assertEquals(List.of(problem), ret.anyProblems());
    }

    @Test
    void mapNullable_ok() {
        final RetVoid orig = createForVoid();
        final RetNullable<String> ret = orig.mapNullable(() -> "x");
        assertEquals("x", ret.getValue());
    }

    @Test
    void mapNullable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVoid orig = createForVoidProblems(List.of(problem));
        final RetNullable<Object> ret = orig.mapNullable(() -> {
            throw new IllegalStateException("should never be called");
        });
        assertEquals(List.of(problem), ret.anyProblems());
    }

    @Test
    void thenVoid_ok() {
        final RetVoid orig = createForVoid();
        final RetVoid expected = createForVoidProblems(List.of(LocalizedProblem.from("x")));
        final RetVoid ret = orig.thenVoid(() -> expected);
        assertSame(expected, ret);
    }

    @Test
    void thenVoid_problem() {
        final RetVoid orig = createForVoidProblems(List.of(LocalizedProblem.from("x")));
        final RetVoid ret = orig.thenVoid(() -> {
            throw new IllegalStateException("should never be called");
        });
        // indeed, the values are the same.
        assertSame(orig, ret);
    }

    @Test
    void thenRun_ok() {
        final RetVoid orig = createForVoid();
        final int[] callCount = {0};
        final RetVoid ret = orig.thenRun(() -> {
            callCount[0]++;
        });
        assertSame(orig, ret);
        assertEquals(1, callCount[0]);
    }

    @Test
    void thenRun_problem() {
        final RetVoid orig = createForVoidProblems(List.of(LocalizedProblem.from("x")));
        final RetVoid ret = orig.thenRun(() -> {
            throw new IllegalStateException("should never be called");
        });
        // indeed, the values are the same.
        assertSame(orig, ret);
    }

    @Test
    void forwardProblems_ok() {
        try {
            createForVoid().forwardProblems();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // Skip exception introspection
        }
    }

    @Test
    void forwardProblems_problems() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVal<Object> res = createForVoidProblems(List.of(problem))
                .forwardProblems();
        assertEquals(
                List.of(problem),
                res.anyProblems()
        );
    }

    @Test
    void forwardNullableProblems_ok() {
        try {
            createForVoid().forwardNullableProblems();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // Skip exception introspection
        }
    }

    @Test
    void forwardNullableProblems_problems() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetNullable<Object> res = createForVoidProblems(List.of(problem))
                .forwardNullableProblems();
        assertEquals(
                List.of(problem),
                res.anyProblems()
        );
    }

    @Test
    void forwardVoidProblems_ok() {
        try {
            createForVoid().forwardVoidProblems();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // Skip exception introspection
        }
    }

    @Test
    void forwardVoidProblems_problems() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVoid res = createForVoidProblems(List.of(problem))
                .forwardVoidProblems();
        assertEquals(
                List.of(problem),
                res.anyProblems()
        );
    }

    @Test
    void joinProblemsWith_ok() {
        final RetVoid res = createForVoid();
        final List<Problem> joined = new ArrayList<>();
        res.joinProblemsWith(joined);
        assertEquals(List.of(), joined);
    }

    @Test
    void joinProblemsWith_1problem() {
        final LocalizedProblem problem = LocalizedProblem.from("p1");
        final RetVoid res = createForVoidProblems(List.of(problem));
        final List<Problem> joined = new ArrayList<>();
        res.joinProblemsWith(joined);
        assertEquals(List.of(problem), joined);
    }

    @Test
    void toString_ok() {
        final RetVoid res = createForVoid();
        assertEquals(
                "Ret(ok)",
                res.toString()
        );
    }

    @Test
    void toString_1problem() {
        final RetVoid res = createForVoidProblems(List.of(LocalizedProblem.from("p1")));
        assertEquals(
                "Ret(1 problems: p1)",
                res.toString()
        );
    }

    @Test
    void toString_2problems() {
        final RetVoid res = createForVoidProblems(List.of(
                LocalizedProblem.from("p1"), LocalizedProblem.from("p2")
        ));
        assertEquals(
                "Ret(2 problems: p1; p2)",
                res.toString()
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
