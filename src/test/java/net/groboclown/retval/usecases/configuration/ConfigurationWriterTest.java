// Released under the MIT License.
package net.groboclown.retval.usecases.configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.monitor.MockProblemMonitor;
import net.groboclown.retval.problems.LocalizedProblem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurationWriterTest {
    MockProblemMonitor monitor;

    @Test
    void writeConfigurationFrom_empty() {
        final RetVal<Properties> res = ConfigurationWriter.writeConfigurationFrom(List.of());
        // Ensure that there's no dangling checks.
        assertEquals(List.of(res), this.monitor.getNeverObserved());

        // This is a more effective way of checking that there were no problems.
        // Just checking true/false on isOk() will not tell you in the error report
        // what the errors were if there was a failure.
        assertEquals(List.of(), res.anyProblems());

        assertEquals(
                Map.of("projects", ""),
                Map.copyOf(res.result())
        );
    }

    @Test
    void writeConfigurationFrom_valid() throws MalformedURLException {
        final URL project1Url = new URL("http://a.b/p1");
        final URL project2Url = new URL("http://a.b/p2");
        final ProjectUser pu11 = new ProjectUser(
                "p1", "p 1", project1Url,
                "u1", "u 1", "u1@a.b");
        final ProjectUser pu12 = new ProjectUser(
                "p1", "p 1", project1Url,
                "u2", "u 2", "u2@a.b");
        final ProjectUser pu21 = new ProjectUser(
                "p2", "p 2", project2Url,
                "u1", "u 1", "u1@a.b");
        final ProjectUser pu23 = new ProjectUser(
                "p2", "p 2", project2Url,
                "u3", "u 3", "u3@a.b");

        final RetVal<Properties> res = ConfigurationWriter.writeConfigurationFrom(List.of(
                pu11, pu12, pu21, pu23
        ));
        // Ensure that there's no dangling checks.
        assertEquals(List.of(res), this.monitor.getNeverObserved());

        // This is a more effective way of checking that there were no problems.
        // Just checking true/false on isOk() will not tell you in the error report
        // what the errors were if there was a failure.
        assertEquals(List.of(), res.anyProblems());

        assertEquals(
                Map.copyOf(res.result()),
                Map.ofEntries(
                        Map.entry("projects", "p1,p2"),
                        Map.entry("project.p1.name", "p 1"),
                        Map.entry("project.p1.url", "http://a.b/p1"),
                        Map.entry("project.p1.users", "u1,u2"),
                        Map.entry("project.p2.name", "p 2"),
                        Map.entry("project.p2.url", "http://a.b/p2"),
                        Map.entry("project.p2.users", "u1,u3"),
                        Map.entry("user.u1.name", "u 1"),
                        Map.entry("user.u1.email", "u1@a.b"),
                        Map.entry("user.u2.name", "u 2"),
                        Map.entry("user.u2.email", "u2@a.b"),
                        Map.entry("user.u3.name", "u 3"),
                        Map.entry("user.u3.email", "u3@a.b")
                )
        );
    }

    @Test
    void writeConfigurationFrom_mismatchUserProject() throws MalformedURLException {
        final URL project1Url = new URL("http://a.b/p1");
        final URL project2Url = new URL("http://a.b/p2");
        final ProjectUser pu1 = new ProjectUser(
                "p1", "p 1a", project1Url,
                "u1", "u 1a", "u1@a.b");
        final ProjectUser pu2 = new ProjectUser(
                "p1", "p 1b", project1Url,
                "u1", "u 1b", "u1@a.b");

        final RetVal<Properties> res = ConfigurationWriter.writeConfigurationFrom(List.of(
                pu1, pu2
        ));
        // Ensure that there's no dangling checks.
        assertEquals(List.of(res), this.monitor.getNeverObserved());

        // This is a more effective way of checking that there were no problems.
        // Just checking true/false on isOk() will not tell you in the error report
        // what the errors were if there was a failure.
        assertEquals(
                List.of(
                        // Order is fixed because of the way it's constructed.
                        LocalizedProblem.from("User u1 defined differently in projects p1 and p1"),
                        LocalizedProblem.from("Project p1 defined differently in users u1 and u1")
                ),
                res.anyProblems()
        );
    }

    @BeforeEach
    void beforeEach() {
        this.monitor = MockProblemMonitor.setup();
        // For robustness...
        this.monitor.traceEnabled = true;
    }

    @AfterEach
    void afterEach() {
        this.monitor.tearDown();
    }
}
