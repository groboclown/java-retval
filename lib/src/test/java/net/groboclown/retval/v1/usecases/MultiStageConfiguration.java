// Released under the MIT License. 
package net.groboclown.retval.v1.usecases;

import net.groboclown.retval.v1.RetCollector;
import net.groboclown.retval.v1.RetVal;
import net.groboclown.retval.v1.problems.LocalizedProblem;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Properties;


/**
 * Builds a configuration based on a properties file.
 */
public class MultiStageConfiguration {
    private final Properties props;

    private static class Builder {
        private String username;
        private String email;
        private String project;

        @Nonnull
        public String requireUsername() {
            return Objects.requireNonNull(this.username);
        }

        public void setUsername(@Nonnull final String username) {
            this.username = Objects.requireNonNull(username);
        }

        @Nonnull
        public String requireEmail() {
            return Objects.requireNonNull(this.email);
        }

        public void setEmail(@Nonnull final String email) {
            this.email = Objects.requireNonNull(email);
        }

        @Nonnull
        public String requireProject() {
            return Objects.requireNonNull(this.project);
        }

        public void setProject(@Nonnull final String project) {
            this.project = Objects.requireNonNull(project);
        }
    }

    private MultiStageConfiguration(final Properties props) {
        this.props = props;
    }


    /**
     * Load a ProjectUser instance with the property values.
     *
     * @param props properties
     * @return the project user instance.
     */
    @Nonnull
    public static RetVal<ProjectUser> readProjectUser(final Properties props) {
        final MultiStageConfiguration config = new MultiStageConfiguration(props);
        final Builder builder = new Builder();
        return RetCollector
            // Load the independent parts first, and then the dependent parts.
            .fromValue(config.loadName(), builder::setUsername)
            .withValue(config.loadProject(), builder::setProject)

            // This requires the username.
            .then(() -> config.loadEmailFor(builder.requireUsername()))
            .thenVoid(builder::setEmail)

            // This requires everything to be loaded correctly.
            .thenValue(() -> new ProjectUser(
                    builder.requireProject(),
                    builder.requireUsername(),
                    builder.requireEmail()));
    }


    @Nonnull
    private RetVal<String> loadName() {
        return requireKey("name");
    }

    @Nonnull
    private RetVal<String> loadProject() {
        return requireKey("project");
    }

    @Nonnull
    private RetVal<String> loadEmailFor(@Nonnull final String username) {
        return requireKey(username + ".email");
    }

    private RetVal<String> requireKey(@Nonnull final String key) {
        final String value = this.props.getProperty(key);
        if (value == null) {
            return RetVal.error(LocalizedProblem.from("no `" + key + "` property"));
        }
        if (value.trim().isEmpty()) {
            return RetVal.error(LocalizedProblem.from("`" + key + "` value is blank"));
        }
        return RetVal.ok(value);
    }
}
