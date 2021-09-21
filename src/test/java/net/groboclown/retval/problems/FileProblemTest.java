// Released under the MIT License.
package net.groboclown.retval.problems;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileProblemTest {

    @Test
    void from_string_string() {
        final FileProblem problem = FileProblem.from("fs", "pr");
        assertEquals("fs", problem.getSource());
        assertEquals("pr", problem.localMessage());
        assertEquals("fs: pr", problem.toString());
    }

    @Test
    void from_file_ex_withMessage() {
        final FileProblem problem = FileProblem.from(
                new File(new File("a"), "fs"), new IOException("ex"));
        final String name = "a" + File.separator + "fs";
        assertEquals(name, problem.getSource());
        assertEquals("ex", problem.localMessage());
        assertEquals(name + ": ex", problem.toString());
    }

    @Test
    void from_file_ex_withoutMessage() {
        final FileProblem problem = FileProblem.from(
                new File(new File("a"), "fs"), new IOException());
        final String name = "a" + File.separator + "fs";
        assertEquals(name, problem.getSource());
        assertEquals(name + " caused a problem", problem.localMessage());
        assertEquals(name + ": " + name + " caused a problem", problem.toString());
    }

    @Test
    void from_string_ex() {
        final FileProblem problem = FileProblem.from("fs", new IOException("pr"));
        assertEquals("fs", problem.getSource());
        assertEquals("pr", problem.localMessage());
        assertEquals("fs: pr", problem.toString());
    }
}
