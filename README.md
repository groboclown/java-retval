# retval

Library for Java 9+ programs to combine error messages and return values in a single return object, without using Exceptions.

[![dev branch build](https://github.com/groboclown/java-retval/actions/workflows/build.yaml/badge.svg?branch=dev)](https://github.com/groboclown/java-retval/actions/workflows/build.yaml) ![100% code coverage](https://img.shields.io/badge/coverage-100%25-yellow) [![license: MIT](https://img.shields.io/badge/license-MIT-brightgreen)](https://github.com/groboclown/java-retval)


## Why?

Sometimes, users want to know all the problems encountered by a program without going through the cycle of run, fix, run.  Sometimes, you want to write code that is error aware without using exceptions to handle well known problems states.

Exceptions have their use, and this isn't intended to replace them.  However, they are limited in what they can do, and generally can make code less user-friendly by showing stack traces instead of human-readable messages.


## Documentation

The full user guide is located at the [project page](https://groboclown.github.io/java-retval/).

If you want to contribute to the project, please see the [contributing guide](CONTRIBUTING.md).


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
