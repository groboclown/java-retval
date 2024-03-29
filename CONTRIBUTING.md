# java-retval

## How to contribute to the project

You can contribute by writing up bugs, writing documentation, or helping out with the development effort.


### Bugs, Feature Requests, and Questions

The library uses [Github](https://github.com/groboclown/java-retval/issues) for issue tracking.  It should be used for tracking bugs and feature requests.

**Bugs**: Please include a description of the actions run against the library that caused the issue, the expected behavior (if anything beyond "should not cause an exception"), and the actual behavior.  The *best* bug reports include a test case exposing the bad behavior.

**Feature Requests**: Attempt to have a summary of the requested behavior and a solid use case for how the new behavior would be used by users of the library.

**Questions**:  Questions should have the subject line of the issue be in the form of a question.


### Developers

To develop the library, you'll need to fork the repository and submit changes back to the main project.  All changes you make will need to first have a good build.


#### Code Style Guide

The library uses a style check based off of the Google standard.  There are more local variations that, though they conform to the standard, don't look like normal code.

Each `usecase` test must include a `package-info.java` file to describe the purpose of the use case.  They should also be described in this file.

Each primary end-user class must provide an explanation of the use case for the class in the JavaDoc.  This will also aid developers looking to understand what additional methods should be applicable to the class.

The wordage in the library expressly calls the invalid values "problems" and not errors.  This is intentional, and should be used throughout the code.

The library strives for 100% code coverage with its tests.  That shouldn't be a goal, but a *starting point*.  Our tests are intended for covering expected use cases (or use case violations), not for covering code.  If tests are just to bump up the code coverage, then that test should be examined for whether it's covering a valid use case, and the covered code may need to be removed.  If there's code that's so complex to make 100% coverage difficult, then the code should be re-examined for whether it covers a valid use case.


#### Process for deploying releases

For the person performing the release:

1. Prepare all the files for release into the `dev` branch.  Ensure the Github activity builds complete without error.
2. Prepare documentation:
   1. Ensure the [CHANGES.md](CHANGES.md) file contains all appropriate updates.
   2. If this is a major or minor version change (not patch):
      1. Run `./gradlew javadoc` and put the generated documentation into the [docs](docs) directory, under a sub-directory named after the version to release (`mkdir docs/12.2 && cp -R build/docs/javadoc/* docs/12.2/.`).
      2. Update the [docs/index.md](docs/index.md) file with a new link in the JavaDoc list.
   3. Update the [docs/index.md](docs/index.md) file with the new version in the "importing into your project" guide.
   4. Update the [README.md](README.md) file with the new version in the "importing into your project" guide.
   5. Add, commit, and push.
3. Merge the `dev` branch into `main`, then commit and push.  Make sure GitHub activity builds are good.
4. Locally run the build out of the `main` branch (`./gradlew clean build`).
5. Publish the jar as a Github release artifact.
6. Publish the jar to the maven repo.
   ```bash
   ./gradlew publish \
       -Psigning.gnupg.keyName=(gpg short key ID) \
       -Psigning.gnupg.executable=gpg \
       -Psigning.gnupg.passphrase=(gpg key passphrase) \
       -PossrhUsername=(OSSRH username) \
       -PossrhPassword=(OSSRH password)
   ```
   ... or use a `GRADLE_USER_HOME` gradle.properties containing those `-P(key)=(value)` settings.
