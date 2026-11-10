package org.comp3607.analytics;

import java.util.List;

import org.comp3607.model.EventType;
import org.comp3607.model.TelemetryEvent;

// Calculates how many compiles the student did for every program run.

public class CompileToRunRatioAggregator {

    public Double calculate(List<TelemetryEvent> events) {
        int compiles = countType(events, EventType.COMPILE_FINISH);
        int runs = countType(events, EventType.EXECUTION);
        if (runs == 0) {
            return 0.0;
        }
        double ratio = (double) compiles / runs;
        return ratio;
    }

    // Counts the EXECUTION events
    
    public int countRuns(List<TelemetryEvent> events) {
        return countType(events, EventType.EXECUTION);
    }

    private int countType(List<TelemetryEvent> events, EventType type) {
        int count = 0;
        for (TelemetryEvent event : events) {
            if (event.getType() == type) {
                count = count + 1;
            }
        }
        return count;
    }
}