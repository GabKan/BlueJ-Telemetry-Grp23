package org.comp3607.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import org.comp3607.model.EventType;
import org.comp3607.model.TelemetryEvent;
import org.junit.jupiter.api.Test;

public class CompileSuccessAggregatorTest {

    private final CompileSuccessAggregator aggregator = new CompileSuccessAggregator();
    private final List<String> noErrors = new ArrayList<String>();
    private final List<String> oneError = Arrays.asList("missing_semicolon");

    private TelemetryEvent makeCompileFinish(long time, boolean success, List<String> errors) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("success", success);
        data.put("errors", errors);
        return new TelemetryEvent(EventType.COMPILE_FINISH, time, time, data);
    }

    private TelemetryEvent makeExecution(long time) {
        Map<String, Object> data = new HashMap<String, Object>();
        return new TelemetryEvent(EventType.EXECUTION, time, time, data);
    }

    @Test
    public void successRate_twoOutofThreePasses() {
        List<TelemetryEvent> events = new ArrayList<TelemetryEvent>();
        events.add(makeCompileFinish(1000, true, noErrors));
        events.add(makeCompileFinish(2000, true, noErrors));
        events.add(makeCompileFinish(3000, false, oneError));

        double rate = aggregator.calculate(events);

        assertEquals(0.6667, rate, 0.0001);
    }

    @Test
    public void successRate_noEvents() {
        List<TelemetryEvent> events = new ArrayList<TelemetryEvent>();

        double rate = aggregator.calculate(events);

        assertEquals(0.0, rate, 0.0001);
    }

    @Test
    public void successRate_noCompiles() {
        List<TelemetryEvent> events = new ArrayList<TelemetryEvent>();
        events.add(makeExecution(1000));

        double rate = aggregator.calculate(events);

        assertEquals(0.0, rate, 0.0001);
    }

    @Test
    public void successRate_allFailed() {
        List<TelemetryEvent> events = new ArrayList<TelemetryEvent>();
        events.add(makeCompileFinish(1000, false, oneError));
        events.add(makeCompileFinish(2000, false, oneError));

        double rate = aggregator.calculate(events);

        assertEquals(0.0, rate, 0.0001);
    }

    // Add this test in the commit that adds countTotal and countPassed.
    @Test
    public void counts_returnsTotalAndPassed() {
        List<TelemetryEvent> events = new ArrayList<TelemetryEvent>();
        events.add(makeCompileFinish(1000, true, noErrors));
        events.add(makeCompileFinish(2000, true, noErrors));
        events.add(makeCompileFinish(3000, false, oneError));

        assertEquals(3, aggregator.countTotal(events));
        assertEquals(2, aggregator.countPassed(events));
    }
}