// Released under the MIT License. 
package net.groboclown.retval.monitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Class to aid in making the code testable.
 */
class SystemEnvUtil {
    private static final Map<String, String> settings;

    static {
        // Note: Not unmodifiable...
        // And order is important.
        settings = new HashMap<>(System.getenv());
        final Properties props = System.getProperties();
        props.propertyNames().asIterator().forEachRemaining((key) ->
                settings.put(key.toString(), props.getProperty(key.toString())));
    }

    static boolean isValueEqual(@Nonnull final String key, @Nonnull final String expectedValue) {
        return expectedValue.equals(settings.get(key));
    }


    static Map<String, String> getSettings() {
        return settings;
    }

    private SystemEnvUtil() {
        // prevent instantiation
    }
}
