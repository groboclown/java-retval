// Released under the MIT License. 
package net.groboclown.retval.v1.usecases;


import javax.annotation.Nonnull;

/**
 * An example of a class that has required values.
 */
public class ProjectUser {
    private final String project;
    private final String username;
    private final String email;

    ProjectUser(
            @Nonnull final String project,
            @Nonnull final String username,
            @Nonnull final String email) {
        this.project = project;
        this.username = username;
        this.email = email;
    }

    @Nonnull
    public String getProject() {
        return this.project;
    }

    @Nonnull
    public String getUsername() {
        return this.username;
    }

    @Nonnull
    public String getEmail() {
        return this.email;
    }
}
