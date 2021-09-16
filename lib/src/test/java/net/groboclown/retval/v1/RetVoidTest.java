// Released under the MIT License.

package net.groboclown.retval.v1;

import java.util.List;
import net.groboclown.retval.v1.problems.LocalizedProblem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RetVoidTest {
    @Test
    void errors_collection_none() {
        final RetVoid ret = RetVoid.errors();
        assertEquals(
                List.of(),
                ret.anyProblems()
        );
    }

    @Test
    void errors_collection_empty() {
        final RetVoid ret = RetVoid.errors(List.of(), List.of());
        assertEquals(
                List.of(),
                ret.anyProblems()
        );
    }

    @Test
    void error_collection_some() {
        final Problem p1 = LocalizedProblem.from("1");
        final Problem p2 = LocalizedProblem.from("2");
        final RetVoid ret = RetVoid.error(List.of(p1), List.of(p2));
        assertEquals(
                List.of(p1, p2),
                ret.anyProblems()
        );
    }

    @Test
    void errors_empty() {
        final RetVoid ret = RetVoid.errors();
        assertEquals(
                List.of(),
                ret.anyProblems()
        );
    }
}
