package org.comp3607.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import org.comp3607.model.EventType;
import org.comp3607.model.TelemetryEvent;
import org.junit.jupiter.api.Test;

public class ActiveIdleTimeAggregatorTest {

    private final ActiveIdleTimeAggregator aggregator = new ActiveIdleTimeAggregator();

    private TelemetryEvent makeEvent(long time) {
        Map<String, Object> data = new HashMap<String, Object>();
        return new TelemetryEvent(EventType.EXECUTION, time, time, data);
    }

    private long active(Map<String, Long> times) {
        return times.get(ActiveIdleTimeAggregator.ACTIVE_KEY);
    }

    private long idle(Map<String, Long> times) {
        return times.get(ActiveIdleTimeAggregator.IDLE_KEY);
    }

    @Test
    public void activeIdle_shortAndLongGap() {
        List<TelemetryEvent> events = new ArrayList<TelemetryEvent>();
        events.add(makeEvent(0));
        events.add(makeEvent(30000));
        events.add(makeEvent(100000));

        Map<String, Long> times = aggregator.calculate(events);

        assertEquals(30000, active(times));
        assertEquals(70000, idle(times));
    }

    @Test
    public void activeIdle_gapExactlyAtLimit() {
        List<TelemetryEvent> events = new ArrayList<TelemetryEvent>();
        events.add(makeEvent(0));
        events.add(makeEvent(ActiveIdleTimeAggregator.IDLE_LIMIT_MS));

        Map<String, Long> times = aggregator.calculate(events);

        assertEquals(ActiveIdleTimeAggregator.IDLE_LIMIT_MS, active(times));
        assertEquals(0, idle(times));
    }

    @Test
    public void activeIdle_gapOneOverLimit() {
        List<TelemetryEvent> events = new ArrayList<TelemetryEvent>();
        events.add(makeEvent(0));
        events.add(makeEvent(ActiveIdleTimeAggregator.IDLE_LIMIT_MS + 1));

        Map<String, Long> times = aggregator.calculate(events);

        assertEquals(0, active(times));
        assertEquals(ActiveIdleTimeAggregator.IDLE_LIMIT_MS + 1, idle(times));
    }

    @Test
    public void activeIdle_noEvents() {
        List<TelemetryEvent> events = new ArrayList<TelemetryEvent>();

        Map<String, Long> times = aggregator.calculate(events);

        assertEquals(0, active(times));
        assertEquals(0, idle(times));
    }

    @Test
    public void activeIdle_oneEvent() {
        List<TelemetryEvent> events = new ArrayList<TelemetryEvent>();
        events.add(makeEvent(5000));

        Map<String, Long> times = aggregator.calculate(events);

        assertEquals(0, active(times));
        assertEquals(0, idle(times));
    }
}