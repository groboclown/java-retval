// Released under the MIT License. 
package net.groboclown.retval.usecases.examples;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.problems.FileProblem;

class ReadFile {
    public RetVal<Properties> readPropertiesFile(File file) {
        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8);) {
            final Properties ret = new Properties();
            ret.load(reader);
            return RetVal.ok(ret);
        } catch (final IOException e) {
            return RetVal.fromProblem(FileProblem.from(file, e));
        }
    }
}
