// Released under the MIT License.

package net.groboclown.retval.v1;

import net.groboclown.retval.v1.problems.LocalizedProblem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RetVoidTest {
    @Test
    void withProblemSets_none() {
        RetVoid ret = RetVoid.withProblemSets();
        assertSame(
                RetVoid.OK,
                ret
        );
    }

    @Test
    void withProblemSets_empty() {
        RetVoid ret = RetVoid.withProblemSets(List.of(), List.of());
        assertSame(
                RetVoid.OK,
                ret
        );
    }

    @Test
    void withProblemSets_some() {
        final Problem p1 = LocalizedProblem.from("1");
        final Problem p2 = LocalizedProblem.from("2");
        RetVoid ret = RetVoid.withProblemSets(List.of(p1), List.of(p2));
        assertEquals(
                List.of(p1, p2),
                ret.anyProblems()
        );
    }
}
