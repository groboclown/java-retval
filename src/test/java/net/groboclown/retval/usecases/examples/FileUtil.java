// Released under the MIT License. 
package net.groboclown.retval.usecases.examples;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import net.groboclown.retval.Ret;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.function.NonnullFunction;
import net.groboclown.retval.problems.FileProblem;

class FileUtil {
    static <T> RetVal<T> processContents(File file, Function<String, T> func) {
        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            return RetVal.ok(func.apply(readFully(file.getPath(), reader)));
        } catch (IOException e) {
            return RetVal.fromProblem(FileProblem.from(file, e));
        }
    }

    static String readFully(final String sourceName, final Reader reader) throws IOException {
        // ...
        throw new IllegalStateException("not implemented here");
    }
}
