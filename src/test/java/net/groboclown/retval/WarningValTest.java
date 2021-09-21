// Released under the MIT License. 
package net.groboclown.retval;

import java.util.ArrayList;
import java.util.List;
import net.groboclown.retval.problems.LocalizedProblem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WarningValTest {
    @Test
    void noProblems() {
        final WarningVal<String> val = WarningVal.from("x");
        assertEquals(List.of(), val.anyProblems());
        assertEquals("x", val.getValue());
        try {
            val.validProblems();
            fail("Did not throw ISE");
        } catch (final IllegalStateException e) {
            // do not inspect exception
        }
        assertTrue(val.isOk());
        assertFalse(val.hasProblems());
        assertFalse(val.isProblem());
        assertEquals("", val.debugProblems(";"));
        final List<Problem> joined = new ArrayList<>();
        val.joinProblemsWith(joined);
        assertEquals(List.of(), joined);
    }

    @Test
    void problems() {
        final LocalizedProblem problem1 = LocalizedProblem.from("p1");
        final LocalizedProblem problem2 = LocalizedProblem.from("p2");
        final WarningVal<String> val = WarningVal.from("x",
                new TestableProblemContainer(problem1, problem2));
        assertEquals(List.of(problem1, problem2), val.anyProblems());
        assertEquals(List.of(problem1, problem2), val.validProblems());
        assertEquals("x", val.getValue());
        assertFalse(val.isOk());
        assertTrue(val.hasProblems());
        assertTrue(val.isProblem());
        assertEquals("p1;p2", val.debugProblems(";"));
        final List<Problem> joined = new ArrayList<>();
        val.joinProblemsWith(joined);
        assertEquals(List.of(problem1, problem2), joined);
    }
}
