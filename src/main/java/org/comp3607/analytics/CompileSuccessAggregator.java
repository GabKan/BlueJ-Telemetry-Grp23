package org.comp3607.analytics;

import java.util.List;

import org.comp3607.model.EventType;
import org.comp3607.model.TelemetryEvent;

//  Calculates the proportion of compiles that passed 

public class CompileSuccessAggregator {

    public Double calculate(List<TelemetryEvent> events) {
        int total = countTotal(events);
        if (total == 0) {
            return 0.0;
        }
        int passed = countPassed(events);
        double rate = (double) passed / total;
        return rate;
    }

    // Counts every COMPILE_FINISH event, passed or failed.

    public int countTotal(List<TelemetryEvent> events) {
        int total = 0;
        for (TelemetryEvent event : events) {
            if (event.getType() == EventType.COMPILE_FINISH) {
                total = total + 1;
            }
        }
        return total;
    }

    public int countPassed(List<TelemetryEvent> events) {
        int passed = 0;
        for (TelemetryEvent event : events) {
            if (event.getType() == EventType.COMPILE_FINISH && isSuccessful(event)) {
                passed = passed + 1;
            }
        }
        return passed;
    }

    private boolean isSuccessful(TelemetryEvent event) {
        Object value = event.getData().get("success");
        return String.valueOf(value).equals("true");
    }
}