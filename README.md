# retval

Library for Java 9+ programs to combine error messages and return values in a single return object, without using Exceptions.

[![dev branch build](https://travis-ci.com/groboclown/java-retval.svg?branch=dev)](https://travis-ci.com/github/groboclown/java-retval) [![license: MIT](https://img.shields.io/badge/license-MIT-brightgreen)](https://github.com/groboclown/java-retval)


## Why?

Sometimes, users want to know all the problems encountered by a program without going through the cycle of run, fix, run.  Sometimes, you want to write code that is error aware without using exceptions to handle well known problems states.

Exceptions have their use, and this isn't intended to replace them.  However, they are limited in what they can do, and generally can make code less user-friendly by showing stack traces instead of human-readable messages.


## What It Offers

The library has 3 fundamental classes:

* `RetVoid` - a basic holder for problems.
* `RetVal` - contains problems or a non-null value.
* `RetNullable` - contains problems or a nullable value.

These `Ret*` classes contain either an error state or a possible value; they cannot contain both.  The auxiliary methods help to make the program flow easier to work with them.   These classes are acutely `null` aware by using the `javax.annotation.Nullable` and `javax.annotation.Nonnull` annotations, and giving names to the classes to distinguish them.  You may find development easier if you use an IDE that is `null` annotation aware.

Additionally, there are 3 related classes, `WarningVoid`, `WarningVal`, and `WarningNullable`, which have similar semantics to the `Ret*`, but allow for both containing problems and values.

The library uses a method naming convention:

* Static methods used as constructors start with `from`.  The `Ret*` classes have `ok` to create a non-problem value, `error` for adding problem values into the instance, and `errors` to combine multiple `Ret*` values with errors.
* Methods that collect information and return the called object start with `with`.  Sometimes, due to object immutability, this will return a different object, but the semantics of "collecting information" still apply.
* Methods that run an action in a parameter and return a different value start with `then`.
* Methods that return non-null value variations have no special suffix.
* Methods that return nullable value variations have a `Nullable` suffix.
* Methods that use a function or supplier argument which returns a raw value (not wrapped in a `Ret*`) have a `Value` suffix.
* Methods that return objects without a value have a `Void` suffix.
* Methods that perform an operation conditionally based upon the problem state on a `Runnable` argument take the `Run` suffix.


## How To Use

In all the basic `Ret*` types, the basic flow involves first collecting information that could have problems, then using that data, if it is problem-free, to perform other operations.

```java
class ServiceRunner {
    public static RetVal<ServiceRunner> loadService(String serviceMode) {
        return
            loadConfig(serviceMode)
            .then(ServiceRunner::new);
    }

    static RetVal<Configuration> loadConfig(String serviceMode) {
        if ("daemon".equals(serviceMode)) {
            return RetVal.ok(Configuration.loadDaemon());
        } else if ("active".equals(serviceMode)) {
            return RetVal.ok(Configuration.loadActive());
        } else {
            return RetVal.error(LocalizedProblem.from("Invalid service mode"));
        }
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
                .from(createToken(settingsDir), access::setJwtToken)
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

The [`usecases`](lib/src/test/java/net/groboclown/retval/usecases) package in the test directory contains some complete examples of different ways to use this library.

* [`readfile`](lib/src/test/java/net/groboclown/retval/usecases/readfile/package-info.java) shows converting exceptions into something the end user can consume.
* [`configuration`](lib/src/test/java/net/groboclown/retval/usecases/configuration/package-info.java) defines a complicated setup, where the configuration definition allows for a messy setup (the user doesn't need to make a 100% pristine file), and only validating what is directly requested.  Along with this, the configuration definition doesn't line up 1-to-1 with the data model.  This shows examples of `RetVal` and continuations, `RetCollector` to gather multiple problem groups, `ValueAccumulator` to gather a list of values, where each one may have a validation problem, and a `WarningVal` for maintaining a value in a partial state of construction, whose values can be useful for loading more data for problem inspections.


## Closable Values

Some situations may arise where the returned value must be closed, such as with an I/O type.  The simple approach is to require the closable value be passed as an argument to the creator.

```java
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Function;

class FileUtil {
    static <T> RetVal<T> processContents(File file, Function<String, T> func) {
        try (FileReader reader = new FileReader(file, "UTF-8")) {
            return func.apply(readFully(reader));
        } catch (IOException e) {
            return RetVal.error(FileProblem.from(file, e));
        }
    }
}
```


## Leave No Check Unturned

By default, the library will silently ignore Problems that haven't been checked.  Leaving these objects unchecked can be a source of subtle bugs, where problems that may occur in some configuration won't be passed on down stream.

However, you can set the environment variable `RETVAL_MONITOR_DEBUG` to `true` to enable logging when a tracked closeable or problem collection is garbage collected but not checked.  This allows for better inspection of where these problem areas may live.  Problems are sent to the Java logging mechanism's warning level, along with a stack trace for where the object was first created.


## Developing The Library

To develop the library, you'll need to fork the repository and submit changes back to the main project.  All changes you make will need to first have a good build.


### What's Left To Implement

* Fill in the remaining placeholder functions.
* Add in JavaDoc everywhere.  This is partially done.
* Get up to 100% code coverage in tests.
* Add in Closeable support for the `ret` values.  This will require careful use of the `CloseableCollection`.


### Code Style Guide

The library uses a style check based off of the Google standard.  There are more local variations that, though they conform to the standard, don't look like normal code.

Each `usecase` test must include a `package-info.java` file to describe the purpose of the use case.  They should also be described in this file.
