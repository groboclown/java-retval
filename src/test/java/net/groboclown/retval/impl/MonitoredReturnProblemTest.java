// Released under the MIT License. 
package net.groboclown.retval.impl;

import java.util.List;
import net.groboclown.retval.Problem;
import net.groboclown.retval.problems.LocalizedProblem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

class MonitoredReturnProblemTest {
    @Test
    void constructor_problems_empty() {
        try {
            new MonitoredReturnProblem<String>(List.of());
            fail("Did not throw IAE");
        } catch (final IllegalArgumentException e) {
            // Skip exception text introspection
        }
    }


    @Test
    void constructor_problems_notEmpty() {
        final List<Problem> problems = List.of(LocalizedProblem.from("a"));
        final MonitoredReturnProblem<Object> res = new MonitoredReturnProblem<>(problems);
        assertSame(problems, res.anyProblems());
        assertSame(problems, res.validProblems());
    }
}
