// Released under the MIT License. 
package net.groboclown.retval.usecases.configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.groboclown.retval.v1.RetCollector;
import net.groboclown.retval.v1.RetVal;
import net.groboclown.retval.v1.ValueAccumulator;
import net.groboclown.retval.v1.WarningVal;
import net.groboclown.retval.v1.problems.FileProblem;
import net.groboclown.retval.v1.problems.LocalizedProblem;


/**
 * Builds a configuration based on a properties file.
 */
public class MultiStageConfiguration {
    private final Properties props;

    private MultiStageConfiguration(final Properties props) {
        this.props = props;
    }


    /**
     * Load a ProjectUser instance with the property values.  The property file contains a set
     * of projects, which are in the form "project.(name).(key)", and a set of users, which are
     * in the form "user.(name).(key)".  Each project has a "name" (human-readable name),
     * "url" (project site), and "users" (which is a comma-separated list of user ids).  Each user
     * has a "name" (real name), and "email" (contact email address).  A top level property,
     * "projects", contains a comma-separated list of project IDs.
     *
     * @param props properties
     * @return the project user instance.
     */
    @Nonnull
    public static RetVal<List<ProjectUser>> readProjectUsers(
            @Nonnull final String projectId, @Nonnull final Properties props
    ) {
        final MultiStageConfiguration config = new MultiStageConfiguration(props);

        // First, find the project.  If this isn't found, then there's no other problems to check.
        final RetVal<String> projectIdRes = config.validateProjectId(projectId);
        if (projectIdRes.hasProblems()) {
            return projectIdRes.forwardError();
        }

        final WarningVal<ProjectBuilder> projectRes = config.loadProject(projectId);
        // The project might be invalid, but we should also ensure the list of users for it
        // are valid, to help gather as many problems as possible without going too deep in
        // validating the whole file.
        final RetVal<Collection<UserBuilder>> userListRes = ValueAccumulator.<UserBuilder>from()
                .withEach(projectRes.getValue().getProjectUsers(), config::loadUser)
                .asRetVal();

        if (projectRes.hasProblems() || userListRes.hasProblems()) {
            return RetVal.errors(projectRes, userListRes);
        }

        return RetVal.ok(
                // Convert the list of UserBuilder into one ProjectUser per user, populating
                // the values from the project and the user.
                userListRes.result().stream().map((builder) -> new ProjectUser(
                        projectRes.getValue().requireProjectId(),
                        projectRes.getValue().requireProjectName(),
                        projectRes.getValue().requireProjectUrl(),
                        builder.requireUser(),
                        builder.requireRealName(),
                        builder.requireEmail()
                )).collect(Collectors.toList())
        );
    }

    @Nonnull
    private RetVal<String> validateProjectId(@Nonnull final String projectId) {
        // Fetch the list of project IDs, and ensure the requested ID is in the list.
        return
                loadProjectIdList()
            .then((projectIdList) -> {
                // Validate the list, to ensure the requested project exists.
                if (! projectIdList.contains(projectId)) {
                    return RetVal.error(LocalizedProblem.from(
                            "project `" + projectId + "` is not registered"
                    ));
                }
                return RetVal.ok(projectId);
            });
    }

    /**
     * Load a user, or the set of problems associated with its definition.  A user doesn't have
     * any values that can be in a partially usable state.
     *
     * @param userId user ID of the user to load
     * @return the user or problems.
     */
    @Nonnull
    private RetVal<UserBuilder> loadUser(@Nonnull final String userId) {
        final UserBuilder builder = new UserBuilder();
        builder.setUser(userId);
        return RetCollector.from()
            .withValue(loadRealNameFor(userId), builder::setRealName)
            .withValue(loadEmailFor(userId), builder::setEmail)
            .thenReturn(builder);
    }

    /**
     * It's possible to have a project with valid user ID list that can be checked for
     * validity, even if other values in the project are not valid.
     *
     * @param projectId project ID to load
     * @return a warning value
     */
    @Nonnull
    private WarningVal<ProjectBuilder> loadProject(@Nonnull final String projectId) {
        final ProjectBuilder builder = new ProjectBuilder();
        final WarningVal<ProjectBuilder> ret = WarningVal.from(builder);
        builder.setProjectId(projectId);
        ret.getCollector()
                .withValue(loadProjectName(projectId), builder::setProjectName)
                .withValue(loadAllowedProjectUserList(projectId), builder::setProjectUsers)
                .withValue(loadProjectUrl(projectId), builder::setProjectUrl);
        return ret;
    }


    @Nonnull
    private RetVal<Collection<String>> loadProjectIdList() {
        return requireKeyList("projects");
    }

    @Nonnull
    private RetVal<String> loadProjectName(@Nonnull final String projectId) {
        return requireKey("project." + projectId + ".name");
    }

    @Nonnull
    private RetVal<URL> loadProjectUrl(@Nonnull final String projectId) {
        // Need to transform the string value into a valid URL.
        return
                requireKey("project." + projectId + ".url")
            .then((value) -> {
                // Perform the value mapping.
                try {
                    return RetVal.ok(new URL(value));
                } catch (final MalformedURLException e) {
                    return RetVal.error(FileProblem.from(value, e));
                }
            });
    }

    @Nonnull
    private RetVal<Collection<String>> loadAllowedProjectUserList(@Nonnull final String projectId) {
        return requireKeyList("project." + projectId + ".users");
    }

    @Nonnull
    private RetVal<String> loadRealNameFor(@Nonnull final String user) {
        return requireKey("user." + user + ".email");
    }

    @Nonnull
    private RetVal<String> loadEmailFor(@Nonnull final String user) {
        return requireKey("user." + user + ".email");
    }

    private RetVal<Collection<String>> requireKeyList(@Nonnull final String key) {
        // This is complex logic wrapped in something simple looking.  Comments here will break
        // it down.
        // Note that the typing can be tricky, as Lambdas tend to be.
        return
                // A ValueAccumulator gathers values (here, Strings) and problems for a final
                // RetVal generation.
                ValueAccumulator.<String>from()

            // withEach loops over the first RetVal<Collection> collection values (or gathers the
            // problems), and with each item it processes it through the second argument function
            // for additional checking.
            .withEach(
                // requireKey finds the key's value in the properties, and validates that it has
                // an appropriate form.
                requireKey(key)
                        // once a valid value is found, split it into a list.
                        .thenValue((val) -> Arrays.asList(val.split(","))),

                // This lambda examines each piece split out by the above computation for validity.
                (part) -> {
                    if (part == null || part.trim().isEmpty()) {
                        return RetVal.error(LocalizedProblem.from(
                                "List of `" + key + "` cannot contain empty values"
                        ));
                    }
                    return RetVal.ok(part.trim());
                })
            .asRetVal();
    }

    private RetVal<String> requireKey(@Nonnull final String key) {
        // Simple, plain java value validity checks, with the one variation to
        // wrap the validation and value in a RetVal, rather than throwing exceptions.
        final String value = this.props.getProperty(key);
        if (value == null) {
            return RetVal.error(LocalizedProblem.from("no `" + key + "` property"));
        }
        if (value.trim().isEmpty()) {
            return RetVal.error(LocalizedProblem.from("`" + key + "` value is blank"));
        }
        return RetVal.ok(value);
    }


    private static class ProjectBuilder {
        private String projectId;
        private String projectName;
        private URL projectUrl;
        private List<String> projectUsers = new ArrayList<>();

        @Nonnull
        public String requireProjectId() {
            return Objects.requireNonNull(this.projectId);
        }

        public void setProjectId(@Nonnull final String projectId) {
            this.projectId = Objects.requireNonNull(projectId);
        }

        @Nonnull
        public String requireProjectName() {
            return Objects.requireNonNull(this.projectName);
        }

        public void setProjectName(@Nonnull final String projectName) {
            this.projectName = Objects.requireNonNull(projectName);
        }

        /** Unlike all the other getters, this gets the values whether they were set
         *  correctly or not. */
        @Nonnull
        public List<String> getProjectUsers() {
            return this.projectUsers;
        }

        public void setProjectUsers(@Nonnull final Collection<String> projectUsers) {
            this.projectUsers = List.copyOf(projectUsers);
        }

        @Nonnull
        public URL requireProjectUrl() {
            return Objects.requireNonNull(this.projectUrl);
        }

        public void setProjectUrl(@Nonnull final URL projectUrl) {
            this.projectUrl = projectUrl;
        }
    }

    private static class UserBuilder {
        private String user;
        private String realName;
        private String email;

        @Nonnull
        public String requireUser() {
            return Objects.requireNonNull(this.user);
        }

        public void setUser(@Nonnull final String user) {
            this.user = Objects.requireNonNull(user);
        }

        @Nonnull
        public String requireRealName() {
            return Objects.requireNonNull(this.realName);
        }

        public void setRealName(@Nonnull final String realName) {
            this.realName = Objects.requireNonNull(realName);
        }

        @Nonnull
        public String requireEmail() {
            return Objects.requireNonNull(this.email);
        }

        public void setEmail(@Nonnull final String email) {
            this.email = Objects.requireNonNull(email);
        }
    }
}
