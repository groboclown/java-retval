// Released under the MIT License.
package net.groboclown.retval.problems;

import java.util.HashMap;
import java.util.Map;
import net.groboclown.retval.Problem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocalizedProblemTest {

    @Test
    void from() {
        final LocalizedProblem problem = LocalizedProblem.from("x");
        assertEquals("x", problem.localMessage());
        assertEquals("x", problem.toString());
    }

    @Test
    void testHashCode() {
        final LocalizedProblem problemX1 = LocalizedProblem.from("x");
        final LocalizedProblem problemX2 = LocalizedProblem.from("x");
        final LocalizedProblem problemY = LocalizedProblem.from("y");

        final Map<Problem, String> map = new HashMap<>();
        map.put(problemX1, "1");
        assertEquals(Map.of(problemX1, "1"), map);
        map.put(problemY, "2");
        assertEquals(Map.of(problemX1, "1", problemY, "2"), map);
        map.put(problemX1, "3");
        assertEquals(Map.of(problemX1, "3", problemY, "2"), map);
        map.put(problemX2, "4");
        assertEquals(Map.of(problemX2, "4", problemY, "2"), map);
    }

    @Test
    void testEquals() {
        final LocalizedProblem problemX1 = LocalizedProblem.from("x");
        final LocalizedProblem problemX2 = LocalizedProblem.from("x");
        final LocalizedProblem problemY = LocalizedProblem.from("y");

        assertTrue(problemX1.equals(problemX1));
        assertTrue(problemX1.equals(problemX2));
        assertTrue(problemX2.equals(problemX1));
        assertFalse(problemX1.equals(problemY));
        assertFalse(problemY.equals(problemX1));
        assertFalse(problemX1.equals(null));
        assertFalse(problemX1.equals("x"));
    }
}
