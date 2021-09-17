// Released under the MIT License.

package net.groboclown.retval;

import java.util.List;
import net.groboclown.retval.problems.LocalizedProblem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RetVoidTest {
    @Test
    void fromProblems_collection_none() {
        final RetVoid ret = RetVoid.fromProblems(List.of());
        assertEquals(
                List.of(),
                ret.anyProblems()
        );
    }

    @Test
    void fromProblems_collection_empty() {
        final RetVoid ret = RetVoid.fromProblems(List.of(), List.of());
        assertEquals(
                List.of(),
                ret.anyProblems()
        );
    }

    @Test
    void fromProblems_collection_some() {
        final Problem p1 = LocalizedProblem.from("1");
        final Problem p2 = LocalizedProblem.from("2");
        final RetVoid ret = RetVoid.fromProblem(List.of(p1), List.of(p2));
        assertEquals(
                List.of(p1, p2),
                ret.anyProblems()
        );
    }

    @Test
    void fromProblems_empty() {
        final RetVoid ret = RetVoid.fromProblems(List.of());
        assertEquals(
                List.of(),
                ret.anyProblems()
        );
    }
}
