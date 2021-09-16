// Released under the MIT License. 
package net.groboclown.retval.v1.usecases;

import net.groboclown.retval.v1.RetVal;
import net.groboclown.retval.v1.problems.LocalizedProblem;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MultiStageConfigurationTest {
    @Test
    void test_readProjectUser_empty() {
        final Properties props = new Properties();
        final RetVal<ProjectUser> res = MultiStageConfiguration.readProjectUser(props);
        assertEquals(
                List.of(
                        LocalizedProblem.from("no `name` property"),
                        LocalizedProblem.from("no `project` property")
                ),
                res.anyProblems()
        );
    }
}
