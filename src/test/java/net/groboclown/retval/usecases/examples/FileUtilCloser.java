// Released under the MIT License. 
package net.groboclown.retval.usecases.examples;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import net.groboclown.retval.Ret;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.function.NonnullFunction;
import net.groboclown.retval.problems.FileProblem;

class FileUtilCloser {
    static <T> RetVal<T> processContentsCloser(File file, NonnullFunction<String, T> func) {
        return openFile(file)
                .then((reader) ->
                    Ret.closeWith(reader, (r) ->
                        readFullyWrapped(file.getPath(), reader)))
                .map(func);
    }

    static RetVal<Reader> openFile(File file) {
        try {
            return RetVal.ok(new FileReader(file, StandardCharsets.UTF_8));
        } catch (IOException e) {
            return RetVal.fromProblem(FileProblem.from(file, e));
        }
    }

    static RetVal<String> readFullyWrapped(final String sourceName, final Reader reader)
            throws IOException {
        // ...
        throw new IllegalStateException("not implemented here");
    }
}
