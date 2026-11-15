package org.comp3607.json;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.comp3607.api.EventSource;
import org.comp3607.model.EventType;
import org.comp3607.model.TelemetryEvent;

public class ExecutionJsonBuilder {

    private static final double ROUND = 10000.0;

    public Map<String, Object> build(EventSource source) {
        List<TelemetryEvent> events = source.getEvents();

        int compiles = 0;
        int runs = 0;

        for (TelemetryEvent event : events) {
            if (event.getType() == EventType.COMPILE_START) {
                compiles++;
            } else if (event.getType() == EventType.EXECUTION) {
                runs++;
            }
        }

        double ratio = 0.0;
        if (runs > 0) {
            ratio = (double) compiles / runs;
        }

        Map<String, Object> execution = new LinkedHashMap<>();
        execution.put("total_invocations", runs);
        execution.put("compile_to_run_ratio", Math.round(ratio * ROUND) / ROUND);
        return execution;
    }
}
