package org.comp3607.json;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.comp3607.api.EventSource;
import org.comp3607.model.EventType;
import org.comp3607.model.TelemetryEvent;

public class CompilationJsonBuilder {

    private static final double ROUND = 10000.0;

    public Map<String, Object> build(EventSource source) {
        int total = 0;
        int failed = 0;
        int totalErrors = 0;
        Map<String, Object> errorsByType = new LinkedHashMap<>();

        for (TelemetryEvent event : source.snapshot()) {
            if (event.getType() == EventType.COMPILE_START) {
                total++;
            } else if (event.getType() == EventType.COMPILE_FINISH) {
                Map<String, Object> data = event.getData();
                boolean success = Boolean.TRUE.equals(data.get("success"));
                if (!success) {
                    failed++;
                }
                Object errors = data.get("errors");
                if (errors instanceof List<?>) {
                    for (Object error : (List<?>) errors) {
                        if (!(error instanceof Map<?, ?>)) {
                            continue;
                        }
                        Object type = ((Map<?, ?>) error).get("type");
                        String name = type == null ? "unknown" : String.valueOf(type);
                        totalErrors++;
                        int count = errorsByType.containsKey(name)
                            ? ((Number) errorsByType.get(name)).intValue()
                            : 0;
                        errorsByType.put(name, count + 1);
                    }
                }
            }
        }

        double rate = total == 0 ? 0.0 : (double) (total - failed) / total;

        Map<String, Object> compilation = new HashMap<>();
        compilation.put("total_compilations", total);
        compilation.put("failed_compilations", failed);
        compilation.put("compile_success_rate", Math.round(rate * ROUND) / ROUND);
        compilation.put("total_errors", totalErrors);
        compilation.put("errors_by_type", errorsByType);
        return compilation;
    }
}
