// Released under the MIT License. 
package net.groboclown.retval.usecases.examples;

import java.io.File;
import javax.xml.crypto.Data;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.RetVoid;

class DataStore {
    private MyData myData;

    static RetVal<MyData> readData(File source) {
        // ...
        throw new IllegalStateException("not implemented");
    }

    RetVoid processData_poorlyThoughtOut(File source) {
        RetVal<MyData> res = readData(source);
        if (res.isOk()) {
            myData = res.result();
            return RetVoid.ok();
        }
        return res.thenVoid((x) -> {});
    }

    RetVoid processData_better(File source) {
        return readData(source)
                .thenVoid((value) -> {
                    this.myData = value;
                });
    }

    static RetVal<DataStore> processData_evenBetter(File source) {
        // In this way, the myData field could be made final and taken
        // as the parameter to a private constructor.
        return readData(source)
                .map((value) -> new DataStore(value));
    }

    // ---------------------------------
    // Cut from the example

    DataStore() {

    }

    DataStore(MyData value) {
        this.myData = value;
    }

    static class MyData {

    }
}
