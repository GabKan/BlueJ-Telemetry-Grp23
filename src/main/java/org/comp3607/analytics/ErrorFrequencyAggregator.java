package org.comp3607.analytics;

import java.util.*;
import org.comp3607.model.EventType;
import org.comp3607.model.TelemetryEvent;


// Counts compilation errors by error type

public class ErrorFrequencyAggregator {

    // Returns a map of error type to how many times it happened 

    public Map<String, Long> calculate(List<TelemetryEvent> events) {
        Map<String, Long> counts = new HashMap<String, Long>();

        for (TelemetryEvent event : events) {
            if (event.getType() == EventType.COMPILE_FINISH) {
                addErrorsFromEvent(event, counts);
            }
        }
        return counts;
    }

    private void addErrorsFromEvent(TelemetryEvent event, Map<String, Long> counts) {
        Object errorsValue = event.getData().get("errors");
        if (!(errorsValue instanceof List)) {
            return;
        }

        List errorList = (List) errorsValue;
        for (Object error : errorList) {
            addOne(counts, String.valueOf(error));
        }
    }

    private void addOne(Map<String, Long> counts, String type) {
        if (counts.containsKey(type)) {
            long oldCount = counts.get(type);
            long newCount = oldCount + 1;
            counts.put(type, newCount);
        } else {
            long firstCount = 1;
            counts.put(type, firstCount);
        }
    }
}