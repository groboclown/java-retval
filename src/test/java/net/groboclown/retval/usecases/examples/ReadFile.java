// Released under the MIT License. 
package net.groboclown.retval.usecases.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.problems.FileProblem;

class ReadFile {
    public RetVal<Properties> readPropertiesFile(File file) {
        try (Reader reader = new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8);) {
            final Properties ret = new Properties();
            ret.load(reader);
            return RetVal.ok(ret);
        } catch (final IOException e) {
            return RetVal.fromProblem(FileProblem.from(file, e));
        }
    }
}
