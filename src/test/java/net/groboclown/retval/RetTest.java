// Released under the MIT License.
package net.groboclown.retval;

import java.util.List;
import net.groboclown.retval.Problem;
import net.groboclown.retval.Ret;
import net.groboclown.retval.problems.LocalizedProblem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests for the {@link Ret} class.
 */
class RetTest {
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
                Ret.joinProblemSets(List.of(p1, p2))
        );
        assertEquals(
                List.of(p1, p2),
                Ret.joinProblemSets(List.of(), List.of(p1), List.of(p2))
        );
        // Ordering...
        assertEquals(
                List.of(p2, p1),
                Ret.joinProblemSets(List.of(p2), List.of(p1))
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
}
