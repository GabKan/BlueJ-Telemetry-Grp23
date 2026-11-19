package org.comp3607.json;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.comp3607.api.EventSource;
import org.comp3607.model.EventType;
import org.comp3607.model.TelemetryEvent;

/**
 * Builds the compilation section.
 */
public class CompilationJsonBuilder implements SectionBuilder {

    private static final double ROUND = 10000.0;
    private static final int MAX_NAME_LENGTH = 45;

    @Override
    public String sectionName() {
        return "compilation";
    }

    /**
     * Compile counts success rate and errors grouped by type.
     */
    @Override
    public Map<String, Object> build(EventSource source) {
        int total = 0;
        int failed = 0;
        int totalErrors = 0;
        Map<String, Object> errorsByType = new LinkedHashMap<>();

        for (TelemetryEvent event : source.snapshot()) {
            if (event.getType() != EventType.COMPILE_FINISH) {
                continue;
            }

            total++;
            Map<String, Object> data = event.getData();
            boolean success = Boolean.TRUE.equals(data.get("success"));
            if (!success) {
                failed++;
            }

            Object errors = data.get("errors");
            if (errors instanceof List<?>) {
                for (Object error : (List<?>) errors) {
                    String name = errorName(error);
                    totalErrors++;
                    int count = errorsByType.containsKey(name)
                            ? ((Number) errorsByType.get(name)).intValue()
                            : 0;
                    errorsByType.put(name, count + 1);
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

    /**
     * Uses the error's message if it has one, otherwise its string form.
     */
    private String errorName(Object error) {
        if (error instanceof Map<?, ?>) {
            Object message = ((Map<?, ?>) error).get("message");
            if (message != null) {
                return cleanName(String.valueOf(message));
            }
        }
        return cleanName(String.valueOf(error));
    }

    /**
     * Cuts the name at the first line break, swaps pipes for slashes, and caps the length.
     */
    private String cleanName(String name) {
        String cleaned = name;
        int lineBreak = cleaned.indexOf("\n");
        if (lineBreak >= 0) {
            cleaned = cleaned.substring(0, lineBreak);
        }
        cleaned = cleaned.replace("|", "/").trim();
        if (cleaned.length() > MAX_NAME_LENGTH) {
            cleaned = cleaned.substring(0, MAX_NAME_LENGTH) + "...";
        }
        return cleaned;
    }
}
