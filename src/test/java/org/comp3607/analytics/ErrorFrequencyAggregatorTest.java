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
}