// Released under the MIT License. 
package net.groboclown.retval.usecases.configuration;

import java.util.List;
import java.util.Properties;
import net.groboclown.retval.v1.RetVal;
import net.groboclown.retval.v1.impl.MockCheckMonitor;
import net.groboclown.retval.v1.problems.LocalizedProblem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationReaderTest {
    MockCheckMonitor monitor;

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
                this.monitor.getNeverChecked()
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
                res.anyProblems()
        );
        assertEquals(
                List.of(),
                this.monitor.getNeverChecked()
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
                this.monitor.getNeverChecked()
        );
    }

    @BeforeEach
    void beforeEach() {
        this.monitor = MockCheckMonitor.setup();
    }

    @AfterEach
    void afterEach() {
        this.monitor.tearDown();
    }
}
