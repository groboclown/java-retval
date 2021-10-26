# java-retval library Change Log


## 2.1.0

API Changes:

* `RetVal`, `RetNullable`, and `WarningVal` now also extend from the `ValuedProblemContainer`, which makes for creating user functions that take problem + value objects easier to make; you don't need to have multiple implementations.
* `ProblemCollector`:
  * Added `completeVoid()`
  * Added `withAll()` for `RetVoid`
  * Added `with()` for `WarningVal`.
  * Added `add()` and `addAll()`.
  * Added `withValuedProblemContainer()`.
* `ValueAccumulator`:
  * Added `asRetValList()`
  * Added `getCollector()`
  * Added `withAll()`
  * Added `withAllValues()`
  * Added `add()` and `addAll()`
  * Added `addValue()` and `addAllValues()`
* Added `consume(Consumable)` and `produceVoid(Function)` to `RetVal` and `RetNullable`.  These handle very specific use cases where the normal `thenVoid()` call would cause an ambiguous compile error; specifically, where the argument value is ignored and a simple lambda is invoked.


Documentation Improvements:

* Added more linking in JavaDoc.
* Added more JavaDoc in `ProblemCollector`.


Test Improvements:

* Added a collection of tests to play with the API to see how well the usage flows.


## 2.0.1

Bug Fixes:

* Changed the build to compile the class files as JDK 9 classes.  This required some under-the-cover Java API usage changes, but the code itself is still compatible with 2.0.0.
* Improved the debug monitor stack reporting to be compatible with more JDK implementations.  It used to have a fixed stack removal plan, but now performs a deeper inspection.


Documentation Changes:

* Clarified the `ReturnTypeFactory` documentation to describe the requirements for the input parameter in the create-with-problem calls.
* Moved from the official size from including per-patch release JavaDocs to a per minor / major versions.



## 2.0.0

Version 2 of the library supports API *compile compatible* end-user portions of the library.  Any standard use of the API should continue to work, but may need to be recompiled.


API Changes:

* The `Ret*` values are now interfaces, which means any extensions of those types now require interface usage rather than extends.  Other uses that may have performed Class level introspection will need work.
* This version introduces an extra level of extensibility by allowing for runtime replacement of the underlying `Ret*` value generation mechanism. 
* `RetVoid` now supports `forwardProblems`, `forwardNullableProblems`, and `forwardVoidProblems` for compatibility with the other `Ret*` values.
* `ObservedMonitor` is now an interface, and the API call to replace it has moved to the `ObservedMonitorRegistrar` class.
* `ValueBuilder` prefers a new method call, `evaluate()`, over the previous `then()` statement.  The original `then()` statement could lead to confusion with the usage.


Implementation Changes:

* The low-level observed monitor detection code has also moved location, but implementations shouldn't be directly using that.
* The `RetGenerator` class now handles calls to construct the `Ret*` instances.  This calls into an instance of `ReturnTypeFactory` to perform the actual construction.  The `ReturnTypeFactoryDetection` class handles determination of which factory to load at start time.  The return type factory can determine whether to use the active observed monitor.
* The logic to determine the observed monitor to load now lives in the `ObservedMonitorDetection` class.
* To support pluggable implementations of the `Ret*` classes, the unit tests are now published.  These include contract test classes for ensuring new implementations conform to the interface contracts and monitoring contracts.


Documentation Changes:

* Clarification on the `thenValidate` calls.
* General word usage clarifications.
* Updated `WebAccessExample` to reflect new `evaluate` API.
* Updated `DataStore` example to use a slightly better API.
* Updated `index.md` user guide:
  * Some general language and typo improvements.
  * Included a section for how to unit test code that uses the library.
  * Included a section for custom problem classes.
  * Changed the name of the `Smells` and `Leave No Check Unturned` sections.
* Improved JavaDoc descriptions for API calls in `MockObservedMonitor`.


## 1.0.0

Initial public offering.
