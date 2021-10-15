# java-retval library Change Log


## 2.0.0

Version 2 of the library supports API *compile compatible* end-user portions of the library.  Any standard use of the API should continue to work, but may need to be recompiled.


API Changes:

* The `Ret*` values are now interfaces, which means any extensions of those types now require interface usage rather than extends.  Other uses that may have performed Class level introspection will need work.
* This version introduces an extra level of extensibility by allowing for runtime replacement of the underlying `Ret*` value generation mechanism. 
* `RetVoid` now supports `forwardProblems`, `forwardNullableProblems`, and `forwardVoidProblems` for compatibility with the other `Ret*` values.


Implementation Changes:

* The low-level observed monitor detection code has also moved location, but implementations shouldn't be directly using that.
* The `RetGenerator` class now handles calls to construct the `Ret*` instances.  This calls into an instance of `ReturnTypeFactory` to perform the actual construction.  The `ReturnTypeFactoryDetection` class handles determination of which factory to load at start time.  The return type factory can determine whether to use the active observed monitor.
* The logic to determine the observed monitor to load now lives in the `ObservedMonitorDetection` class.
* To support pluggable implementations of the `Ret*` classes, the unit tests are now published.  These include contract test classes for ensuring new implementations conform to the interface contracts and monitoring contracts.


Documentation Changes:

* Clarification on the `thenValidate` calls.
* General word usage clarifications.


## 1.1.0

API Changes:

* `ValueBuilder` prefers a new method call, `evaluate()`, over the previous `then()` statement.  The original `then()` statement could lead to confusion with the usage.


Documentation Changes:

* Updated `WebAccessExample` to reflect new `evaluate` API.
* Updated `DataStore` example to use a slightly better API.
* Updated `index.md` user guide:
  * Some general language and typo improvements.
  * Included a section for how to unit test code that uses the library.
  * Included a section for custom problem classes.
  * Changed the name of the `Smells` and `Leave No Check Unturned` sections.
* Improved JavaDoc descriptions for API calls in `MockObservedMonitor`.
* Updated the `CONTRIBUTING.md` guide to include issue tracking.


General Code Changes:

* Basic code format cleanup.
* Improved the example tests for `ConfigurationReaderTest.java`


## 1.0.0

Initial public offering.
