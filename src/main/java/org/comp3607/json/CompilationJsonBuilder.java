package org.comp3607.json;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.comp3607.api.EventSource;
import org.comp3607.model.EventType;
import org.comp3607.model.TelemetryEvent;

public class CompilationJsonBuilder {

    public Map<String, Object> build(EventSource source) {
        List<TelemetryEvent> events = source.getEvents();

        int totalCompiles = 0;
        int passed = 0;
        int failed = 0;
        Map<String, Integer> errorsByType = new LinkedHashMap<>();

        for (TelemetryEvent event : events) {
            if (event.getType() == EventType.COMPILE_START) {
                totalCompiles++;
            } else if (event.getType() == EventType.COMPILE_FINISH) {
                boolean success = (boolean) event.getData().get("success");
                if (success) {
                    passed++;
                } else {
                    failed++;
                }

                List<Map<String, Object>> errors = (List<Map<String, Object>>) event.getData().get("errors");
                for (Map<String, Object> error : errors) {
                    String type = (String) error.get("type");
                    if (errorsByType.containsKey(type)) {
                        errorsByType.put(type, errorsByType.get(type) + 1);
                    } else {
                        errorsByType.put(type, 1);
                    }
                }
            }
        }

        int totalErrors = 0;
        for (int count : errorsByType.values()) {
            totalErrors += count;
        }

        double successRate = 0.0;
        if (totalCompiles > 0) {
            successRate = (double) passed / totalCompiles;
        }

        Map<String, Object> compilation = new LinkedHashMap<>();
        compilation.put("total_compilations", totalCompiles);
        compilation.put("failed_compilations", failed);
        compilation.put("compile_success_rate", Math.round(successRate * 10000.0) / 10000.0);
        compilation.put("total_errors", totalErrors);
        compilation.put("errors_by_type", errorsByType);
        return compilation;
    }
}
