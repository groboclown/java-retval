// Released under the MIT License. 
package net.groboclown.retval.usecases.configuration;

import java.net.URL;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * An example immutable POJO (plain old java object) for a property container.
 */
public class ProjectUser {
    private final String projectId;
    private final String projectName;
    private final URL projectUrl;
    private final String user;
    private final String realName;
    private final String email;

    ProjectUser(
            @Nonnull final String projectId,
            @Nonnull final String projectName,
            @Nonnull final URL projectUrl,
            @Nonnull final String user,
            @Nonnull final String realName,
            @Nonnull final String email) {
        this.projectId = Objects.requireNonNull(projectId);
        this.projectName = Objects.requireNonNull(projectName);
        this.projectUrl = Objects.requireNonNull(projectUrl);
        this.user = Objects.requireNonNull(user);
        this.realName = Objects.requireNonNull(realName);
        this.email = Objects.requireNonNull(email);
    }

    @Nonnull
    public String getProjectId() {
        return this.projectId;
    }

    @Nonnull
    public String getProjectName() {
        return this.projectName;
    }

    @Nonnull
    public String getUser() {
        return this.user;
    }

    @Nonnull
    public String getRealName() {
        return this.realName;
    }

    @Nonnull
    public String getEmail() {
        return this.email;
    }

    @Nonnull
    public URL getProjectUrl() {
        return this.projectUrl;
    }
}
