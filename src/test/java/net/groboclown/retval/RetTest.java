// Released under the MIT License.
package net.groboclown.retval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.groboclown.retval.monitor.MockProblemMonitor;
import net.groboclown.retval.problems.LocalizedProblem;
import net.groboclown.retval.problems.UnhandledExceptionProblem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * Tests for the {@link Ret} class.
 */
class RetTest {
    MockProblemMonitor monitor;

    @Test
    void joinProblemSets_empty() {
        assertSame(
                Ret.NO_PROBLEMS,
                Ret.joinProblemSets(List.of(), List.of())
        );
    }

    @Test
    void joinProblemSets_one() {
        final Problem p1 = LocalizedProblem.from("p1");
        assertEquals(
                List.of(p1),
                Ret.joinProblemSets(List.of(p1))
        );
        assertEquals(
                List.of(p1),
                Ret.joinProblemSets(List.of(), List.of(p1), List.of())
        );
    }

    @Test
    void joinProblemSets_multipleSame() {
        final Problem p1 = LocalizedProblem.from("p1");
        assertEquals(
                List.of(p1, p1),
                Ret.joinProblemSets(List.of(), List.of(p1), List.of(p1))
        );
    }

    @Test
    void joinProblemSets_multiple() {
        final Problem p1 = LocalizedProblem.from("p1");
        final Problem p2 = LocalizedProblem.from("p2");
        assertEquals(
                List.of(p1, p2),
                Ret.joinProblemSets(Arrays.asList(p1, null, p2))
        );
        assertEquals(
                List.of(p1, p2),
                Ret.joinProblemSets(List.of(), List.of(p1), List.of(p2))
        );
        // Ordering...
        assertEquals(
                List.of(p2, p1),
                Ret.joinProblemSets(List.of(p2), Arrays.asList(null, p1))
        );
    }

    @Test
    void joinProblemMessages_empty() {
        assertEquals(
                "",
                Ret.joinProblemMessages(";", List.of())
        );
    }

    @Test
    void joinProblemMessages_one() {
        assertEquals(
                "p1",
                Ret.joinProblemMessages(";", List.of(
                        LocalizedProblem.from("p1")
                ))
        );
    }

    @Test
    void joinProblemMessages_two() {
        assertEquals(
                "p1;p2",
                Ret.joinProblemMessages(";", List.of(
                        LocalizedProblem.from("p1"),
                        LocalizedProblem.from("p2")
                ))
        );
    }

    @Test
    void joinRetProblemSets_nullInContainer() {
        final LocalizedProblem problem = LocalizedProblem.from("p1");
        final TestableProblemContainer container
                = new TestableProblemContainer(null, problem, null);
        assertEquals(
                List.of(problem, problem),
                Ret.joinRetProblemSets(
                        Arrays.asList(container, null),
                        Arrays.asList(null, container)
                )
        );
    }

    @Test
    void closeWith_funcThreadDeath() {
        // the function throws a ThreadDeath.
        final ThreadDeath td = new ThreadDeath();
        try {
            Ret.closeWith(new TestableCloseable(), (x) -> {
                throw td;
            });
            fail("Did not throw ThreadDeath");
        } catch (final ThreadDeath e) {
            assertSame(td, e);
        }
        assertEquals(List.of(), this.monitor.getNeverObserved());
    }

    @Test
    void closeWith_ok_closeThreadDeath() {
        // the auto-close throws a ThreadDeath.
        final ThreadDeath td = new ThreadDeath();
        final TestableCloseable closer = new TestableCloseable(td);
        final RetVal<String> ret = RetVal.ok("x");
        try {
            Ret.closeWith(closer, (x) -> ret);
            fail("Did not throw ThreadDeath");
        } catch (final ThreadDeath e) {
            assertSame(td, e);
        }
        // Return value should never have been observed.
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(1, closer.closeCount);
    }

    @Test
    void closeWith_problem_closeThreadDeath() {
        // the auto-close throws a ThreadDeath.
        final ThreadDeath td = new ThreadDeath();
        final TestableCloseable closer = new TestableCloseable(td);
        final RetVal<String> ret = RetVal.fromProblem(LocalizedProblem.from("x"));
        try {
            Ret.closeWith(closer, (x) -> ret);
            fail("Did not throw ThreadDeath");
        } catch (final ThreadDeath e) {
            assertSame(td, e);
        }
        // Return value should never have been observed.
        assertEquals(List.of(ret), this.monitor.getNeverObserved());
        assertEquals(1, closer.closeCount);
    }

    @Test
    void closeWith_funcException_closeThreadDeath() {
        // the auto-close throws a ThreadDeath.
        final ThreadDeath td = new ThreadDeath();
        final TestableCloseable closer = new TestableCloseable(td);
        try {
            Ret.closeWith(closer, (x) -> {
                throw new Exception();
            });
            fail("Did not throw ThreadDeath");
        } catch (final ThreadDeath e) {
            assertSame(td, e);
        }
        assertEquals(List.of(), this.monitor.getNeverObserved());
        assertEquals(1, closer.closeCount);
    }

    @Test
    void closeWith_ok_closeException() {
        // the auto-close throws Exception.
        final Exception ex = new Exception();
        final TestableCloseable closer = new TestableCloseable(ex);
        final RetVal<String> ret = RetVal.ok("x");
        final RetVal<String> val = Ret.closeWith(closer, (x) -> ret);
        // closeWith should have passed the monitor state to the return value.
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        final List<Problem> problems = new ArrayList<>(val.anyProblems());
        assertEquals(1, problems.size());
        assertEquals(UnhandledExceptionProblem.class, problems.get(0).getClass());
        assertEquals(ex, ((UnhandledExceptionProblem) problems.get(0)).getSourceException());
        assertEquals(1, closer.closeCount);
    }

    @Test
    void closeWith_problem_closeException() {
        // the auto-close throws Exception.
        final Exception ex = new Exception();
        final TestableCloseable closer = new TestableCloseable(ex);
        final LocalizedProblem problem = LocalizedProblem.from("p1");
        final RetVal<String> ret = RetVal.fromProblem(problem);
        final RetVal<String> val = Ret.closeWith(closer, (x) -> ret);
        // closeWith should have passed the monitor state to the return value.
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        final List<Problem> problems = new ArrayList<>(val.anyProblems());
        assertEquals(2, problems.size());
        assertSame(problem, problems.get(0));
        assertEquals(UnhandledExceptionProblem.class, problems.get(1).getClass());
        assertEquals(ex, ((UnhandledExceptionProblem) problems.get(1)).getSourceException());
        assertEquals(1, closer.closeCount);
    }

    @Test
    void closeWith_funcException_closeException() {
        // the auto-close throws Exception.
        final Exception funcEx = new Exception();
        final Exception closeEx = new Exception();
        final TestableCloseable closer = new TestableCloseable(closeEx);
        final RetVal<Object> val = Ret.closeWith(closer, (x) -> {
            throw funcEx;
        });
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        final List<Problem> problems = new ArrayList<>(val.anyProblems());
        assertEquals(1, problems.size());
        assertEquals(UnhandledExceptionProblem.class, problems.get(0).getClass());
        assertEquals(funcEx, ((UnhandledExceptionProblem) problems.get(0)).getSourceException());
        assertArrayEquals(new Throwable[] { closeEx }, funcEx.getSuppressed());
        assertEquals(1, closer.closeCount);
    }

    @Test
    void closeWith_ok() {
        final TestableCloseable closer = new TestableCloseable();
        final List<AutoCloseable> args = new ArrayList<>();
        final RetVal<String> ret = RetVal.ok("x");
        final RetVal<String> val = Ret.closeWith(closer, (arg) -> {
            args.add(arg);
            return ret;
        });
        // closeWith should have passed the monitor state to the return value.
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        assertSame(ret, val);
        assertEquals(List.of(closer), args);
        assertEquals("x", ret.getValue());
        assertEquals(1, closer.closeCount);
    }

    @Test
    void closeWith_problem() {
        final TestableCloseable closer = new TestableCloseable();
        final LocalizedProblem problem = LocalizedProblem.from("p1");
        final RetVal<String> ret = RetVal.fromProblem(problem);
        final List<AutoCloseable> args = new ArrayList<>();
        final RetVal<String> val = Ret.closeWith(closer, (arg) -> {
            args.add(arg);
            return ret;
        });
        // closeWith should have passed the monitor state to the return value.
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        assertSame(ret, val);
        assertEquals(List.of(closer), args);
        assertEquals(List.of(problem), ret.anyProblems());
        assertEquals(1, closer.closeCount);
    }

    @Test
    void closeWith_funcException() {
        final TestableCloseable closer = new TestableCloseable();
        final Exception funcEx = new Exception();
        final List<AutoCloseable> args = new ArrayList<>();
        final RetVal<Object> val = Ret.closeWith(closer, (arg) -> {
            args.add(arg);
            throw funcEx;
        });
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        final List<Problem> problems = new ArrayList<>(val.anyProblems());
        assertEquals(1, problems.size());
        assertEquals(UnhandledExceptionProblem.class, problems.get(0).getClass());
        assertEquals(funcEx, ((UnhandledExceptionProblem) problems.get(0)).getSourceException());
        assertEquals(List.of(closer), args);
        assertEquals(1, closer.closeCount);
    }

    // closeWithNullable performs the same basic call as closeWith, so rather than
    // exhaustively testing it here, we'll do a simple check.  However, due to
    // observability, this could do with exhaustive checks.
    @Test
    void closeWithNullable_ok() {
        final TestableCloseable closer = new TestableCloseable();
        final List<AutoCloseable> args = new ArrayList<>();
        final RetNullable<String> res = RetNullable.ok("x");
        final RetNullable<String> val = Ret.closeWithNullable(closer, (arg) -> {
            args.add(closer);
            return res;
        });
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        assertSame(res, val);
        assertEquals(List.of(closer), args);
        assertEquals(1, closer.closeCount);
    }

    @Test
    void closeWithNullable_funcException() {
        final TestableCloseable closer = new TestableCloseable();
        final Exception ex = new Exception();
        final List<AutoCloseable> args = new ArrayList<>();
        final RetNullable<String> val = Ret.closeWithNullable(closer, (arg) -> {
            args.add(closer);
            throw ex;
        });
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        final List<Problem> problems = new ArrayList<>(val.anyProblems());
        assertEquals(1, problems.size());
        assertEquals(UnhandledExceptionProblem.class, problems.get(0).getClass());
        assertEquals(ex, ((UnhandledExceptionProblem) problems.get(0)).getSourceException());
        assertEquals(List.of(closer), args);
        assertEquals(List.of(closer), args);
        assertEquals(1, closer.closeCount);
    }

    // closeWithNullable performs the same basic call as closeWith, so rather than
    // exhaustively testing it here, we'll do a simple check.  However, due to
    // observability, this could do with exhaustive checks.
    @Test
    void closeWithVoid_ok() {
        final TestableCloseable closer = new TestableCloseable();
        final List<AutoCloseable> args = new ArrayList<>();
        final RetVoid res = RetVoid.ok();
        final RetVoid val = Ret.closeWithVoid(closer, (arg) -> {
            args.add(closer);
            return res;
        });
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        assertSame(res, val);
        assertEquals(List.of(closer), args);
        assertEquals(1, closer.closeCount);
    }

    @Test
    void closeWithVoid_funcException() {
        final TestableCloseable closer = new TestableCloseable();
        final Exception ex = new Exception();
        final List<AutoCloseable> args = new ArrayList<>();
        final RetVoid val = Ret.closeWithVoid(closer, (arg) -> {
            args.add(closer);
            throw ex;
        });
        assertEquals(List.of(val), this.monitor.getNeverObserved());
        final List<Problem> problems = new ArrayList<>(val.anyProblems());
        assertEquals(1, problems.size());
        assertEquals(UnhandledExceptionProblem.class, problems.get(0).getClass());
        assertEquals(ex, ((UnhandledExceptionProblem) problems.get(0)).getSourceException());
        assertEquals(List.of(closer), args);
        assertEquals(List.of(closer), args);
        assertEquals(1, closer.closeCount);
    }

    @Test
    void enforceNoProblems_problems() {
        try {
            Ret.enforceNoProblems(List.of(LocalizedProblem.from("x")));
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // don't inspect message
        }
    }

    @Test
    void enforceNoProblems_noProblems() {
        Ret.enforceNoProblems(List.of());
        // No exception is thrown.
    }

    @Test
    void enforceHasProblems_problems() {
        Ret.enforceHasProblems(List.of(LocalizedProblem.from("x")));
        // No exception is thrown.
    }

    @Test
    void enforceHasProblems_noProblems() {
        try {
            Ret.enforceHasProblems(List.of());
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // don't inspect message
        }
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


    static class TestableCloseable implements AutoCloseable {
        final Error err;
        final Exception ex;
        int closeCount = 0;

        TestableCloseable(@Nullable final Error err) {
            this.err = err;
            this.ex = null;
        }

        TestableCloseable(@Nullable final Exception ex) {
            this.err = null;
            this.ex = ex;
        }

        TestableCloseable() {
            this.err = null;
            this.ex = null;
        }

        @Override
        public void close() throws Exception {
            this.closeCount++;
            if (this.err != null) {
                throw this.err;
            }
            if (this.ex != null) {
                throw this.ex;
            }
        }
    }
}
