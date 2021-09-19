// Released under the MIT License. 
package net.groboclown.retval.usecases.configuration;

import java.util.List;
import java.util.Properties;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.monitor.MockProblemMonitor;
import net.groboclown.retval.problems.LocalizedProblem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurationReaderTest {
    MockProblemMonitor monitor;

    @Test
    void test_readProjectUser_empty() {
        final Properties props = new Properties();
        final RetVal<List<ProjectUser>> res = ConfigurationReader.readProjectUsers("p1", props);
        assertEquals(
                List.of(
                        LocalizedProblem.from("no `projects` property")
                ),
                res.anyProblems()
        );
        assertEquals(
                List.of(),
                this.monitor.getNeverObserved()
        );
    }

    @Test
    void test_readProjectUser_noSuchProject() {
        final Properties props = new Properties();
        props.setProperty("projects", "p2,p3");
        final RetVal<List<ProjectUser>> res = ConfigurationReader.readProjectUsers("p1", props);
        assertEquals(
                List.of(
                        LocalizedProblem.from("project `p1` is not registered")
                ),
                // This should count as a check.
                res.anyProblems()
        );
        assertEquals(
                List.of(),
                this.monitor.getNeverObserved()
        );
    }

    @Test
    void test_readProjectUser_noProjectValues() {
        final Properties props = new Properties();
        props.setProperty("projects", "p1");
        final RetVal<List<ProjectUser>> res = ConfigurationReader.readProjectUsers("p1", props);
        assertEquals(
                List.of(
                        LocalizedProblem.from("no `project.p1.name` property"),
                        LocalizedProblem.from("no `project.p1.users` property"),
                        LocalizedProblem.from("no `project.p1.url` property")
                ),
                res.anyProblems()
        );
        assertEquals(
                List.of(),
                this.monitor.getNeverObserved()
        );
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
}
