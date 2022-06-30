# Java `net.groboclown:retval` Library

Library for Java 9+ programs to combine error messages and return values in a single return object, without using Exceptions.

[Visit the project on GitHub.](https://github.com/groboclown/java-retval)


# Topics

* JavaDoc:
  * [2.2](2.2)
  * [2.1](2.1)
  * [2.0](2.0)
  * [1.0](1.0)
* External Links:
  * [Main Project Page](https://github.com/groboclown/java-retval)
  * [Known Issues](https://github.com/groboclown/java-retval/issues)
* User Guide
  * [Introduction for Users](#introduction-for-users)
  * [Importing into Your Project](#importing-into-your-project)
  * [How to Use](#how-to-use)
  * [Example Use Cases](#example-use-cases)
  * [Closeable Values](#closeable-values)
  * [Custom Problems](#custom-problems)
  * [Unit Tests](#unit-tests)
  * [Validating All Checks are Accounted For](#validating-all-checks-are-accounted-for)
  * [Identifying Code Smells](#identifying-code-smells)


# User Guide

## Introduction for Users

### Why?

Sometimes, users want to know all the problems encountered by a program without going through the cycle of run, fix, run.  Sometimes, you want to write code that is error aware without using exceptions to handle well known problem states.

Exceptions have their use, and this isn't intended to replace them.  However, they are limited in what they can do, and generally can make code less user-friendly by showing stack traces instead of human-readable messages.


### What This Library Offers

The library has these fundamental classes:

* `RetVoid` - a basic holder for problems.
* `RetVal` - contains problems or a non-null value (but not both).
* `RetNullable` - contains problems or a nullable value (but not both).
* `WarningVal` - contains both problems (possibly empty) and a non-null value.

These `Ret*` classes contain either an error state or a possible value; they cannot contain both.  The auxiliary methods help make the program flow easier to work with them.   These classes are acutely `null` aware by using the `javax.annotation.Nullable` and `javax.annotation.Nonnull` annotations, and giving names to the classes to distinguish them.  You may find development easier if you use an IDE that is `null` annotation aware.

Along with these, the library provides some helper utility classes:

* `Ret` - provides standard constructor functions for the utility classes, and help with `AutoCloseable` classes.
* `ValueAccumulator` - joins lists of values along with problems encountered while loading them.
* `ValueBuilder` - aids in the builder pattern in fault-tolerant ways.
* `ProblemCollector` - a bit like a list, but conforms to library standard APIs and makes working with problem containers easier.

On top of this, if you're running in a development environment, you can [turn on monitoring](#leave-no-check-unturned), which helps you understand where problems or values may be dropped out of the application.



## Importing into Your Project

Gradle projects will need to add the jar to the dependencies section:

```groovy
dependencies {
  implementation 'net.groboclown:retval:2.2.0'
}
```

Maven projects will need to include the runtime dependency:

```xml
   <dependency>
      <groupId>net.groboclown</groupId>
      <artifactId>retval</artifactId>
      <version>2.2.0</version>
      <scope>runtime</scope>
    </dependency>
```


## How To Use

In all the `Ret*` types, the normal flow involves first collecting information that could have problems, then using that data, if it is problem-free, to perform other operations.

<!-- src/test/java/net/groboclown/retval/usecases/examples/ServiceRunner.java -->
```java
class ServiceRunner {
  public static RetVal<ServiceRunner> loadService(String serviceMode) {
    return
            loadConfig(serviceMode)
                    .map(ServiceRunner::new);
  }

  static RetVal<Configuration> loadConfig(String serviceMode) {
    if ("daemon".equals(serviceMode)) {
      return RetVal.ok(Configuration.loadDaemon());
    } else if ("active".equals(serviceMode)) {
      return RetVal.ok(Configuration.loadActive());
    } else {
      return RetVal.fromProblem(LocalizedProblem.from("Invalid service mode"));
    }
  }

  ServiceRunner(Configuration config) {
    // ...
  }
}
```


When dealing with a library that can generate exceptions, the library can encapsulate those as well:

<!-- src/test/java/net/groboclown/retval/usecases/examples/ReadFile.java -->
```java
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

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
```

If your program needs to handle many values before beginning the processing, then a `ValueBuilder` pattern can help.  The value builder pattern helps avoid the tower of lambdas, and also correctly collects all the setup problems that the lambdas would otherwise skip.

<!-- src/test/java/net/groboclown/retval/usecases/examples/WebAccessExample.java -->
```java
import java.io.File;
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
  }

  public RetVal<URL> readUrl(File settingsDir) {
    // ...
  }

  public RetVal<String> fetchUrl(URL url, String jwtToken) {
    // ...
  }
}
```

## Example Use Cases

The [`usecases`](https://github.com/groboclown/java-retval/tree/dev/src/test/java/net/groboclown/retval/usecases) test package contains some complete examples of different ways to use this library.

* [`readfile`](https://github.com/groboclown/java-retval/tree/dev/src/test/java/net/groboclown/retval/usecases/readfile) shows converting exceptions into something the end user can consume.
* [`configuration`](https://github.com/groboclown/java-retval/tree/dev/src/test/java/net/groboclown/retval/usecases/configuration) defines a complicated setup, where the configuration definition allows for a messy setup (the user doesn't need to make a 100% pristine file), and only validating what is directly requested.  Along with this, the configuration definition doesn't line up 1-to-1 with the data model.  This shows examples of `RetVal` and continuations, `ValueBuilder` to gather multiple problem groups, `ValueAccumulator` to gather a list of values, where each one may have a validation problem, and a `WarningVal` for maintaining a value in a partial state of construction, whose values can be useful for loading more data for problem inspections.


## Closeable Values

Some situations may arise where the returned value must be closed, such as with an I/O type.  The simple approach is to require the closable value be passed as an argument to the creator.

<!-- src/test/java/net/groboclown/retval/usecases/examples/FileUtil.java -->

```java
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

class FileUtil {
  // One approach, 
  static <T> RetVal<T> processContents(File file, Function<String, T> func) {
    try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
      return RetVal.ok(func.apply(readFully(file.getPath(), reader)));
    } catch (IOException e) {
      return RetVal.fromProblem(FileProblem.from(file, e));
    }
  }

  static String readFully(final String sourceName, final Reader reader) throws IOException {
    // ...
  }
}
```

The `retval` library also includes helper functions to correctly and fully protect your application against incorrect close semantics that can easily creep into your program.

<!-- src/test/java/net/groboclown/retval/usecases/examples/FileUtilCloser.java -->

```java
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

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
      return RetVal.ok(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
    } catch (IOException e) {
      return RetVal.fromProblem(FileProblem.from(file, e));
    }
  }

  static RetVal<String> readFullyWrapped(final String sourceName, final Reader reader)
          throws IOException {
    // ...
  }
}
```

This "close" version of the example requires a bit more complexity to use.  However, it prevents some situations with exceptions that would otherwise cause error conditions to not be returned correctly.  In particular, error catching on the close statement after exception handling could cause lost information.


## Custom Problems

The library provides some simple problem classes to get you started.  However, a more refined problem definitions can make for clearer error reporting.

All problem classes must implement the `Problem` interface.  Because problems usually come from a user source, the additional interface `SourcedProblem` provides an example for adding additional information to the problem.  You may find that you want a more robust problem source, perhaps a complex structure that includes both a source file and a path to a location in a Json document.

The library doesn't put limitations on what your problem classes should be, but on the other hand, it doesn't provide much functionality for robust problems.  Future versions may include a larger variety of built-in problem classes.


## Unit Tests

To add unit tests around the `RetVal` classes, you will want to add in a `MockProblemMonitor` to ensure the code doesn't forget problems.

In JUnit 5, you can add code like this:

<!-- src/test/java/net/groboclown/retval/usecases/configuration/ConfigurationReaderTest.java -->
```java
class ConfigurationReaderTest {
  MockProblemMonitor monitor;

  @Test
  void test_readProjectUser_noProjectValues() {
    final Properties props = new Properties();
    props.setProperty("projects", "p1");
    final RetVal<List<ProjectUser>> res = ConfigurationReader.readProjectUsers("p1", props);
    assertEquals(
            List.of(
                    LocalizedProblem.from("no `project.p1.name` property"),
                    LocalizedProblem.from("no `project.p1.users` property"),
                    LocalizedProblem.from("no `project.p1.url` property")
            ),
            res.anyProblems()
    );
    assertEquals(
            List.of(),
            this.monitor.getNeverObserved()
    );
  }

  @Test
  void test_readProjectUser_ok() {
    final Properties props = new Properties();
    // setup properties correctly
    // ...
    
    // Run the code
    final RetVal<List<ProjectUser>> res = ConfigurationReader.readProjectUsers("p1", props);
    
    // Validate no errors.
    assertEquals(
            List.of(),
            res.anyProblems()
    );
    
    // Validate that no problems were dropped.
    assertEquals(
            List.of(),
            this.monitor.getNeverObserved()
    );

    // validate returned value
    final List<ProjectUser> projects = res.result();
    assertEquals(1, projects.size());
    // ...
  }

  @BeforeEach
  void beforeEach() {
    this.monitor = MockProblemMonitor.setup();
    // In all cases, we want tracing enabled to ensure we track observation status.
    this.monitor.traceEnabled = true;
  }

  @AfterEach
  void afterEach() {
    this.monitor.tearDown();
  }
  
}
```

This ensures that the list of problems matches the expected set.  This takes advantage of the [monitor](#leave-no-check-unturned) system, by keeping track of every registered problem during the execution of the test case.

The `beforeEach` method creates and configures the problem monitor for use by the test, and the `afterEach` restores the problem monitor to its previous state.

The failure state checks ensure the list of expected problems return from `RetVal.getProblems()`.  Tests can rely on deterministic ordering of the problems.

For success checks, it's more helpful to first check for an empty list of problems, rather than checking `isOk()` state first.  This allows the unit tests to report discovered problems in the error text, which helps in debugging failing tests.


## Validating All Checks are Accounted For

By default, the library will silently ignore Problems that haven't been checked.  Leaving these objects unchecked can be a source of subtle bugs, where problems that may occur in some configurations won't be passed on down stream.

However, you can set the environment variable or Java property `RETVAL_MONITOR_DEBUG` to `true` to enable logging when a tracked closeable or problem collection is garbage collected but not checked.  This allows for better inspection of where these problem areas may live.  Problems are sent to the Java logging mechanism's warning level, along with a stack trace for where the object was first created.

For production environments, you can set the environment variable or Java property `RETVAL_PRODUCTION` to `true`.  This will disable all monitoring capabilities, which enables optimizations that disables many checks, calls, and object allocations for the `Ret*` objects.

Under the covers, the code provides for two extension points - `Ret*` object creation and monitor implementation.  You can't control these directly through startup parameters, but your code can call static methods on the `RetGenerator` class and the `ObservedMonitorRegistrar` class.  See the API documentation for more details if you are interested in changing these out.  You can find examples of implementing and changing these mechanisms in the unit test helpers that come with the library.


## Identifying Code Smells

The API is carefully constructed to push you down a path that doesn't lose information.  If you find yourself performing identity transforms, using no-op consumers, or filling the code with "if" statements, then you should reconsider your code logic.

<!-- src/test/java/net/groboclown/retval/usecases/examples/DataStore.java -->
```java
class DataStore {
    private MyData myData;
    
    static RetVal<MyData> readData(File source) {
        // Example method for loading the MyData with the contents of the source file.
        // ...
    }
    
    RetVoid processData_poorlyThoughtOut(File source) {
        // This example runs using old-style Java that requires additional
        // path handling.  This pushes explicit error checking into the
        // readData caller.
        RetVal<MyData> res = readData(source);
        if (res.isOk()) {
            this.myData = res.result();
            return RetVoid.ok();
        }
        // res has a problem, so it can be directly forwarded.
        return res.forwardVoidProblems();
        
        // Returning as void would be even worse:
        // e.g.: return res.thenVoid((x) -> {});
    }
    
    RetVoid processData_better(File source) {
        return readData(source)
                // This one call will invoke the lambda function if the return value
                // from readData didn't cause problems.
                .thenVoid((value) -> { this.myData = value; });
    }
    
    static RetVal<DataStore> processData_evenBetter(File source) {
        // This particular version is an alternative to a constructor, and follows the
        // builder pattern better.  It hides the complexity around creating the DataStore
        // object + constructor invoking behind a single call.
        // In this way, the myData field could be made final.
        return readData(source)
                .map((value) -> new DataStore(value));
    }
}
```


Similarly, if you find your method uses combinations of `ValueBuilder` and `ProblemCollector`, then you may need to rethink things.
