package org.comp3607.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import org.comp3607.model.EventType;
import org.comp3607.model.TelemetryEvent;
import org.junit.jupiter.api.Test;

public class ClassTimelineAggregatorTest {

    private final ClassTimelineAggregator aggregator = new ClassTimelineAggregator();

    private TelemetryEvent makeClassEvent(EventType type, long time, String className) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("className", className);
        return new TelemetryEvent(type, time, time, data);
    }

    private TelemetryEvent makeExecution(long time) {
        Map<String, Object> data = new HashMap<String, Object>();
        return new TelemetryEvent(EventType.EXECUTION, time, time, data);
    }

    @Test
    public void timeline_classEvents() {
        List<TelemetryEvent> events = new ArrayList<TelemetryEvent>();
        events.add(makeClassEvent(EventType.CLASS_CREATED, 5000, "Dog"));
        events.add(makeClassEvent(EventType.CLASS_DELETED, 65000, "Cat"));
        events.add(makeClassEvent(EventType.CLASS_MODIFIED, 125000, "Dog"));

        List<String> timeline = aggregator.calculate(events);

        assertEquals(3, timeline.size());
        assertEquals("0m 05s - Created Dog", timeline.get(0));
        assertEquals("1m 05s - Deleted Cat", timeline.get(1));
        assertEquals("2m 05s - Modified Dog", timeline.get(2));
    }

    @Test
    public void timeline_otherEventTypes() {
        List<TelemetryEvent> events = new ArrayList<TelemetryEvent>();
        events.add(makeExecution(1000));

        List<String> timeline = aggregator.calculate(events);

        assertEquals(0, timeline.size());
    }

    @Test
    public void timeline_noEvents() {
        List<TelemetryEvent> events = new ArrayList<TelemetryEvent>();

        List<String> timeline = aggregator.calculate(events);

        assertEquals(0, timeline.size());
    }
}