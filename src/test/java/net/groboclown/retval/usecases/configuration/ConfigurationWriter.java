// Released under the MIT License. 
package net.groboclown.retval.usecases.configuration;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.groboclown.retval.ProblemCollector;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.WarningVal;
import net.groboclown.retval.problems.LocalizedProblem;

/**
 * Creates the properties for a complete configuration, that can then be read by the
 * {@link ConfigurationReader}.
 */
public class ConfigurationWriter {
    private ConfigurationWriter() {
        // prevent instantiation.
    }

    /**
     * Converts the list of project users into a form that can be read by the
     * {@link ConfigurationReader}.
     *
     * @param projectUsers list of projects+users.
     * @return properties with the configuration, or problems.
     */
    @Nonnull
    public static RetVal<Properties> writeConfigurationFrom(
            @Nonnull final Collection<ProjectUser> projectUsers) {
        final WarningVal<Collection<ProjectUser>> users = getDistinctUsers(projectUsers);
        final WarningVal<Collection<ProjectUser>> projects = getDistinctProjects(projectUsers);
        if (users.hasProblems() || projects.hasProblems()) {
            return RetVal.fromProblems(users, projects);
        }
        final Properties ret = new Properties();
        ret.setProperty("projects", projects.getValue().stream()
                .map(ProjectUser::getProjectId)
                .collect(Collectors.joining(",")));
        for (final ProjectUser project : projects.getValue()) {
            ret.setProperty(
                    "project." + project.getProjectId() + ".name",
                    project.getProjectName()
            );
            ret.setProperty(
                    "project." + project.getProjectId() + ".url",
                    project.getProjectUrl().toString()
            );
            ret.setProperty(
                    "project." + project.getProjectId() + ".users",
                    getUsersForProject(projectUsers, project.getProjectId())
                            .map(ProjectUser::getUser)
                            .collect(Collectors.joining(","))
            );
        }

        for (final ProjectUser user : users.getValue()) {
            ret.setProperty(
                    "user." + user.getUser() + ".name",
                    user.getRealName()
            );
            ret.setProperty(
                    "user." + user.getUser() + ".email",
                    user.getEmail()
            );
        }

        return RetVal.ok(ret);
    }

    /**
     * Split the list of project/user objects into instances of distinct users.  The
     * project part should be ignored.
     *
     * @return distinct user objects.
     */
    @Nonnull
    private static WarningVal<Collection<ProjectUser>> getDistinctUsers(
            @Nonnull final Collection<ProjectUser> projectUsers
    ) {
        final Map<String, ProjectUser> distinctUsers = new HashMap<>();
        final ProblemCollector problems = ProblemCollector.from();

        for (final ProjectUser projectUser : projectUsers) {
            // Just replace the existing one with the new value, but capture the
            // old one to see if there's a difference between them.
            final ProjectUser existing = distinctUsers.put(projectUser.getUser(), projectUser);
            if (existing != null
                    && (! Objects.equals(existing.getEmail(), projectUser.getEmail())
                    || ! Objects.equals(existing.getRealName(), projectUser.getRealName()))) {
                problems.with(LocalizedProblem.from(
                        "User " + projectUser.getUser() + " defined differently in projects "
                        + projectUser.getProjectId() + " and " + existing.getProjectId()));
            }
        }

        return WarningVal.from(distinctUsers.values(), problems);
    }

    /**
     * Split the list of project/user objects into instances of distinct projects.  The
     * user part is ignored.
     *
     * @return distinct projects.
     */
    @Nonnull
    private static WarningVal<Collection<ProjectUser>> getDistinctProjects(
            @Nonnull final Collection<ProjectUser> projectUsers
    ) {
        final Map<String, ProjectUser> distinctProjects = new HashMap<>();
        final ProblemCollector problems = ProblemCollector.from();

        for (final ProjectUser projectUser : projectUsers) {
            // Just replace the existing one with the new value, but capture the
            // old one to see if there's a difference between them.
            final ProjectUser existing = distinctProjects.put(
                    projectUser.getProjectId(), projectUser
            );
            if (existing != null
                    && (! Objects.equals(existing.getProjectName(), projectUser.getProjectName())
                    || ! Objects.equals(existing.getProjectUrl(), projectUser.getProjectUrl()))) {
                problems.with(LocalizedProblem.from(
                        "Project " + projectUser.getProjectId()
                        + " defined differently in users "
                        + projectUser.getUser() + " and " + existing.getUser()));
            }
        }

        return WarningVal.from(distinctProjects.values(), problems);
    }


    @Nonnull
    private static Stream<ProjectUser> getUsersForProject(
            @Nonnull final Collection<ProjectUser> all,
            @Nonnull final String projectId
    ) {
        return all.stream()
                .filter((projectUser) -> projectId.equals(projectUser.getProjectId()))
                // For testability ease, sort the returned list.
                .sorted(Comparator.comparing(ProjectUser::getUser));
    }
}
