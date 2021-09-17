# java-retval

## How to contribute to the project

You can contribute by writing up bugs, writing documentation, or helping out with the development effort.


### Developers

To develop the library, you'll need to fork the repository and submit changes back to the main project.  All changes you make will need to first have a good build.


#### What's Left To Implement

* Fill in the remaining placeholder functions.
* Add in JavaDoc everywhere.  This is partially done.
* Get up to 100% code coverage in tests.
* Add in Closeable support for the `ret` values.  This will require careful use of the `CloseableCollection`.
* API must be closer examined to ensure proper use cases for each top-level class and method.


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
   1. Ensure the the [CHANGES.md]() file contains all appropriate updates.
   2. Run `./gradlew javadoc` and put the generated documentation into the [docs]() directory, under a sub-directory named after the version to release (`mkdir docs/2.2.3 && cp -R build/docs/javadoc/* docs/2.2.3/.`).
   3. Update the [docs/index.md]() file with the new version.
   4. Add, commit, and push.
3. Merge the `dev` branch into `main`, then commit and push.  Make sure GitHub activity builds are good.
4. Locally run the build out of the `main` branch (`./gradlew clean build`).  *TODO setup the activity to publish the jar*
5. Publish the jar as a Github release artifact.
6. Publish the jar to the maven repo.
