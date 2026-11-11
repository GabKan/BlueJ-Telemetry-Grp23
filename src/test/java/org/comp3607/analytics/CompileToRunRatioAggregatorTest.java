package org.comp3607.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import org.comp3607.model.EventType;
import org.comp3607.model.TelemetryEvent;
import org.junit.jupiter.api.Test;

public class CompileToRunRatioAggregatorTest {

    private final CompileToRunRatioAggregator aggregator = new CompileToRunRatioAggregator();
    private final List<String> noErrors = new ArrayList<String>();

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
    public void ratio_fourCompilesTwoRuns() {
        List<TelemetryEvent> events = new ArrayList<TelemetryEvent>();
        events.add(makeCompileFinish(1000, true, noErrors));
        events.add(makeCompileFinish(2000, true, noErrors));
        events.add(makeExecution(3000));
        events.add(makeCompileFinish(4000, true, noErrors));
        events.add(makeCompileFinish(5000, true, noErrors));
        events.add(makeExecution(6000));

        double ratio = aggregator.calculate(events);

        assertEquals(2.0, ratio, 0.0001);
        assertEquals(2, aggregator.countRuns(events));
    }

    @Test
    public void ratio_noRuns() {
        List<TelemetryEvent> events = new ArrayList<TelemetryEvent>();
        events.add(makeCompileFinish(1000, false, noErrors));

        double ratio = aggregator.calculate(events);

        assertEquals(0.0, ratio, 0.0001);
    }

    @Test
    public void ratio_noEvents() {
        List<TelemetryEvent> events = new ArrayList<TelemetryEvent>();

        double ratio = aggregator.calculate(events);

        assertEquals(0.0, ratio, 0.0001);
    }
}