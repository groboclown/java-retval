# retval

Library for Java programs to combine error messages and return values in a single return object, without using Exceptions.

# Why?

Sometimes, users want to know all the problems encountered by a program without going through the cycle of run, fix, run.  Sometimes, you want to write code that is error aware without using exceptions to handle well known problems states.

Exceptions have their use, and this isn't intended to replace them.  However, they are limited in what they can do, and generally can make code less user-friendly by showing stack traces instead of human-readable messages.

# How To Use

The library has 3 basic types:

* `RetVoid` - a basic holder for problems.
* `RetVal` - contains problems or a non-null value.
* `RetNullable` - contains problems or a nullable value.

The types are acutely null aware by using the `javax.annotation.Nullable` and `javax.annotation.Nonnull` annotations.

In all these types, the basic flow involves first collecting information that could have problems, then using that data, if it is problem-free, to perform other operations.

```java
class ServiceRunner {
    public static RetVal<ServiceRunner> load(File configFile) {
        return
            readConfig(configFile)
            .then(ServiceRunner::new);
    }

    static RetVal<Configuration> readConfig(File configFile) {
        // ...
    }
    
    ServiceRunner(Configuration config) {
        // ...
    }
}
```

When dealing with a library that can generate exceptions, the library can encapsulate those as well:

```java
import java.io.FileReader;
import java.io.IOException;

abstract class ReadFile {
    public RetVal<String> readFile(File file) {
        try (FileReader reader = new FileReader(file)) {
            return RetVal.ok(readFully(reader));
        } catch (IOException e) {
            return RetVal.error(FileProblem.from(file, e));
        }
    }
    
    public abstract String readFully(FileReader reader) throws IOException;
}
```

If your program needs to handle many values before beginning the processing, then a `RetCollector` pattern can help.  The collector pattern helps avoid the tower of lambdas, and also correctly collects all the setup problems that the lambdas would otherwise skip.

```java
import java.net.URL;
import java.util.Objects;

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
        WebAccess access = new WebAccess();
        RetCollector
                // The collector keeps gathering all the information,
                // even if the one before it encountered a problem.
                .start(createToken(settingsDir), access::setJwtToken)
                .with(readUrl(settingsDir), access::setUrl)
                
                // The "then" evaluates the results only if they all ran successfully.
                .then(fetchUrl(access.requireUrl(), access.requireJwtToken()));
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
    }

    public RetVal<URL> readUrl(File settingsDir) {
        // ...
    }
    
    public RetVal<String> fetchUrl(URL url, String jwtToken) {
        // ...
    }
}
```
