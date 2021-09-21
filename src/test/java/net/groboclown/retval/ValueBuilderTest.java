// Released under the MIT License.
package net.groboclown.retval;

import java.util.ArrayList;
import java.util.List;
import net.groboclown.retval.problems.LocalizedProblem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValueBuilderTest {

    @Test
    void from_null() {
        try {
            ValueBuilder.from(null);
            fail("Did not throw NPE");
        } catch (final NullPointerException e) {
            // don't inspect exception
        }
    }

    @Test
    void from_nonNull() {
        final ValueBuilder<String> builder = ValueBuilder.from("x");
        assertEquals("x", builder.getValue());
        assertEquals(List.of(), builder.anyProblems());
        assertEquals(List.of(), builder.getCollector().anyProblems());
    }

    @Test
    void getValue() {
        final Object value = new Object();
        assertSame(value, ValueBuilder.from(value).getValue());
        assertEquals("x", ValueBuilder.from("x").getValue());
    }

    @Test
    void then_ok() {
        final Object value = new Object();
        final RetVal<Object> res = ValueBuilder.from(value).then();
        assertTrue(res.isOk());
        assertSame(value, res.getValue());
        assertEquals(List.of(), res.anyProblems());
    }

    @Test
    void then_problems() {
        final LocalizedProblem problem = LocalizedProblem.from("p1");
        final RetVal<Object> res = ValueBuilder
                .from(new Object())
                .with(RetVoid.fromProblem(problem))
                .then();
        assertTrue(res.hasProblems());
        assertNull(res.getValue());
        assertEquals(List.of(problem), res.anyProblems());
    }

    @Test
    void asWarning() {
        final Object value = new Object();
        final LocalizedProblem problem = LocalizedProblem.from("p1");
        final WarningVal<Object> warning = ValueBuilder
                .from(value)
                .with(RetVoid.fromProblem(problem))
                .asWarning();
        assertTrue(warning.hasProblems());
        assertEquals(value, warning.getValue());
        assertEquals(List.of(problem), warning.anyProblems());
    }

    @Test
    void with_RetVal_ok() {
        final Object value1 = new Object();
        final Object value2 = new Object();
        final List<Object> arg1Values = new ArrayList<>();
        final List<Object> arg2Values = new ArrayList<>();
        final ValueBuilder<Object> builder = ValueBuilder.from(value1);
        final ValueBuilder<Object> res = builder.with(RetVal.ok(value2), (a, b) -> {
            arg1Values.add(a);
            arg2Values.add(b);
        });
        assertSame(res, builder);
        assertEquals(List.of(value1), arg1Values);
        assertEquals(List.of(value2), arg2Values);
        assertEquals(List.of(), res.anyProblems());
        assertSame(value1, res.getValue());
    }

    @Test
    void with_RetVal_problem() {
        final Object value1 = new Object();
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ValueBuilder<Object> builder = ValueBuilder.from(value1);
        final ValueBuilder<Object> res = builder.with(RetVal.fromProblem(problem), (a, b) -> {
            throw new IllegalStateException("should not call");
        });
        assertSame(res, builder);
        assertEquals(List.of(problem), res.anyProblems());
        assertSame(value1, res.getValue());
    }

    @Test
    void with_RetNullable_ok() {
        final Object value1 = new Object();
        final Object value2 = new Object();
        final List<Object> arg1Values = new ArrayList<>();
        final List<Object> arg2Values = new ArrayList<>();
        final ValueBuilder<Object> builder = ValueBuilder.from(value1);
        final ValueBuilder<Object> res = builder.with(RetNullable.ok(value2), (a, b) -> {
            arg1Values.add(a);
            arg2Values.add(b);
        });
        assertSame(res, builder);
        assertEquals(List.of(value1), arg1Values);
        assertEquals(List.of(value2), arg2Values);
        assertEquals(List.of(), res.anyProblems());
        assertSame(value1, res.getValue());
    }

    @Test
    void with_RetNullable_problem() {
        final Object value1 = new Object();
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ValueBuilder<Object> builder = ValueBuilder.from(value1);
        final ValueBuilder<Object> res = builder.with(RetNullable.fromProblem(problem), (a, b) -> {
            throw new IllegalStateException("should not call");
        });
        assertSame(res, builder);
        assertEquals(List.of(problem), res.anyProblems());
        assertSame(value1, res.getValue());
    }

    @Test
    void with_RetVoid_ok() {
        final Object value1 = new Object();
        final ValueBuilder<Object> builder = ValueBuilder.from(value1);
        final ValueBuilder<Object> res = builder.with(RetVoid.ok());
        assertSame(res, builder);
        assertEquals(List.of(), res.anyProblems());
        assertSame(value1, res.getValue());
    }

    @Test
    void with_RetVoid_problem() {
        final Object value1 = new Object();
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ValueBuilder<Object> builder = ValueBuilder.from(value1);
        final ValueBuilder<Object> res = builder.with(RetVoid.fromProblem(problem));
        assertSame(res, builder);
        assertEquals(List.of(problem), res.anyProblems());
        assertSame(value1, res.getValue());
    }

    @Test
    void withValue() {
        final Object value1 = new Object();
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final List<Object> arg1Values = new ArrayList<>();
        final ValueBuilder<Object> builder = ValueBuilder.from(value1)
                .with(RetVoid.fromProblem(problem));
        final ValueBuilder<Object> res = builder.withValue(arg1Values::add);
        assertSame(res, builder);
        assertEquals(List.of(value1), arg1Values);
        assertEquals(List.of(problem), res.anyProblems());
        assertSame(value1, res.getValue());
    }

    @Test
    void getCollector() {
        final LocalizedProblem problem1 = LocalizedProblem.from("p1");
        final LocalizedProblem problem2 = LocalizedProblem.from("p2");
        final ValueBuilder<Object> builder = ValueBuilder.from("x");
        final ProblemCollector collector = builder.getCollector();

        assertEquals(List.of(), builder.getCollector().anyProblems());
        assertEquals(List.of(), collector.anyProblems());

        builder.with(RetVoid.fromProblem(problem1));
        assertEquals(List.of(problem1), builder.getCollector().anyProblems());
        assertEquals(List.of(problem1), collector.anyProblems());

        builder.with(RetVoid.fromProblem(problem2));
        assertEquals(List.of(problem1, problem2), builder.getCollector().anyProblems());
        assertEquals(List.of(problem1, problem2), collector.anyProblems());
    }

    @Test
    void isProblem() {
        final LocalizedProblem problem1 = LocalizedProblem.from("p1");
        final LocalizedProblem problem2 = LocalizedProblem.from("p2");
        final ValueBuilder<Object> builder = ValueBuilder.from("x");

        assertFalse(builder.isProblem());
        builder.with(RetVoid.fromProblem(problem1));
        assertTrue(builder.isProblem());
        builder.with(RetVoid.fromProblem(problem2));
        assertTrue(builder.isProblem());
    }

    @Test
    void hasProblems() {
        final LocalizedProblem problem1 = LocalizedProblem.from("p1");
        final LocalizedProblem problem2 = LocalizedProblem.from("p2");
        final ValueBuilder<Object> builder = ValueBuilder.from("x");

        assertFalse(builder.hasProblems());
        builder.with(RetVoid.fromProblem(problem1));
        assertTrue(builder.hasProblems());
        builder.with(RetVoid.fromProblem(problem2));
        assertTrue(builder.hasProblems());
    }

    @Test
    void isOk() {
        final LocalizedProblem problem1 = LocalizedProblem.from("p1");
        final LocalizedProblem problem2 = LocalizedProblem.from("p2");
        final ValueBuilder<Object> builder = ValueBuilder.from("x");

        assertTrue(builder.isOk());
        builder.with(RetVoid.fromProblem(problem1));
        assertFalse(builder.isOk());
        builder.with(RetVoid.fromProblem(problem2));
        assertFalse(builder.isOk());
    }

    @Test
    void anyProblems() {
        final LocalizedProblem problem1 = LocalizedProblem.from("p1");
        final LocalizedProblem problem2 = LocalizedProblem.from("p2");
        final ValueBuilder<Object> builder = ValueBuilder.from("x");

        assertEquals(List.of(), builder.anyProblems());
        builder.with(RetVoid.fromProblem(problem1));
        assertEquals(List.of(problem1), builder.anyProblems());
        builder.with(RetVoid.fromProblem(problem2));
        assertEquals(List.of(problem1, problem2), builder.anyProblems());
    }

    @Test
    void validProblems_ok() {
        final ValueBuilder<Object> builder = ValueBuilder.from("x");
        try {
            builder.validProblems();
            fail("did not throw ISE");
        } catch (final IllegalStateException e) {
            // do not inspect the exception
        }
    }

    @Test
    void validProblems_problem() {
        final LocalizedProblem problem1 = LocalizedProblem.from("p1");
        final ValueBuilder<String> builder = ValueBuilder
                .from("x")
                .with(RetVoid.fromProblem(problem1));
        assertEquals(List.of(problem1), builder.validProblems());
    }

    @Test
    void debugProblems_ok() {
        final ValueBuilder<String> builder = ValueBuilder.from("x");
        assertEquals("", builder.debugProblems(";"));
    }

    @Test
    void debugProblems_problems() {
        final LocalizedProblem problem1 = LocalizedProblem.from("p1");
        final LocalizedProblem problem2 = LocalizedProblem.from("p2");
        final ValueBuilder<String> builder = ValueBuilder
                .from("x")
                .with(RetVoid.fromProblem(problem1, problem2));
        assertEquals("p1;p2", builder.debugProblems(";"));
    }

    @Test
    void joinProblemsWith() {
        final LocalizedProblem problem1 = LocalizedProblem.from("p1");
        final LocalizedProblem problem2 = LocalizedProblem.from("p2");
        final List<Problem> joined = new ArrayList<>();
        final ValueBuilder<String> builder = ValueBuilder
                .from("x")
                .with(RetVoid.fromProblem(problem1, problem2));
        builder.joinProblemsWith(joined);
        assertEquals(List.of(problem1, problem2), joined);
    }
}
