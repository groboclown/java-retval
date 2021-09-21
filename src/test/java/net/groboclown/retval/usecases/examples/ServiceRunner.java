// Released under the MIT License. 
package net.groboclown.retval.usecases.examples;

import net.groboclown.retval.RetVal;
import net.groboclown.retval.problems.LocalizedProblem;

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

    // ------------------------------------------------------
    // Ignored from docs.

    static class Configuration {
        static Configuration loadDaemon() {
            throw new IllegalStateException("no implementation");
        }
        
        static Configuration loadActive() {
            throw new IllegalStateException("no implementation");
        }
    }
}