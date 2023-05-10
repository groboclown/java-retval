// Released under the MIT License.
package net.groboclown.retval.usecases.collected;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.groboclown.retval.RetNullable;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.RetVoid;
import net.groboclown.retval.problems.LocalizedProblem;

/**
 * Stores typed resources.
 */
public class ResourceStore {
    private final Object sync = new Object();
    private final Map<String, Object> resources = new HashMap<>();

    /**
     * Add a unique resource to the store.
     *
     * @param name name of the resource
     * @param value non-null value to store.
     * @return success or failure
     */
    @Nonnull
    public RetVoid addResource(@Nonnull final String name, @Nonnull final Object value) {
        synchronized (this.sync) {
            if (this.resources.containsKey(name)) {
                return RetVoid.fromProblem(LocalizedProblem.from(
                        "Duplicate resource " + name));
            }
            this.resources.put(Objects.requireNonNull(name), Objects.requireNonNull(value));
        }
        return RetVoid.ok();
    }

    /**
     * Get the resource from the store.  If the resource isn't
     * registered, then a null value is returned.
     * If the value is stored but of the wrong type, then a
     * problem is returned.
     *
     * @param name name of the resource; passing null is allowed as a helper.
     * @param type expected value type
     * @param <T> value type
     * @return the value if found and of the correct type, null if not found,
     *      or an error if of the wrong type
     */
    @Nonnull
    public <T> RetNullable<T> getResource(
            @Nullable final String name,
            @Nonnull final Class<? extends T> type) {
        if (name == null) {
            return RetNullable.ok(null);
        }

        final Object value;
        synchronized (this.sync) {
            value = this.resources.get(name);
        }
        if (value == null) {
            return RetNullable.ok(null);
        }
        if (type.isInstance(value)) {
            return RetNullable.ok(type.cast(value));
        }
        return RetNullable.fromProblem(
                typeMismatch(name, type, value.getClass()));
    }

    /**
     * Require that the named resource exists and is of the expected type.
     *
     * @param name resource name
     * @param type expected type
     * @param <T> value type
     * @return the value if found and of the correct type, else a problem
     */
    @Nonnull
    public <T> RetVal<T> requireResource(
            @Nonnull final String name,
            @Nonnull final Class<? extends T> type) {
        final Object value;
        synchronized (this.sync) {
            value = this.resources.get(name);
        }
        if (value == null) {
            return RetVal.fromProblem(LocalizedProblem.from(
                    name + ": resource not registered"));
        }
        if (type.isInstance(value)) {
            return RetVal.ok(type.cast(value));
        }
        return RetVal.fromProblem(typeMismatch(name, type, value.getClass()));
    }

    @Nonnull
    private static LocalizedProblem typeMismatch(
            @Nonnull final String name,
            @Nonnull final Class<?> expected,
            @Nonnull final Class<?> actual) {
        return LocalizedProblem.from(
                name + ": expected " + expected + ", found " + actual);
    }
}
