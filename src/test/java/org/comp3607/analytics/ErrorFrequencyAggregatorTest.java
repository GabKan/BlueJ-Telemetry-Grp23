package org.comp3607.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import org.comp3607.model.EventType;
import org.comp3607.model.TelemetryEvent;
import org.junit.jupiter.api.Test;

public class ErrorFrequencyAggregatorTest {

    private final ErrorFrequencyAggregator aggregator = new ErrorFrequencyAggregator();

    private TelemetryEvent makeCompileFinish(long time, boolean success, List<String> errors) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("success", success);
        data.put("errors", errors);
        return new TelemetryEvent(EventType.COMPILE_FINISH, time, time, data);
    }

    private TelemetryEvent makeFailedCompile(String message) {
        Map<String, Object> error = new HashMap<String, Object>();
        error.put("message", message);
        List<Map<String, Object>> errors = new ArrayList<Map<String, Object>>();
        errors.add(error);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("success", false);
        data.put("errors", errors);
        return new TelemetryEvent(EventType.COMPILE_FINISH, 1000, 1000, data);
    }

    @Test
    public void errorFrequency_repeatedErrors() {
        List<TelemetryEvent> events = new ArrayList<TelemetryEvent>();
        events.add(makeCompileFinish(1000, false,
        Arrays.asList("missing_semicolon", "missing_semicolon")));
        events.add(makeCompileFinish(2000, false, Arrays.asList("undefined_variable")));
        events.add(makeCompileFinish(3000, true, new ArrayList<String>()));

        Map<String, Long> counts = aggregator.calculate(events);

        long semicolons = counts.get("missing_semicolon");
        long variables = counts.get("undefined_variable");
        assertEquals(2, counts.size());
        assertEquals(2, semicolons);
        assertEquals(1, variables);
    }

    @Test
    public void errorFrequency_noEvents() {
        List<TelemetryEvent> events = new ArrayList<TelemetryEvent>();

        Map<String, Long> counts = aggregator.calculate(events);

        assertEquals(0, counts.size());
    }

    @Test
    public void errorFrequency_onlyPassedCompiles() {
        List<TelemetryEvent> events = new ArrayList<TelemetryEvent>();
        events.add(makeCompileFinish(1000, true, new ArrayList<String>()));

        Map<String, Long> counts = aggregator.calculate(events);

        assertEquals(0, counts.size());
    }

    @Test
    public void errorFrequency_longMessages() {
        List<TelemetryEvent> events = new ArrayList<TelemetryEvent>();
        events.add(makeFailedCompile(
        "cannot find symbol\n  symbol:   variable y\n  location: class Hello"));
        events.add(makeFailedCompile(
        "cannot find symbol\n  symbol:   variable x\n  location: class Hello"));

        Map<String, Long> counts = aggregator.calculate(events);

        long symbols = counts.get("cannot find symbol");
        assertEquals(1, counts.size());
        assertEquals(2, symbols);
    }

    @Test
    public void errorFrequency_longName() {
        String longMessage = "unnamed classes are a preview feature and are disabled by default.";
        List<TelemetryEvent> events = new ArrayList<TelemetryEvent>();
        events.add(makeFailedCompile(longMessage));

        Map<String, Long> counts = aggregator.calculate(events);

        String cutName = longMessage.substring(0, 45) + "...";
        long count = counts.get(cutName);
        assertEquals(1, counts.size());
        assertEquals(1, count);
    }
}