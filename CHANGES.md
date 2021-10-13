# java-retval library Change Log

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


General Code Changes:

* Basic code format cleanup.
* Improved the example tests for `ConfigurationReaderTest.java`


## 1.0.0

Initial public offering.
