// Released under the MIT License. 
package net.groboclown.retval.contract;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.groboclown.retval.Problem;
import net.groboclown.retval.ProblemContainer;
import net.groboclown.retval.RetNullable;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.RetVoid;
import net.groboclown.retval.function.NonnullConsumer;
import net.groboclown.retval.function.NonnullFunction;
import net.groboclown.retval.function.NonnullReturnFunction;
import net.groboclown.retval.monitor.MockProblemMonitor;
import net.groboclown.retval.problems.LocalizedProblem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;


/**
 * Contract tests for the Return objects which are monitored.  There are strict rules around
 * when a value becomes observed.
 *
 * <p>The monitor observation rules are based upon usage patterns.  In general, values with
 * problems should only be marked as observed when the problems are fetched, whereas okay
 * values should only be marked as observed when a "isOk" or "hasProblem" call is made.
 *
 * <p>TODO to be *really* right, this needs to perform monitor contract inspection on
 *     values returned by the tested calls.
 */
public abstract class MonitoredContract {
    private MockProblemMonitor monitor;

    @Nonnull
    protected abstract <T> RetNullable<T> createForNullable(@Nullable T value);

    @Nonnull
    protected abstract <T> RetNullable<T> createForNullableProblems(
            @Nonnull List<Problem> problems);

    @Nonnull
    protected abstract <T> RetVal<T> createForVal(@Nullable T value);

    @Nonnull
    protected abstract <T> RetVal<T> createForValProblems(@Nonnull List<Problem> problems);

    @Nonnull
    protected abstract RetVoid createForVoid();

    @Nonnull
    protected abstract RetVoid createForVoidProblems(@Nonnull List<Problem> problems);

    // ----------------------------------------------------------------------
    // Object construction - upon construction, the value should be registered as
    // observable, but marked as not observed.

    @Test
    void nullable_create_problem() {
        final RetNullable<Object> res = createForNullableProblems(
                List.of(LocalizedProblem.from("x")));
        assertNeverObserved(res);
    }

    @Test
    void nullable_create_ok() {
        final RetNullable<Object> res = createForNullable(45);
        assertNeverObserved(res);
    }

    @Test
    void nullable_create_ok_null() {
        final RetNullable<Object> res = createForNullable(null);
        assertNeverObserved(res);
    }

    @Test
    void val_create_problem() {
        final RetVal<Object> res = createForValProblems(List.of(LocalizedProblem.from("a")));
        assertNeverObserved(res);
    }

    @Test
    void val_create_ok() {
        final RetVal<Object> res = createForVal(new Object());
        assertNeverObserved(res);
    }

    @Test
    void void_create_problem() {
        final RetVoid res = createForVoidProblems(List.of(LocalizedProblem.from("a")));
        assertNeverObserved(res);
    }

    @Test
    void void_create_ok() {
        final RetVoid res = createForVoid();
        assertNeverObserved(res);
    }

    // ----------------------------------------------------------------------
    // getValue() does not trigger an observation.

    @Test
    void nullable_getValue_problem() {
        final RetNullable<Object> res = createForNullableProblems(
                List.of(LocalizedProblem.from("x")));
        res.getValue();
        assertNeverObserved(res);
    }

    @Test
    void nullable_getValue_ok() {
        final RetNullable<String> res = createForNullable("a");
        res.getValue();
        assertNeverObserved(res);
    }

    @Test
    void val_getValue_problem() {
        final RetVal<Object> res = createForValProblems(
                List.of(LocalizedProblem.from("x")));
        res.getValue();
        assertNeverObserved(res);
    }

    @Test
    void val_getValue_ok() {
        final RetVal<String> res = createForVal("a");
        res.getValue();
        assertNeverObserved(res);
    }

    // ----------------------------------------------------------------------
    // asOptional does not trigger an observation.

    @Test
    void nullable_asOptional_problem() {
        final RetNullable<Object> res = createForNullableProblems(
                List.of(LocalizedProblem.from("x")));
        res.asOptional();
        assertNeverObserved(res);
    }

    @Test
    void nullable_asOptional_ok() {
        final RetNullable<String> res = createForNullable("a");
        res.asOptional();
        assertNeverObserved(res);
    }

    @Test
    void nullable_asOptional_ok_null() {
        final RetNullable<String> res = createForNullable(null);
        res.asOptional();
        assertEquals(List.of(res), this.monitor.getNeverObserved());
    }

    @Test
    void val_asOptional_problem() {
        final RetVal<Object> res = createForValProblems(List.of(LocalizedProblem.from("x")));
        res.asOptional();
        assertNeverObserved(res);
    }

    @Test
    void val_asOptional_ok() {
        final RetVal<Boolean> res = createForVal(true);
        res.asOptional();
        assertNeverObserved(res);
    }

    // ----------------------------------------------------------------------
    // requireOptional triggers an observation only for problem states.
    // Exceptions indicate a programmer error and should not be used for
    // ok/problem state; however, this exception should not then follow up
    // with a did-not-observe problem.

    @Test
    void nullable_requireOptional_problem() {
        final RetNullable<Object> res = createForNullableProblems(
                List.of(LocalizedProblem.from("x")));
        try {
            res.requireOptional();
        } catch (final IllegalStateException e) {
            // don't inspect the exception
        }
        assertNeverObserved();
    }

    @Test
    void nullable_requireOptional_ok() {
        final RetNullable<String> res = createForNullable("a");
        res.requireOptional();
        assertNeverObserved(res);
    }

    @Test
    void nullable_requireOptional_ok_null() {
        final RetNullable<String> res = createForNullable(null);
        res.requireOptional();
        assertNeverObserved(res);
    }

    @Test
    void val_requireOptional_problem() {
        final RetVal<Object> res = createForValProblems(List.of(LocalizedProblem.from("x")));
        try {
            res.requireOptional();
        } catch (final IllegalStateException e) {
            // don't inspect the exception
        }
        assertNeverObserved();
    }

    @Test
    void val_requireOptional_ok() {
        final RetVal<String> res = createForVal("a");
        final Optional<String> optional = res.requireOptional();
        assertNeverObserved(res);
    }


    // ----------------------------------------------------------------------
    // result, like requireOptional, triggers an observation only for problem states.
    // Exceptions indicate a programmer error and should not be used for
    // ok/problem state; however, this exception should not then follow up
    // with a did-not-observe problem.


    @Test
    void nullable_result_problem() {
        final RetNullable<Object> res = createForNullableProblems(
                List.of(LocalizedProblem.from("x")));
        try {
            res.result();
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
        // Ensure that, after being called, regardless of the ok/problem state, it is still
        // considered unobserved.
        assertNeverObserved();
    }

    @Test
    void nullable_result_ok_not_checked() {
        final RetNullable<Long> res = createForNullable(Long.MIN_VALUE);
        // Call and check result.  Note this is done without the "isOk" wrapper.
        res.result();
        // Ensure that, after being called, it is still considered unobserved.
        assertNeverObserved(res);
    }

    @Test
    void val_result_problem() {
        final RetVal<Object> res = createForValProblems(List.of(LocalizedProblem.from("x")));
        try {
            res.result();
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
        assertNeverObserved();
    }

    @Test
    void val_result_ok_not_checked() {
        final RetVal<Long> res = createForVal(Long.MAX_VALUE);
        // Call and check result.  Note this is done without the "isOk" wrapper.
        res.result();
        // Ensure that, after being called, it is still considered unobserved.
        assertNeverObserved(res);
    }


    @Test
    void nullable_result_default_problem() {
        final RetNullable<Object> res = createForNullableProblems(
                List.of(LocalizedProblem.from("x")));
        try {
            res.result("foo");
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
        // Ensure that, after being called, regardless of the ok/problem state, it is still
        // considered unobserved.
        assertNeverObserved();
    }

    @Test
    void nullable_result_default_ok_not_checked() {
        final RetNullable<Long> res = createForNullable(Long.MIN_VALUE);
        // Call and check result.  Note this is done without the "isOk" wrapper.
        res.result(10L);
        // Ensure that, after being called, it is still considered unobserved.
        assertNeverObserved(res);
    }

    @Test
    void nullable_result_default_null_not_checked() {
        final RetNullable<Object> res = createForNullable(null);
        // Call and check result.  Note this is done without the "isOk" wrapper.
        res.result(new Object());
        // Ensure that, after being called, it is still considered unobserved.
        assertNeverObserved(res);
    }

    // ----------------------------------------------------------------------
    // forwardProblems pushes the observation responsibility to the owner of
    // the returned value, and the original should be marked as observed.
    // In the situation where the state was wrong (no problems), it should still
    // be marked as observed, so that the programmer does not receive a double
    // error.

    @Test
    void nullable_forwardProblems_ok() {
        final RetNullable<String> res = createForNullable("a");
        try {
            res.forwardProblems();
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
        assertNeverObserved();
    }

    @Test
    void nullable_forwardProblems_problems() {
        final RetNullable<String> res = createForNullableProblems(
                List.of(LocalizedProblem.from("x")));

        final RetVal<Integer> forwarded = res.forwardProblems();
        // The original "res" is now observed
        assertNeverObserved(forwarded);
    }

    @Test
    void val_forwardProblems_ok() {
        final RetVal<String> res = createForVal("a");
        try {
            res.forwardProblems();
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
        assertNeverObserved();
    }

    @Test
    void val_forwardProblems_problems() {
        final RetVal<String> res = createForValProblems(
                List.of(LocalizedProblem.from("x")));

        final RetVal<Integer> forwarded = res.forwardProblems();
        // The original "res" is now observed
        assertNeverObserved(forwarded);
    }

    @Test
    void void_forwardProblems_ok() {
        final RetVoid res = createForVoid();
        try {
            res.forwardProblems();
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
        assertNeverObserved();
    }

    @Test
    void void_forwardProblems_problems() {
        final RetVoid res = createForVoidProblems(List.of(LocalizedProblem.from("x")));

        final RetVal<Integer> forwarded = res.forwardProblems();
        // The original "res" is now observed
        assertNeverObserved(forwarded);
    }


    // ----------------------------------------------------------------------
    // forwardNullableProblems has the same observation semantics as
    // forwardProblems.

    @Test
    void nullable_forwardNullableProblems_ok() {
        final RetNullable<String> res = createForNullable("a");
        try {
            res.forwardNullableProblems();
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
        assertNeverObserved();
    }

    @Test
    void nullable_forwardNullableProblems_problems() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetNullable<String> res = createForNullableProblems(List.of(problem));
        final RetNullable<Integer> forwarded = res.forwardNullableProblems();
        assertNeverObserved(forwarded);
    }

    @Test
    void val_forwardNullableProblems_ok() {
        final RetVal<String> res = createForVal("a");
        try {
            res.forwardNullableProblems();
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
        assertNeverObserved();
    }

    @Test
    void val_forwardNullableProblems_problems() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVal<String> res = createForValProblems(List.of(problem));
        final RetNullable<Integer> forwarded = res.forwardNullableProblems();
        assertNeverObserved(forwarded);
    }

    @Test
    void void_forwardNullableProblems_ok() {
        final RetVoid res = createForVoid();
        try {
            res.forwardNullableProblems();
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
        assertNeverObserved();
    }

    @Test
    void void_forwardNullableProblems_problems() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        final RetVoid res = createForVoidProblems(List.of(problem));
        final RetNullable<Integer> forwarded = res.forwardNullableProblems();
        assertNeverObserved(forwarded);
    }

    // ----------------------------------------------------------------------
    // forwardVoidProblems has the same observation semantics as
    // forwardProblems.

    @Test
    void nullable_forwardVoidProblems_ok() {
        final RetNullable<String> res = createForNullable("a");
        try {
            res.forwardVoidProblems();
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
        assertNeverObserved();
    }

    @Test
    void nullable_forwardVoidProblems_problems() {
        final RetNullable<String> res = createForNullableProblems(List.of(
                LocalizedProblem.from("x")));
        final RetVoid forwarded = res.forwardVoidProblems();
        assertNeverObserved(forwarded);

        // TODO ensure the void object has void style observed semantics
    }

    @Test
    void val_forwardVoidProblems_ok() {
        final RetVal<String> res = createForVal("a");
        try {
            res.forwardVoidProblems();
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
        assertNeverObserved();
    }

    @Test
    void val_forwardVoidProblems_problems() {
        final RetVal<String> res = createForValProblems(List.of(
                LocalizedProblem.from("t")));
        final RetVoid forwarded = res.forwardVoidProblems();
        assertNeverObserved(forwarded);

        // TODO ensure the void object has void style observed semantics
    }

    @Test
    void void_forwardVoidProblems_ok() {
        final RetVoid res = createForVoid();
        try {
            res.forwardVoidProblems();
        } catch (final IllegalStateException e) {
            // skip exception inspection.
        }
        assertNeverObserved();
    }

    @Test
    void void_forwardVoidProblems_problems() {
        final RetVoid res = createForVoidProblems(List.of(LocalizedProblem.from("x")));
        // Notice the implicit API usage check for altering the signature to an incompatible
        // type.
        final RetVoid forwarded = res.forwardVoidProblems();
        assertNeverObserved(forwarded);

        // TODO ensure the void object has void style observed semantics
    }

    // ----------------------------------------------------------------------
    // thenValidate does not perform an observation.  The call into the lambda
    // does not necessarily indicate that the problem state is correctly
    // recognized by the invoker.  Likewise, any problem in this object is not
    // inspected.  Problems returned by the validate call must be marked as
    // observed.

    @Test
    void nullable_thenValidate_initialProblem() {
        final RetNullable<String> res = createForNullableProblems(List.of(
                LocalizedProblem.from("x")));
        final RetNullable<String> val = res.thenValidate((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNeverObserved(val);
    }

    @Test
    void nullable_thenValidate_ok_ok() {
        final RetNullable<String> res = createForNullable("value");
        final RetNullable<String> val = res.thenValidate((v) -> null);
        assertNeverObserved(val);
    }

    @Test
    void nullable_thenValidate_ok_problem() {
        final RetNullable<String> res = createForNullable("value");
        final RetVoid problemRet = createForVoidProblems(List.of(LocalizedProblem.from("x")));
        final RetNullable<String> val = res.thenValidate((v) -> problemRet);

        // Generated problems must have semantics run across them to make the observation only
        // remain on the final returned value.
        assertNeverObserved(val);
    }

    @Test
    void val_thenValidate_initialProblem() {
        final RetVal<String> res = createForValProblems(List.of(
                LocalizedProblem.from("x")));
        final RetVal<String> val = res.thenValidate((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNeverObserved(val);
    }

    @Test
    void val_thenValidate_ok_ok() {
        final RetVal<String> res = createForVal("value");
        final RetVal<String> val = res.thenValidate((v) -> null);
        assertNeverObserved(val);
    }

    @Test
    void val_thenValidate_ok_problem() {
        final RetVal<String> res = createForVal("value");
        final RetVoid problemRet = createForVoidProblems(List.of(LocalizedProblem.from("x")));
        final RetVal<String> val = res.thenValidate((v) -> problemRet);

        // Generated problems must have semantics run across them to make the observation only
        // remain on the final returned value.
        assertNeverObserved(val);
    }

    // ----------------------------------------------------------------------
    // then counts as an observation for all values except for the final returned one.

    @Test
    void nullable_then_ok_ok() {
        final RetNullable<Integer> res = createForNullable(3);
        final RetVal<String> innerRes = createForVal("s");
        final RetVal<String> val = res.then((v) -> innerRes);
        assertNeverObserved(val);
    }

    @Test
    void nullable_then_ok_problem() {
        final RetNullable<Integer> res = createForNullable(3);
        final RetVal<Integer> innerRes = createForValProblems(List.of(LocalizedProblem.from("p")));
        final RetVal<Integer> val = res.then((v) -> innerRes);
        assertNeverObserved(val);
    }

    @Test
    void nullable_then_problem() {
        final RetNullable<Integer> res = createForNullableProblems(
                List.of(LocalizedProblem.from("p")));
        final RetVal<Integer> val = res.then((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNeverObserved(val);
    }

    @Test
    void val_then_ok_ok() {
        final RetVal<Integer> res = createForVal(3);
        final RetVal<String> innerRes = createForVal("s");
        final RetVal<String> val = res.then((v) -> innerRes);
        assertNeverObserved(val);
    }

    @Test
    void val_then_ok_problem() {
        final RetVal<Integer> res = createForVal(3);
        final RetVal<Integer> innerRes = createForValProblems(List.of(LocalizedProblem.from("p")));
        final RetVal<Integer> val = res.then((v) -> innerRes);
        assertNeverObserved(val);
    }

    @Test
    void val_then_problem() {
        final RetVal<Integer> res = createForValProblems(
                List.of(LocalizedProblem.from("p")));
        final RetVal<Integer> val = res.then((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNeverObserved(val);
    }

    @Test
    void void_then_ok_ok() {
        final RetVoid res = createForVoid();
        final RetVal<String> innerRes = createForVal("s");
        final RetVal<String> val = res.then(() -> innerRes);
        assertNeverObserved(val);
    }

    @Test
    void void_then_ok_problem() {
        final RetVoid res = createForVoid();
        final RetVal<Integer> innerRes = createForValProblems(List.of(LocalizedProblem.from("p")));
        final RetVal<Integer> val = res.then(() -> innerRes);
        assertNeverObserved(val);
    }

    @Test
    void void_then_problem() {
        final RetVoid res = createForVoidProblems(
                List.of(LocalizedProblem.from("p")));
        final RetVal<Integer> val = res.then(() -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNeverObserved(val);
    }

    // ----------------------------------------------------------------------
    // map passes the observation ball to the final returned value.

    @Test
    void nullable_map_ok() {
        final RetNullable<Integer> res = createForNullable(3);
        final RetVal<Integer> val = res.map((v) -> 2);
        assertNeverObserved(val);
    }

    @Test
    void nullable_map_problem() {
        final RetNullable<Integer> res = createForNullableProblems(
                List.of(LocalizedProblem.from("p")));
        final RetVal<Integer> val = res.map((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNeverObserved(val);
    }

    @Test
    void val_map_ok() {
        final RetVal<Integer> res = createForVal(3);
        final RetVal<Integer> val = res.map((v) -> 2);
        assertNeverObserved(val);
    }

    @Test
    void val_map_problem() {
        final RetVal<Integer> res = createForValProblems(
                List.of(LocalizedProblem.from("p")));
        final RetVal<Integer> val = res.map((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNeverObserved(val);
    }

    @Test
    void void_map_ok() {
        final RetVoid res = createForVoid();
        final RetVal<Integer> val = res.map(() -> 2);
        assertNeverObserved(val);
    }

    @Test
    void void_map_problem() {
        final RetVoid res = createForVoidProblems(
                List.of(LocalizedProblem.from("p")));
        final RetVal<Integer> val = res.map(() -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNeverObserved(val);
    }

    // ----------------------------------------------------------------------
    // thenNullable has the same observable semantics as "then"

    @Test
    void nullable_thenNullable_ok_ok() {
        final RetNullable<Integer> res = createForNullable(3);
        final RetNullable<String> innerRes = createForNullable("");
        final RetNullable<String> val = res.thenNullable((v) -> innerRes);
        assertNeverObserved(val);
    }

    @Test
    void nullable_thenNullable_ok_problem() {
        final RetNullable<Integer> res = createForNullable(3);
        final RetNullable<String> innerRes = createForNullableProblems(
                List.of(LocalizedProblem.from("p")));
        final RetNullable<String> val = res.thenNullable((v) -> innerRes);
        assertNeverObserved(val);
    }

    @Test
    void nullable_thenNullable_problem() {
        final RetNullable<Integer> res = createForNullableProblems(
                List.of(LocalizedProblem.from("p")));
        final RetNullable<Integer> val = res.thenNullable((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNeverObserved(val);
    }

    @Test
    void val_thenNullable_ok_ok() {
        final RetVal<Integer> res = createForVal(3);
        final RetNullable<String> innerRes = createForNullable("");
        final RetNullable<String> val = res.thenNullable((v) -> innerRes);
        assertNeverObserved(val);
    }

    @Test
    void val_thenNullable_ok_problem() {
        final RetVal<Integer> res = createForVal(3);
        final RetNullable<String> innerRes = createForNullableProblems(
                List.of(LocalizedProblem.from("p")));
        final RetNullable<String> val = res.thenNullable((v) -> innerRes);
        assertNeverObserved(val);
    }

    @Test
    void val_thenNullable_problem() {
        final RetVal<Integer> res = createForValProblems(
                List.of(LocalizedProblem.from("p")));
        final RetNullable<Integer> val = res.thenNullable((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNeverObserved(val);
    }

    @Test
    void void_thenNullable_ok_ok() {
        final RetVoid res = createForVoid();
        final RetNullable<String> innerRes = createForNullable("");
        final RetNullable<String> val = res.thenNullable(() -> innerRes);
        assertNeverObserved(val);
    }

    @Test
    void void_thenNullable_ok_problem() {
        final RetVoid res = createForVoid();
        final RetNullable<String> innerRes = createForNullableProblems(
                List.of(LocalizedProblem.from("p")));
        final RetNullable<String> val = res.thenNullable(() -> innerRes);
        assertNeverObserved(val);
    }

    @Test
    void void_thenNullable_problem() {
        final RetVoid res = createForVoidProblems(
                List.of(LocalizedProblem.from("p")));
        final RetNullable<Integer> val = res.thenNullable(() -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNeverObserved(val);
    }

    // ----------------------------------------------------------------------
    // mapNullable has the same observable semantics as "map"

    @Test
    void nullable_mapNullable_ok() {
        final RetNullable<Integer> res = createForNullable(3);
        final RetNullable<Integer> val = res.mapNullable((v) -> 2);
        assertNeverObserved(val);
    }

    @Test
    void nullable_mapNullable_problem() {
        final RetNullable<Integer> res = createForNullableProblems(
                List.of(LocalizedProblem.from("p")));
        final RetNullable<Integer> val = res.mapNullable((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNeverObserved(val);
    }

    @Test
    void val_mapNullable_ok() {
        final RetVal<Integer> res = createForVal(3);
        final RetNullable<Integer> val = res.mapNullable((v) -> 2);
        assertNeverObserved(val);
    }

    @Test
    void val_mapNullable_problem() {
        final RetVal<Integer> res = createForValProblems(
                List.of(LocalizedProblem.from("p")));
        final RetNullable<Integer> val = res.mapNullable((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNeverObserved(val);
    }

    @Test
    void void_mapNullable_ok() {
        final RetVoid res = createForVoid();
        final RetNullable<Integer> val = res.mapNullable(() -> 2);
        assertNeverObserved(val);
    }

    @Test
    void void_mapNullable_problem() {
        final RetVoid res = createForVoidProblems(
                List.of(LocalizedProblem.from("p")));
        final RetNullable<Integer> val = res.mapNullable(() -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNeverObserved(val);
    }

    // ----------------------------------------------------------------------
    // thenRun(Runnable) and thenRunNullable(Runnable) pass the observable
    // ball to the returned value.

    @Test
    void nullable_thenRunNullable_runnable_ok() {
        final RetNullable<String> res = createForNullable("x");
        final RetNullable<String> val = res.thenRunNullable(() -> {});
        assertNeverObserved(val);
    }

    @Test
    void nullable_thenRunNullable_runnable_problem() {
        final RetNullable<Integer> res = createForNullableProblems(
                List.of(LocalizedProblem.from("p")));
        final RetNullable<Integer> val = res.thenRunNullable(() -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNeverObserved(val);
    }

    @Test
    void val_thenRun_runnable_ok() {
        final RetVal<String> res = createForVal("x");
        final RetVal<String> val = res.thenRun(() -> {});
        assertNeverObserved(val);
    }

    @Test
    void val_thenRun_runnable_problem() {
        final RetVal<Integer> res = createForValProblems(
                List.of(LocalizedProblem.from("p")));
        final RetVal<Integer> val = res.thenRun(() -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNeverObserved(val);
    }

    @Test
    void void_thenRun_runnable_ok() {
        final RetVoid res = createForVoid();
        final RetVoid val = res.thenRun(() -> {});
        assertNeverObserved(val);
    }

    @Test
    void void_thenRun_runnable_problem() {
        final RetVoid res = createForVoidProblems(List.of(LocalizedProblem.from("p")));
        final RetVoid val = res.thenRun(() -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNeverObserved(val);
    }

    // ----------------------------------------------------------------------
    // thenRun(Consumer) and thenRunNullable(Consumer) pass the observable
    // ball to the returned value.

    @Test
    void nullable_thenRunNullable_consumer_ok() {
        final RetNullable<String> res = createForNullable("x");
        final RetNullable<String> val = res.thenRunNullable((v) -> {});
        assertNeverObserved(val);
    }

    @Test
    void nullable_thenRunNullable_consumer_problem() {
        final RetNullable<Integer> res = createForNullableProblems(
                List.of(LocalizedProblem.from("p")));
        final RetNullable<Integer> val = res.thenRunNullable((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNeverObserved(val);
    }

    @Test
    void val_thenRun_consumer_ok() {
        final RetVal<String> res = createForVal("x");
        final RetVal<String> val = res.thenRun((v) -> {});
        assertNeverObserved(val);
    }

    @Test
    void val_thenRun_consumer_problem() {
        final RetVal<Integer> res = createForValProblems(
                List.of(LocalizedProblem.from("p")));
        final RetVal<Integer> val = res.thenRun((v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNeverObserved(val);
    }

    // ----------------------------------------------------------------------
    // thenVoid passes the observable ball to the returned value.

    @Test
    void nullable_thenVoid_function_ok_ok() {
        final RetNullable<Integer> res = createForNullable(3);
        final RetVoid innerRet = createForVoid();
        final RetVoid val = res.thenVoid((v) -> innerRet);
        assertNeverObserved(val);
    }

    @Test
    void nullable_thenVoid_function_ok_problem() {
        final RetNullable<Integer> res = createForNullable(3);
        final RetVoid innerRet = createForVoidProblems(List.of(LocalizedProblem.from("p")));
        final RetVoid val = res.thenVoid((v) -> innerRet);
        assertNeverObserved(val);
    }

    @Test
    void nullable_thenVoid_function_problem() {
        final RetNullable<Integer> res = createForNullableProblems(
                List.of(LocalizedProblem.from("p")));
        final RetVoid val = res.thenVoid((NonnullReturnFunction<Integer, RetVoid>) (v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNeverObserved(val);
    }

    @Test
    void val_thenVoid_function_ok_ok() {
        final RetVal<Integer> res = createForVal(3);
        final RetVoid innerRet = createForVoid();
        final RetVoid val = res.thenVoid((v) -> innerRet);
        assertNeverObserved(val);
    }

    @Test
    void val_thenVoid_function_ok_problem() {
        final RetVal<Integer> res = createForVal(3);
        final RetVoid innerRet = createForVoidProblems(List.of(LocalizedProblem.from("p")));
        final RetVoid val = res.thenVoid((v) -> innerRet);
        assertNeverObserved(val);
    }

    @Test
    void val_thenVoid_function_problem() {
        final RetVal<Integer> res = createForValProblems(List.of(LocalizedProblem.from("p")));
        final RetVoid val = res.thenVoid((NonnullFunction<Integer, RetVoid>) (v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNeverObserved(val);
    }

    @Test
    void void_thenVoid_function_ok_ok() {
        final RetVoid res = createForVoid();
        final RetVoid innerRet = createForVoid();
        final RetVoid val = res.thenVoid(() -> innerRet);
        assertNeverObserved(val);
    }

    @Test
    void void_thenVoid_function_ok_problem() {
        final RetVoid res = createForVoid();
        final RetVoid innerRet = createForVoidProblems(List.of(LocalizedProblem.from("p")));
        final RetVoid val = res.thenVoid(() -> innerRet);
        assertNeverObserved(val);
    }

    @Test
    void void_thenVoid_function_problem() {
        final RetVoid res = createForVoidProblems(List.of(LocalizedProblem.from("p")));
        final RetVoid val = res.thenVoid(() -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNeverObserved(val);
    }

    // ----------------------------------------------------------------------
    // thenVoid(Consumer) also passes the observable ball to the final value.

    @Test
    void nullable_thenVoid_consumer_ok() {
        final RetNullable<Integer> res = createForNullable(3);
        final RetVoid val = res.thenVoid((v) -> {});
        assertNeverObserved(val);
    }

    @Test
    void nullable_thenVoid_consumer_problem() {
        final RetNullable<Integer> res = createForNullableProblems(
                List.of(LocalizedProblem.from("p")));
        final RetVoid val = res.thenVoid((Consumer<Integer>) (v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNeverObserved(val);
    }

    @Test
    void val_thenVoid_consumer_ok() {
        final RetVal<Integer> res = createForVal(3);
        final RetVoid val = res.thenVoid((v) -> {});
        assertNeverObserved(val);
    }

    @Test
    void val_thenVoid_consumer_problem() {
        final RetVal<Integer> res = createForValProblems(List.of(LocalizedProblem.from("p")));
        final RetVoid val = res.thenVoid((NonnullConsumer<Integer>) (v) -> {
            throw new IllegalStateException("unreachable code");
        });
        assertNeverObserved(val);
    }

    // ----------------------------------------------------------------------
    // consume and produceVoid are non-deprecated versions of the thenVoid call, to prevent a
    // required "return" to disambiguate between the producer and consumer.

    @Test
    void nullable_consume_ok() {
        final RetNullable<Character> res = createForNullable('1');
        res.consume((c) -> {});
        assertNeverObserved(res);
    }

    @Test
    void nullable_consume_ok_null() {
        final RetNullable<Character> res = createForNullable(null);
        res.consume((c) -> {});
        assertNeverObserved(res);
    }

    @Test
    void nullable_consume_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("t");
        final RetNullable<Character> res = createForNullableProblems(List.of(problem));
        res.consume((c) -> {});
        assertNeverObserved(res);
    }

    @Test
    void nullable_produceVoid_ok() {
        final RetNullable<Character> res = createForNullable('1');
        final RetVoid val = createForVoidProblems(List.of(LocalizedProblem.from("q")));
        res.produceVoid((c) -> val);
        assertNeverObserved(val);
    }

    @Test
    void nullable_produceVoid_ok_null() {
        final RetNullable<Character> res = createForNullable(null);
        final RetVoid val = createForVoidProblems(List.of(LocalizedProblem.from("q")));
        res.produceVoid((c) -> val);
        assertNeverObserved(val);
    }

    @Test
    void nullable_produceVoid_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("t");
        final RetNullable<Character> res = createForNullableProblems(List.of(problem));
        res.produceVoid((c) -> {
            throw new IllegalStateException("not reachable");
        });
        assertNeverObserved(res);
    }

    @Test
    void val_consume_ok() {
        final RetVal<Character> res = createForVal('1');
        res.consume((c) -> {});
        assertNeverObserved(res);
    }

    @Test
    void val_consume_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("t");
        final RetVal<Character> res = createForValProblems(List.of(problem));
        res.consume((c) -> {});
        assertNeverObserved(res);
    }

    @Test
    void val_produceVoid_ok() {
        final RetVal<Character> res = createForVal('1');
        final RetVoid val = createForVoidProblems(List.of(LocalizedProblem.from("q")));
        res.produceVoid((c) -> val);
        assertNeverObserved(val);
    }

    @Test
    void val_produceVoid_problem() {
        final LocalizedProblem problem = LocalizedProblem.from("t");
        final RetNullable<Character> res = createForNullableProblems(List.of(problem));
        res.produceVoid((c) -> {
            throw new IllegalStateException("not reachable");
        });
        assertNeverObserved(res);
    }

    @Test
    void nullable_requireNonNull_ok() {
        final RetNullable<String> res = createForNullable("c");
        res.requireNonNull(LocalizedProblem.from("t"));
        assertNeverObserved(res);
    }

    @Test
    void nullable_requireNonNull_null() {
        final RetNullable<String> src = createForNullable(null);
        final RetVal<String> res = src.requireNonNull(LocalizedProblem.from("t"));
        assertNeverObserved(res);
        assertNotSame(src, res);
    }

    @Test
    void nullable_requireNonNull_problem() {
        final RetNullable<String> res = createForNullableProblems(
                List.of(LocalizedProblem.from("a")));
        res.requireNonNull(LocalizedProblem.from("t"));
        assertNeverObserved(res);
    }

    @Test
    void nullable_defaultAs_ok() {
        final RetNullable<String> res = createForNullable("x");
        res.defaultAs("a");
        assertNeverObserved(res);
    }

    @Test
    void nullable_defaultAs_null() {
        final RetNullable<String> src = createForNullable(null);
        final RetVal<String> res = src.defaultAs("a");
        assertNeverObserved(res);
    }

    @Test
    void nullable_defaultAs_problem() {
        final RetNullable<String> res = createForNullableProblems(
                List.of(LocalizedProblem.from("a")));
        res.defaultAs("a");
        assertNeverObserved(res);
    }

    @Test
    void nullable_consumeIfNonnull_ok() {
        final RetNullable<String> res = createForNullable("x");
        res.consumeIfNonnull((v) -> {});
        assertNeverObserved(res);
    }

    @Test
    void nullable_consumeIfNonnull_null() {
        final RetNullable<String> res = createForNullable(null);
        res.consumeIfNonnull((v) -> {
            throw new IllegalStateException("should never be called");
        });
        assertNeverObserved(res);
    }

    @Test
    void nullable_consumeIfNonnull_problem() {
        final RetNullable<String> res = createForNullableProblems(
                List.of(LocalizedProblem.from("a")));
        res.consumeIfNonnull((v) -> {
            throw new IllegalStateException("should never be called");
        });
        assertNeverObserved(res);
    }

    @Test
    void nullable_defaultOrMap_ok() {
        final RetNullable<String> src = createForNullable("x");
        final RetVal<String> res = src.defaultOrMap("y", (v) -> "z");
        assertNeverObserved(res);
    }

    @Test
    void nullable_defaultOrMap_null() {
        final RetNullable<String> src = createForNullable(null);
        final RetVal<String> res = src.defaultOrMap("y", (v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertNeverObserved(res);
    }

    @Test
    void nullable_defaultOrMap_problem() {
        final RetNullable<String> src = createForNullableProblems(
                List.of(LocalizedProblem.from("a")));
        final RetVal<String> res = src.defaultOrMap("y", (v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertNeverObserved(res);
    }

    @Test
    void nullable_nullOrMap_ok() {
        final RetNullable<String> src = createForNullable("x");
        RetNullable<String> res = src.nullOrMap((v) -> v + "z");
        assertNeverObserved(res);
    }

    @Test
    void nullable_nullOrMap_null() {
        final RetNullable<String> src = createForNullable(null);
        RetNullable<RetVal<String>> res = src.nullOrMap((v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertNeverObserved(res);
    }

    @Test
    void nullable_nullOrMap_problem() {
        final RetNullable<String> src = createForNullableProblems(
                List.of(LocalizedProblem.from("a")));
        RetNullable<RetVal<String>> res = src.nullOrMap((v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertNeverObserved(res);
    }

    @Test
    void nullable_nullOrNullable_ok() {
        final RetNullable<String> src = createForNullable("x");
        RetNullable<String> res = src.nullOrThenNullable((v) -> RetNullable.ok(v + "z"));
        assertNeverObserved(res);
    }

    @Test
    void nullable_nullOrNullable_null() {
        final RetNullable<String> src = createForNullable(null);
        RetNullable<RetVal<String>> res = src.nullOrThenNullable((v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertNeverObserved(res);
    }

    @Test
    void nullable_nullOrNullable_problem() {
        final RetNullable<String> src = createForNullableProblems(
                List.of(LocalizedProblem.from("a")));
        RetNullable<RetVal<String>> res = src.nullOrThenNullable((v) -> {
            throw new IllegalStateException("should not be called");
        });
        assertNeverObserved(res);
    }


    // ----------------------------------------------------------------------
    // isOk only performs an observation if there are no problems.
    // Note that a common pattern is:
    //    res = something();
    //    allProblems.addAll(res.anyProblem());
    //    if (res.isOk()) { ... }
    // To fully and accurately cover this, the anyProblem + isOk would need
    // to work together, but that introduces state.
    // Additionally, we want the observation to happen on "isOk" in order to
    // better enforce the isOk check, so that production issues don't
    // unexpectedly lead to runtime errors when the monitoring during testing
    // could have helped catch this.

    @Test
    void nullable_isOk_ok() {
        final RetNullable<Long> res = createForNullable(Long.MAX_VALUE);
        res.isOk();
        assertNeverObserved();
    }

    @Test
    void nullable_isOk_problem() {
        final RetNullable<Long> res = createForNullableProblems(
                List.of(LocalizedProblem.from("f")));
        res.isOk();
        assertNeverObserved(res);
    }

    @Test
    void val_isOk_ok() {
        final RetVal<Long> res = createForVal(Long.MAX_VALUE);
        res.isOk();
        assertNeverObserved();
    }

    @Test
    void val_isOk_problem() {
        final RetVal<Long> res = createForValProblems(List.of(LocalizedProblem.from("f")));
        res.isOk();
        assertNeverObserved(res);
    }

    @Test
    void void_isOk_ok() {
        final RetVoid res = createForVoid();
        res.isOk();
        assertNeverObserved();
    }

    @Test
    void void_isOk_problem() {
        final RetVoid res = createForVoidProblems(List.of(LocalizedProblem.from("f")));
        res.isOk();
        assertNeverObserved(res);
    }

    // ----------------------------------------------------------------------
    // isProblem only performs an observation if there are no problems,
    // similar to isOk

    @Test
    void nullable_isProblem_ok() {
        final RetNullable<Long> res = createForNullable(Long.MAX_VALUE);
        res.isProblem();
        assertNeverObserved();
    }

    @Test
    void nullable_isProblem_problem() {
        final RetNullable<Long> res = createForNullableProblems(
                List.of(LocalizedProblem.from("f")));
        res.isProblem();
        assertNeverObserved(res);
    }

    @Test
    void val_isProblem_ok() {
        final RetVal<Long> res = createForVal(Long.MAX_VALUE);
        res.isProblem();
        assertNeverObserved();
    }

    @Test
    void val_isProblem_problem() {
        final RetVal<Long> res = createForValProblems(List.of(LocalizedProblem.from("f")));
        res.isProblem();
        assertNeverObserved(res);
    }

    @Test
    void void_isProblem_ok() {
        final RetVoid res = createForVoid();
        res.isProblem();
        assertNeverObserved();
    }

    @Test
    void void_isProblem_problem() {
        final RetVoid res = createForVoidProblems(List.of(LocalizedProblem.from("f")));
        res.isProblem();
        assertNeverObserved(res);
    }

    // ----------------------------------------------------------------------
    // hasProblems is identical in usage to isProblem.

    @Test
    void nullable_hasProblems_ok() {
        final RetNullable<Long> res = createForNullable(Long.MAX_VALUE);
        res.hasProblems();
        assertNeverObserved();
    }

    @Test
    void nullable_hasProblems_problem() {
        final RetNullable<Long> res = createForNullableProblems(
                List.of(LocalizedProblem.from("f")));
        res.hasProblems();
        assertNeverObserved(res);
    }

    @Test
    void val_hasProblems_ok() {
        final RetVal<Long> res = createForVal(Long.MAX_VALUE);
        res.hasProblems();
        assertNeverObserved();
    }

    @Test
    void val_hasProblems_problem() {
        final RetVal<Long> res = createForValProblems(List.of(LocalizedProblem.from("f")));
        res.hasProblems();
        assertNeverObserved(res);
    }

    @Test
    void void_hasProblems_ok() {
        final RetVoid res = createForVoid();
        res.hasProblems();
        assertNeverObserved();
    }

    @Test
    void void_hasProblems_problem() {
        final RetVoid res = createForVoidProblems(List.of(LocalizedProblem.from("f")));
        res.hasProblems();
        assertNeverObserved(res);
    }

    // ----------------------------------------------------------------------
    // anyProblems and isOk work together for observability.
    // anyProblems() marks an observation if there are problems.

    @Test
    void nullable_anyProblems_ok() {
        final RetNullable<Long> res = createForNullable(Long.MAX_VALUE);
        res.anyProblems();
        assertNeverObserved(res);
    }

    @Test
    void nullable_anyProblems_problem() {
        final RetNullable<Long> res = createForNullableProblems(
                List.of(LocalizedProblem.from("f")));
        res.anyProblems();
        assertNeverObserved();
    }

    @Test
    void val_anyProblems_ok() {
        final RetVal<Long> res = createForVal(Long.MAX_VALUE);
        res.anyProblems();
        assertNeverObserved(res);
    }

    @Test
    void val_anyProblems_problem() {
        final RetVal<Long> res = createForValProblems(List.of(LocalizedProblem.from("f")));
        res.anyProblems();
        assertNeverObserved();
    }

    @Test
    void void_anyProblems_ok() {
        final RetVoid res = createForVoid();
        res.anyProblems();
        assertNeverObserved(res);
    }

    @Test
    void void_anyProblems_problem() {
        final RetVoid res = createForVoidProblems(List.of(LocalizedProblem.from("f")));
        res.anyProblems();
        assertNeverObserved();
    }

    // ----------------------------------------------------------------------
    // validProblems will always mark the value as observed.  In the case
    // where no problems exist, a programmer exception is raised, and forcing
    // the observation prevents a double error.

    @Test
    void nullable_validProblems_ok() {
        final RetNullable<Long> res = createForNullable(Long.MAX_VALUE);
        try {
            res.validProblems();
        } catch (final IllegalStateException e) {
            // skip check
        }
        assertNeverObserved();
    }

    @Test
    void nullable_validProblems_problem() {
        final RetNullable<Long> res = createForNullableProblems(
                List.of(LocalizedProblem.from("f")));
        res.validProblems();
        assertNeverObserved();
    }

    @Test
    void val_validProblems_ok() {
        final RetVal<Long> res = createForVal(Long.MAX_VALUE);
        try {
            res.validProblems();
        } catch (final IllegalStateException e) {
            // skip check
        }
        assertNeverObserved();
    }

    @Test
    void val_validProblems_problem() {
        final RetVal<Long> res = createForValProblems(List.of(LocalizedProblem.from("f")));
        try {
            res.validProblems();
        } catch (final IllegalStateException e) {
            // skip check
        }
        assertNeverObserved();
    }

    @Test
    void void_validProblems_ok() {
        final RetVoid res = createForVoid();
        try {
            res.validProblems();
        } catch (final IllegalStateException e) {
            // skip check
        }
        assertNeverObserved();
    }

    @Test
    void void_validProblems_problem() {
        final RetVoid res = createForVoidProblems(List.of(LocalizedProblem.from("f")));
        res.validProblems();
        assertNeverObserved();
    }

    // ----------------------------------------------------------------------
    // joinProblemsWith is considered an observation, mostly for use with
    // the builder pattern classes.

    @Test
    void nullable_joinProblemsWith_ok() {
        final RetNullable<Long> res = createForNullable(Long.MAX_VALUE);
        res.joinProblemsWith(new ArrayList<>());
        assertNeverObserved();
    }

    @Test
    void nullable_joinProblemsWith_problem() {
        final RetNullable<Long> res = createForNullableProblems(
                List.of(LocalizedProblem.from("f")));
        res.joinProblemsWith(new ArrayList<>());
        assertNeverObserved();
    }

    @Test
    void val_joinProblemsWith_ok() {
        final RetVal<Long> res = createForVal(Long.MAX_VALUE);
        res.joinProblemsWith(new ArrayList<>());
        assertNeverObserved();
    }

    @Test
    void val_joinProblemsWith_problem() {
        final RetVal<Long> res = createForValProblems(
                List.of(LocalizedProblem.from("f")));
        res.joinProblemsWith(new ArrayList<>());
        assertNeverObserved();
    }

    @Test
    void void_joinProblemsWith_ok() {
        final RetVoid res = createForVoid();
        res.joinProblemsWith(new ArrayList<>());
        assertNeverObserved();
    }

    @Test
    void void_joinProblemsWith_problem() {
        final RetVoid res = createForVoidProblems(List.of(LocalizedProblem.from("f")));
        res.joinProblemsWith(new ArrayList<>());
        assertNeverObserved();
    }

    // ----------------------------------------------------------------------
    // Debug must never be considered an observation.

    @Test
    void nullable_debugProblems_empty() {
        final RetNullable<String> res = createForNullable("x");
        res.debugProblems(";");
        assertNeverObserved(res);
    }

    @Test
    void nullable_debugProblems_problem() {
        final RetNullable<String> res = createForNullableProblems(List.of(
                LocalizedProblem.from("a")));
        res.debugProblems(";");
        assertNeverObserved(res);
    }

    @Test
    void val_debugProblems_empty() {
        final RetVal<String> res = createForVal("x");
        res.debugProblems(";");
        assertNeverObserved(res);
    }

    @Test
    void val_debugProblems_problem() {
        final RetVal<String> res = createForValProblems(List.of(LocalizedProblem.from("a")));
        res.debugProblems(";");
        assertNeverObserved(res);
    }

    @Test
    void void_debugProblems_empty() {
        final RetVoid res = createForVoid();
        res.debugProblems(";");
        assertNeverObserved(res);
    }

    @Test
    void void_debugProblems_problem() {
        final RetVoid res = createForVoidProblems(List.of(LocalizedProblem.from("a")));
        res.debugProblems(";");
        assertNeverObserved(res);
    }

    // ----------------------------------------------------------------------
    // toString must never count as an observation.

    @Test
    void nullable_toString_empty() {
        final RetNullable<String> res = createForNullable("x");
        res.toString();
        assertNeverObserved(res);
    }

    @Test
    void nullable_toString_problem() {
        final RetNullable<String> res = createForNullableProblems(List.of(
                LocalizedProblem.from("a")));
        res.toString();
        assertNeverObserved(res);
    }

    @Test
    void val_toString_empty() {
        final RetVal<String> res = createForVal("x");
        res.toString();
        assertNeverObserved(res);
    }

    @Test
    void val_toString_problem() {
        final RetVal<String> res = createForValProblems(List.of(LocalizedProblem.from("a")));
        res.toString();
        assertNeverObserved(res);
    }

    @Test
    void void_toString_empty() {
        final RetVoid res = createForVoid();
        res.toString();
        assertNeverObserved(res);
    }

    @Test
    void void_toString_problem() {
        final RetVoid res = createForVoidProblems(List.of(LocalizedProblem.from("a")));
        res.toString();
        assertNeverObserved(res);
    }


    // ======================================================================
    // Test Boilerplate

    @BeforeEach
    final void beforeEach() {
        this.monitor = MockProblemMonitor.setup();
        // Default to using tracing.
        this.monitor.traceEnabled = true;
    }

    @AfterEach
    final void afterEach() {
        this.monitor.tearDown();
    }

    protected void assertNeverObserved(@Nonnull final ProblemContainer... values) {
        // Allow for duplicates and ordering to not matter.

        final Set<ProblemContainer> neverObserved = new HashSet<>(this.monitor.getNeverObserved());
        final Set<ProblemContainer> expected = Set.of(values);
        assertEquals(
                expected,
                neverObserved
        );
    }
}
