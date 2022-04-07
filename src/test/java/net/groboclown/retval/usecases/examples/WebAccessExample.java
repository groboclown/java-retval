// Released under the MIT License. 
package net.groboclown.retval.usecases.examples;

import java.io.File;
import java.net.URL;
import java.util.Objects;
import net.groboclown.retval.Ret;
import net.groboclown.retval.RetVal;

class WebAccessExample {
    class WebAccess {
        private String jwtToken;
        private URL url;

        public void setJwtToken(String jwtToken) {
            this.jwtToken = jwtToken;
        }

        public String requireJwtToken() {
            return Objects.requireNonNull(this.jwtToken);
        }

        public void setUrl(URL url) {
            this.url = url;
        }

        public URL requireUrl() {
            return Objects.requireNonNull(this.url);
        }
    }

    class WebRunner {
        public RetVal<String> loadWebPage(File settingsDir) {
            return Ret.buildValue(new WebAccess())
                // The collector keeps gathering all the information,
                // even if the one before it encountered a problem.
                .with(createToken(settingsDir), WebAccess::setJwtToken)
                .with(readUrl(settingsDir), WebAccess::setUrl)

                // Evaluate the results into a valid object.
                // The code afterwards only runs if it has no discovered problems.
                .evaluate()
                .then((access) -> fetchUrl(access.requireUrl(), access.requireJwtToken()));
        }

        public RetVal<String> loadWebPageWrong(File settingsDir) {
            // In this model, the values are gathered without the POJO accessor,
            // but if the first data gatherer encounters an error, then the second
            // one is never run.  That makes for a quicker run time, but the end-user
            // will not know about that problem until the first one is fixed.
            return
                    createToken(settingsDir)
                .then((token) ->
                    readUrl(settingsDir)
                .then((url) ->
                    fetchUrl(url, token)));
        }

        public RetVal<String> createToken(File settingsDir) {
            // ...
            throw new IllegalStateException("not implemented");
        }

        public RetVal<URL> readUrl(File settingsDir) {
            // ...
            throw new IllegalStateException("not implemented");
        }

        public RetVal<String> fetchUrl(URL url, String jwtToken) {
            // ...
            throw new IllegalStateException("not implemented");
        }
    }
}
