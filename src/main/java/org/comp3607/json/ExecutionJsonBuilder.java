package org.comp3607.json;

import java.util.HashMap;
import java.util.Map;

import org.comp3607.api.EventSource;
import org.comp3607.model.EventType;
import org.comp3607.model.TelemetryEvent;

public class ExecutionJsonBuilder {

    private static final double ROUND = 10000.0;

    public Map<String, Object> build(EventSource source) {
        int compiles = 0;
        int invocations = 0;

        for (TelemetryEvent event : source.snapshot()) {
            if (event.getType() == EventType.COMPILE_START) {
                compiles++;
            } else if (event.getType() == EventType.EXECUTION) {
                invocations++;
            }
        }

        double ratio = invocations == 0 ? 0.0 : (double) compiles / invocations;

        Map<String, Object> execution = new HashMap<>();
        execution.put("total_invocations", invocations);
        execution.put("compile_to_run_ratio", Math.round(ratio * ROUND) / ROUND);
        return execution;
    }
}
