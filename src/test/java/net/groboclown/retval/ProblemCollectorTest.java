// Released under the MIT License.
package net.groboclown.retval;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.groboclown.retval.impl.MonitoredFactory;
import net.groboclown.retval.impl.RetGenerator;
import net.groboclown.retval.impl.ReturnTypeFactory;
import net.groboclown.retval.monitor.MockProblemMonitor;
import net.groboclown.retval.problems.LocalizedProblem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProblemCollectorTest {
    MockProblemMonitor monitor;

    @Test
    void from() {
        final ProblemCollector pc = ProblemCollector.from();
        assertEquals(List.of(), pc.anyProblems());
        assertTrue(pc.isOk());
        assertFalse(pc.isProblem());
        assertFalse(pc.hasProblems());
    }

    @Test
    void from_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ProblemCollector pc = ProblemCollector.from(problem);
        assertEquals(List.of(problem), pc.anyProblems());
        assertFalse(pc.isOk());
        assertTrue(pc.isProblem());
        assertTrue(pc.hasProblems());
    }

    @Test
    void from_collectionProblem_empty() {
        final ProblemCollector pc = ProblemCollector.from(new ArrayList<>());
        assertEquals(List.of(), pc.anyProblems());
        assertTrue(pc.isOk());
        assertFalse(pc.isProblem());
        assertFalse(pc.hasProblems());
    }

    @Test
    void from_collectionProblem_some1() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ProblemCollector pc = ProblemCollector.from(List.of(problem));
        assertEquals(List.of(problem), pc.anyProblems());
        assertFalse(pc.isOk());
        assertTrue(pc.isProblem());
        assertTrue(pc.hasProblems());
    }

    @Test
    void from_collectionProblem_some2() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ProblemCollector pc = ProblemCollector.from(List.of(), List.of(problem));
        assertEquals(List.of(problem), pc.anyProblems());
        assertFalse(pc.isOk());
        assertTrue(pc.isProblem());
        assertTrue(pc.hasProblems());
    }

    @Test
    void from_RetVoid() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ProblemCollector pc = ProblemCollector.from(
                RetVoid.fromProblem(problem), RetVoid.ok()
        );
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), pc.anyProblems());
        assertFalse(pc.isOk());
        assertTrue(pc.isProblem());
        assertTrue(pc.hasProblems());
    }

    @Test
    void from_RetVal_ok() {
        final List<String> calledWith = new ArrayList<>();
        final ProblemCollector pc = ProblemCollector.from(RetVal.ok("a"), calledWith::add);
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of("a"), calledWith);
        assertEquals(List.of(), pc.anyProblems());
        assertTrue(pc.isOk());
        assertFalse(pc.isProblem());
        assertFalse(pc.hasProblems());
    }

    @Test
    void from_RetVal_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ProblemCollector pc = ProblemCollector.from(RetVal.fromProblem(problem), (v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), pc.anyProblems());
        assertFalse(pc.isOk());
        assertTrue(pc.isProblem());
        assertTrue(pc.hasProblems());
    }

    @Test
    void from_RetNullable_ok() {
        final List<String> calledWith = new ArrayList<>();
        final ProblemCollector pc = ProblemCollector.from(RetNullable.ok("a"), calledWith::add);
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of("a"), calledWith);
        assertEquals(List.of(), pc.anyProblems());
        assertTrue(pc.isOk());
        assertFalse(pc.isProblem());
        assertFalse(pc.hasProblems());
    }

    @Test
    void from_RetNullable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ProblemCollector pc = ProblemCollector.from(RetNullable.fromProblem(problem), (v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), pc.anyProblems());
        assertFalse(pc.isOk());
        assertTrue(pc.isProblem());
        assertTrue(pc.hasProblems());
    }

    @Test
    void with_RetVal_ok() {
        final List<String> calledWith = new ArrayList<>();
        final ProblemCollector pc = ProblemCollector.from();
        final ProblemCollector val = pc.with(RetVal.ok("a"), calledWith::add);
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertSame(val, pc);
        assertEquals(List.of("a"), calledWith);
        assertEquals(List.of(), pc.anyProblems());
        assertTrue(pc.isOk());
        assertFalse(pc.isProblem());
        assertFalse(pc.hasProblems());
    }

    @Test
    void with_RetVal_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ProblemCollector pc = ProblemCollector.from();
        final ProblemCollector val = pc.with(RetVal.fromProblem(problem), (v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertSame(val, pc);
        assertEquals(List.of(problem), pc.anyProblems());
        assertFalse(pc.isOk());
        assertTrue(pc.isProblem());
        assertTrue(pc.hasProblems());
    }

    @Test
    void with_RetNullable_ok() {
        final List<String> calledWith = new ArrayList<>();
        final ProblemCollector pc = ProblemCollector.from();
        final ProblemCollector val = pc.with(RetNullable.ok("a"), calledWith::add);
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertSame(pc, val);
        assertEquals(List.of("a"), calledWith);
        assertEquals(List.of(), pc.anyProblems());
        assertTrue(pc.isOk());
        assertFalse(pc.isProblem());
        assertFalse(pc.hasProblems());
    }

    @Test
    void with_RetNullable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ProblemCollector pc = ProblemCollector.from();
        final ProblemCollector val = pc.with(RetNullable.fromProblem(problem), (v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertSame(pc, val);
        assertEquals(List.of(problem), pc.anyProblems());
        assertFalse(pc.isOk());
        assertTrue(pc.isProblem());
        assertTrue(pc.hasProblems());
    }

    @Test
    void with_RetVoid_ok() {
        final ProblemCollector pc = ProblemCollector.from();
        final ProblemCollector val = pc.with(RetVoid.ok());
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertSame(pc, val);
        assertEquals(List.of(), pc.anyProblems());
        assertTrue(pc.isOk());
        assertFalse(pc.isProblem());
        assertFalse(pc.hasProblems());
    }

    @Test
    void with_RetVoid_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ProblemCollector pc = ProblemCollector.from();
        final ProblemCollector val = pc.with(RetVoid.fromProblem(problem));
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertSame(pc, val);
        assertEquals(List.of(problem), pc.anyProblems());
        assertFalse(pc.isOk());
        assertTrue(pc.isProblem());
        assertTrue(pc.hasProblems());
    }

    @Test
    void with_WarningVal_noProblems() {
        final List<String> called = new ArrayList<>();
        final ProblemCollector pc = ProblemCollector.from();
        final ProblemCollector val = pc.with(WarningVal.from("x"), called::add);
        assertSame(pc, val);
        assertEquals(List.of("x"), called);
        assertEquals(List.of(), pc.anyProblems());
        assertTrue(pc.isOk());
        assertFalse(pc.isProblem());
        assertFalse(pc.hasProblems());
    }

    @Test
    void with_WarningVal_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final List<String> called = new ArrayList<>();
        final ProblemCollector pc = ProblemCollector.from();
        final ProblemCollector val = pc.with(
                WarningVal.from("x", RetVoid.fromProblem(problem)),
                called::add);
        assertSame(pc, val);
        assertEquals(List.of("x"), called);
        assertEquals(List.of(problem), pc.anyProblems());
        assertFalse(pc.isOk());
        assertTrue(pc.isProblem());
        assertTrue(pc.hasProblems());
    }

    @Test
    void withProblem() {
        final LocalizedProblem problem1 = LocalizedProblem.from("x");
        final LocalizedProblem problem2 = LocalizedProblem.from("x");
        final ProblemCollector pc = ProblemCollector.from();
        final ProblemCollector val = pc.withProblem(problem1, problem2);
        assertSame(val, pc);
        assertEquals(List.of(problem1, problem2), pc.anyProblems());
        assertFalse(pc.isOk());
        assertTrue(pc.isProblem());
        assertTrue(pc.hasProblems());
    }

    @Test
    void withProblems() {
        final LocalizedProblem problem1 = LocalizedProblem.from("x");
        final LocalizedProblem problem2 = LocalizedProblem.from("x");
        final ProblemCollector pc = ProblemCollector.from();
        final ProblemCollector val = pc.withProblems(List.of(problem1), Set.of(problem2));
        assertSame(val, pc);
        assertEquals(List.of(problem1, problem2), pc.anyProblems());
        assertFalse(pc.isOk());
        assertTrue(pc.isProblem());
        assertTrue(pc.hasProblems());
    }

    @Test
    void withAll_array() {
        final LocalizedProblem problem1 = LocalizedProblem.from("1");
        final LocalizedProblem problem2 = LocalizedProblem.from("2");
        final ProblemCollector pc = ProblemCollector.from().withAll(
                RetVoid.ok(),
                RetVoid.fromProblem(problem1, problem2)
        );
        assertEquals(List.of(problem1, problem2), pc.anyProblems());
    }

    @Test
    void withAll_iterable() {
        final LocalizedProblem problem1 = LocalizedProblem.from("1");
        final LocalizedProblem problem2 = LocalizedProblem.from("2");
        final ProblemCollector pc = ProblemCollector.from().withAll(
                List.of(RetVoid.ok(), RetVoid.fromProblem(problem1, problem2))
        );
        assertEquals(List.of(problem1, problem2), pc.anyProblems());
    }

    @Test
    void withValuedProblemContainer_ok() {
        final TestableValuedProblemContainer<String> container =
                new TestableValuedProblemContainer<>("t");
        final List<String> called = new ArrayList<>();
        final ProblemCollector pc = ProblemCollector.from();
        final ProblemCollector val = pc.withValuedProblemContainer(container, called::add);
        assertSame(pc, val);
        assertEquals(List.of("t"), called);
        assertEquals(List.of(), pc.anyProblems());
        assertTrue(pc.isOk());
        assertFalse(pc.isProblem());
        assertFalse(pc.hasProblems());
    }

    @Test
    void withValuedProblemContainer_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("1");
        final TestableValuedProblemContainer<String> container =
                new TestableValuedProblemContainer<String>(problem);
        final ProblemCollector pc = ProblemCollector.from();
        final ProblemCollector val = pc.withValuedProblemContainer(container, (v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertSame(val, pc);
        assertEquals(List.of(problem), pc.anyProblems());
        assertFalse(pc.isOk());
        assertTrue(pc.isProblem());
        assertTrue(pc.hasProblems());
    }

    @Test
    void withValuedProblemContainer_warning_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("1");
        final WarningVal<String> container = WarningVal.from("t", RetVal.fromProblem(problem));
        final ProblemCollector pc = ProblemCollector.from();
        final ProblemCollector val = pc.withValuedProblemContainer(container, (v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertSame(val, pc);
        assertEquals(List.of(problem), pc.anyProblems());
        assertFalse(pc.isOk());
        assertTrue(pc.isProblem());
        assertTrue(pc.hasProblems());
    }

    @Test
    void validateEach_ok_containers_ok() {
        final List<String> called = new ArrayList<>();
        final ProblemCollector pc = ProblemCollector.from();
        final List<ProblemContainer> retList = new ArrayList<>(List.of(
                ProblemCollector.from(),
                ProblemCollector.from()
        ));
        pc.validateEach(List.of("1", "2"), (val) -> {
            called.add(val);
            return retList.remove(0);
        });
        assertEquals(List.of(), pc.anyProblems());
        assertEquals(List.of("1", "2"), called);
        assertEquals(List.of(), retList);
    }

    @Test
    void validateEach_empty() {
        final ProblemCollector pc = ProblemCollector.from();
        pc.validateEach(List.of(), (val) -> {
            throw new IllegalStateException("unreachable");
        });
        assertEquals(List.of(), pc.anyProblems());
    }

    @Test
    void validateEach_ok_retvals_ok() {
        final List<String> called = new ArrayList<>();
        final ProblemCollector pc = ProblemCollector.from();
        final List<ProblemContainer> retList = new ArrayList<>(List.of(
                // Use monitored values, to ensure the monitoring is handled right.
                MonitoredFactory.INSTANCE.createValOk("x"),
                MonitoredFactory.INSTANCE.createValOk(1)
        ));
        pc.validateEach(List.of("1", "2"), (val) -> {
            called.add(val);
            return retList.remove(0);
        });
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(), pc.anyProblems());
        assertEquals(List.of("1", "2"), called);
        assertEquals(List.of(), retList);
    }

    @Test
    void validateEach_ok_retvals_problems() {
        final LocalizedProblem problem1 = LocalizedProblem.from("1");
        final LocalizedProblem problem2 = LocalizedProblem.from("2");
        final List<String> called = new ArrayList<>();
        final ProblemCollector pc = ProblemCollector.from();
        final List<ProblemContainer> retList = new ArrayList<>(List.of(
                // Use monitored values, to ensure the monitoring is handled right.
                MonitoredFactory.INSTANCE.createValFromProblems(List.of(problem1)),
                MonitoredFactory.INSTANCE.createValFromProblems(List.of(problem2))
        ));
        pc.validateEach(List.of("1", "2"), (val) -> {
            called.add(val);
            return retList.remove(0);
        });
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(problem1, problem2), pc.anyProblems());
        assertEquals(List.of("1", "2"), called);
        assertEquals(List.of(), retList);
    }

    @Test
    void validateEach_problems_retvals_ok() {
        final LocalizedProblem problem = LocalizedProblem.from("p");
        final List<String> called = new ArrayList<>();
        final ProblemCollector pc = ProblemCollector.from()
                .withProblem(problem);
        final List<ProblemContainer> retList = new ArrayList<>(List.of(
                // Use monitored values, to ensure the monitoring is handled right.
                MonitoredFactory.INSTANCE.createValOk("x"),
                MonitoredFactory.INSTANCE.createValOk(1)
        ));
        pc.validateEach(List.of("1", "2"), (val) -> {
            called.add(val);
            return retList.remove(0);
        });
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), pc.anyProblems());
        assertEquals(List.of("1", "2"), called);
        assertEquals(List.of(), retList);
    }

    @Test
    void validateEach_problem_retvals_problems() {
        final LocalizedProblem problem0 = LocalizedProblem.from("0");
        final LocalizedProblem problem1 = LocalizedProblem.from("1");
        final LocalizedProblem problem2 = LocalizedProblem.from("2");
        final List<String> called = new ArrayList<>();
        final ProblemCollector pc = ProblemCollector.from()
                .withProblem(problem0);
        final List<ProblemContainer> retList = new ArrayList<>(List.of(
                // Use monitored values, to ensure the monitoring is handled right.
                MonitoredFactory.INSTANCE.createValFromProblems(List.of(problem1)),
                MonitoredFactory.INSTANCE.createValFromProblems(List.of(problem2))
        ));
        pc.validateEach(List.of("1", "2"), (val) -> {
            called.add(val);
            return retList.remove(0);
        });
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(problem0, problem1, problem2), pc.anyProblems());
        assertEquals(List.of("1", "2"), called);
        assertEquals(List.of(), retList);
    }

    @Test
    void add() {
        final LocalizedProblem problem0 = LocalizedProblem.from("0");
        final LocalizedProblem problem1 = LocalizedProblem.from("1");
        final LocalizedProblem problem2 = LocalizedProblem.from("2");

        final ProblemCollector pc = ProblemCollector.from();
        pc.add(problem0);
        pc.add(problem1);
        pc.add(problem2);

        assertEquals(List.of(problem0, problem1, problem2), pc.anyProblems());
    }

    @Test
    void addAll_several() {
        final LocalizedProblem problem0 = LocalizedProblem.from("0");
        final LocalizedProblem problem1 = LocalizedProblem.from("1");
        final LocalizedProblem problem2 = LocalizedProblem.from("2");

        final ProblemCollector pc = ProblemCollector.from();
        pc.addAll(List.of(problem0, problem1, problem2));

        assertEquals(List.of(problem0, problem1, problem2), pc.anyProblems());
    }

    @Test
    void addAll_empty() {
        final ProblemCollector pc = ProblemCollector.from();

        pc.addAll(List.of());

        assertEquals(List.of(), pc.anyProblems());
    }

    @Test
    void then_ok() {
        final RetVal<String> res = RetVal.ok("x");
        final ProblemCollector pc = ProblemCollector.from();
        final RetVal<String> ret = pc.then(() -> res);
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertSame(ret, res);
    }

    @Test
    void then_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ProblemCollector pc = ProblemCollector.from(problem);
        final RetVal<String> ret = pc.then(() -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), ret.anyProblems());
    }

    @Test
    void thenValue_ok() {
        final int[] callCount = {0};
        final RetVal<String> ret = ProblemCollector.from().thenValue(() -> {
            callCount[0]++;
            return "x";
        });
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(1, callCount[0]);
        assertEquals(List.of(), ret.anyProblems());
        assertEquals("x", ret.getValue());
    }

    @Test
    void thenValue_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVal<String> ret = ProblemCollector.from(problem).thenValue(() -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), ret.anyProblems());
    }

    @Test
    void thenNullable_ok() {
        final RetNullable<String> res = RetNullable.ok("x");
        final ProblemCollector pc = ProblemCollector.from();
        final RetNullable<String> ret = pc.thenNullable(() -> res);
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertSame(ret, res);
    }

    @Test
    void thenNullable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final ProblemCollector pc = ProblemCollector.from(problem);
        final RetNullable<String> ret = pc.thenNullable(() -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), ret.anyProblems());
    }

    @Test
    void thenNullableValue_ok() {
        final int[] callCount = {0};
        final RetNullable<String> ret = ProblemCollector.from().thenNullableValue(() -> {
            callCount[0]++;
            return "x";
        });
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(1, callCount[0]);
        assertEquals(List.of(), ret.anyProblems());
        assertEquals("x", ret.getValue());
    }

    @Test
    void thenNullableValue_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetNullable<String> ret = ProblemCollector
            .from(problem)
            .thenNullableValue(() -> {
                throw new IllegalStateException("should not be called");
            });
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), ret.anyProblems());
    }

    @Test
    void thenRun_ok() {
        final int[] callCount = {0};
        final RetVoid ret = ProblemCollector.from().thenRun(() -> callCount[0]++);
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(1, callCount[0]);
        assertEquals(List.of(), ret.anyProblems());
        assertTrue(ret.isOk());
    }

    @Test
    void thenRun_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVoid ret = ProblemCollector.from(problem).thenRun(() -> {
            throw new IllegalStateException("should not be called");
        });
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), ret.anyProblems());
    }

    @Test
    void complete_ok() {
        final RetVal<String> ret = ProblemCollector.from().complete("x");
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(List.of(), ret.anyProblems());
        assertEquals("x", ret.result());
    }

    @Test
    void complete_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVal<String> ret = ProblemCollector.from(problem).complete("x");
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), ret.anyProblems());
    }

    @Test
    void completeNullable_ok() {
        final RetNullable<String> ret = ProblemCollector.from().completeNullable("x");
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(List.of(), ret.anyProblems());
        assertEquals("x", ret.result());
    }

    @Test
    void completeNullable_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetNullable<String> ret = ProblemCollector.from(problem).completeNullable("x");
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), ret.anyProblems());
    }

    @Test
    void completeVoid_ok() {
        final RetVoid ret = ProblemCollector.from().completeVoid();
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(List.of(), ret.anyProblems());
    }

    @Test
    void completeVoid_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVoid ret = ProblemCollector.from(problem).completeVoid();
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), ret.anyProblems());
    }

    @Test
    void asWarning_ok() {
        final WarningVal<Long> warning = ProblemCollector.from().asWarning(6L);
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(), warning.anyProblems());
        assertEquals(6L, warning.getValue());
    }

    @Test
    void asWarning_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("!");
        final WarningVal<Long> warning = ProblemCollector.from()
                .withProblem(problem)
                .asWarning(6L);
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(List.of(problem), warning.anyProblems());
        assertEquals(6L, warning.getValue());
    }

    @Test
    void problem_state() {
        final LocalizedProblem problem0 = LocalizedProblem.from("0");
        final LocalizedProblem problem1 = LocalizedProblem.from("1");

        final ProblemCollector pc = ProblemCollector.from();
        assertTrue(pc.isOk());
        assertFalse(pc.isProblem());
        assertFalse(pc.hasProblems());
        assertEquals(List.of(), pc.anyProblems());
        // validProblems with no problems causes exception, which is examined elsewhere.

        pc.add(problem0);
        assertFalse(pc.isOk());
        assertTrue(pc.isProblem());
        assertTrue(pc.hasProblems());
        assertEquals(List.of(problem0), pc.anyProblems());
        assertEquals(List.of(problem0), pc.validProblems());

        pc.add(problem1);
        assertFalse(pc.isOk());
        assertTrue(pc.isProblem());
        assertTrue(pc.hasProblems());
        assertEquals(List.of(problem0, problem1), pc.anyProblems());
        assertEquals(List.of(problem0, problem1), pc.validProblems());
    }

    @Test
    void validProblems_ok() {
        final ProblemCollector pc = ProblemCollector.from();
        try {
            pc.validProblems();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // No exception inspection
        }
    }

    @Test
    void debugProblems_ok() {
        final ProblemCollector pc = ProblemCollector.from();
        assertEquals("", pc.debugProblems(" - "));
    }

    @Test
    void debugProblems_problem() {
        final LocalizedProblem problem0 = LocalizedProblem.from("0");
        final LocalizedProblem problem1 = LocalizedProblem.from("1");
        final ProblemCollector pc = ProblemCollector.from()
                .withProblem(problem0, problem1);

        assertEquals("0 - 1", pc.debugProblems(" - "));
    }

    @Test
    void joinProblemsWith_ok() {
        final LocalizedProblem problem0 = LocalizedProblem.from("0");
        final ProblemCollector pc = ProblemCollector.from();
        final List<Problem> collected = new ArrayList<>();
        collected.add(problem0);

        pc.joinProblemsWith(collected);

        assertEquals(List.of(problem0), collected);
        assertEquals(List.of(), pc.anyProblems());
    }

    @Test
    void joinProblemsWith_problem() {
        final LocalizedProblem problem0 = LocalizedProblem.from("0");
        final LocalizedProblem problem1 = LocalizedProblem.from("1");
        final ProblemCollector pc = ProblemCollector.from()
                .withProblem(problem1);
        final List<Problem> collected = new ArrayList<>();
        collected.add(problem0);

        pc.joinProblemsWith(collected);

        assertEquals(List.of(problem0, problem1), collected);
        assertEquals(List.of(problem1), pc.anyProblems());
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