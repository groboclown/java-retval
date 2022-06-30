// Released under the MIT License.
package net.groboclown.retval.usecases.propertyfile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.groboclown.retval.Ret;
import net.groboclown.retval.RetNullable;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.problems.FileProblem;
import net.groboclown.retval.problems.LocalizedProblem;


/**
 * Example of using nullable values.
 */
public class PropertyFile {
    private final Map<String, String> properties;

    private PropertyFile(@Nonnull Map<String, String> properties) {
        this.properties = properties;
    }


    /**
     * Create a {@link PropertyFile} from a file in the classpath.
     *
     * @param owningClass class loader and base path
     * @param path relative path
     * @return the property file result.
     */
    @Nonnull
    public static RetVal<PropertyFile> loadFromClasspath(
            @Nonnull final Class<?> owningClass, @Nonnull final String path) {
        final InputStream stream = owningClass.getResourceAsStream(path);
        if (stream == null) {
            return RetVal.fromProblem(
                    FileProblem.from(
                            path,
                            "No file found in classpath relative to " + owningClass.getName()));
        }
        return Ret.closeWith(new InputStreamReader(stream, StandardCharsets.UTF_8), (inp) -> {
            final Properties props = new Properties();
            props.load(inp);
            final Map<String, String> map = new HashMap<>();
            for (String name : props.stringPropertyNames()) {
                map.put(name, props.getProperty(name));
            }
            return RetVal.ok(new PropertyFile(Collections.unmodifiableMap(map)));
        });
    }


    /**
     * Get the (possibly null) key value.
     *
     * @param key key to get from the properties.
     * @return the value for the key, or {@literal null} if it isn't present.
     */
    @Nullable
    public String get(@Nonnull String key) {
        return this.properties.get(key);
    }

    /**
     * Get the key in a nullable return wrapper.
     *
     * <p>By itself, this is better handled as an {@link java.util.Optional} return
     * value.  However, it's intended to be used with type casting methods, where the
     * problem aspect becomes important.
     *
     * @param key key to get from the properties.
     * @return the value for the key, or {@literal null} value if it isn't present.
     */
    @Nonnull
    public RetNullable<String> resolve(@Nonnull String key) {
        return RetNullable.ok(get(key));
    }

    /**
     * Resolve the property into an integer.  Conversion errors are returned as property errors.
     *
     * @param key key to get from the properties.
     * @return the value for the key, or {@literal null} value if it isn't present.
     */
    @Nonnull
    public RetNullable<Integer> resolveInt(@Nonnull String key) {
        return
                resolve(key)
            .nullOrThenNullable((value) -> {
                try {
                    return RetNullable.ok(Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    return RetNullable.fromProblem(LocalizedProblem.from(
                            "Key " +  key + " is not a number (" + value + ")"));
                }
            });
    }
}
