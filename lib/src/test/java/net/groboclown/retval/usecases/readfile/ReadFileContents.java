// Released under the MIT License. 
package net.groboclown.retval.usecases.readfile;

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;
import javax.annotation.Nonnull;
import javax.annotation.WillClose;
import net.groboclown.retval.v1.RetVal;
import net.groboclown.retval.v1.problems.FileProblem;

/**
 * Use case: read the contents of a file or readable file.
 */
public class ReadFileContents {
    private static final int BUFFER_SIZE = 4096;

    private ReadFileContents() {
        // Prevent instantiation
    }

    /**
     * Read all the contents of the reader into memory.
     *
     * @param sourceName source of the reader, for error reporting.
     * @param reader reader object.
     * @return the contents of the reader.
     */
    @WillClose
    @Nonnull
    public static RetVal<String> readFully(
            @Nonnull final String sourceName, @Nonnull final Reader reader
    ) {
        try (reader) {
            final StringBuilder ret = new StringBuilder();
            final char[] buff = new char[BUFFER_SIZE];
            int len;
            while ((len = reader.read(buff, 0, BUFFER_SIZE)) > 0) {
                ret.append(buff, 0, len);
            }
            return RetVal.ok(ret.toString());
        } catch (final IOException e) {
            return RetVal.error(FileProblem.from(sourceName, e));
        }
    }

    /**
     * Read a properties file from a reader.
     *
     * @param sourceName properties file name.
     * @param reader contents of the properties file.
     * @return the properties.
     */
    @WillClose
    @Nonnull
    public static RetVal<Properties> readProperties(
            @Nonnull final String sourceName, @Nonnull final Reader reader
    ) {
        try (reader) {
            final Properties ret = new Properties();
            ret.load(reader);
            return RetVal.ok(ret);
        } catch (final IOException e) {
            return RetVal.error(FileProblem.from(sourceName, e));
        }
    }
}
