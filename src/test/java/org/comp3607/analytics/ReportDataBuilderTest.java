package org.comp3607.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;
import org.comp3607.api.EventSource;
import org.comp3607.model.EventType;
import org.comp3607.model.SessionMetadata;
import org.comp3607.model.TelemetryEvent;
import org.junit.jupiter.api.Test;

public class ReportDataBuilderTest {

    private static class FakeEventSource implements EventSource {
        private final List<TelemetryEvent> events;

        FakeEventSource(List<TelemetryEvent> events) {
            this.events = events;
        }

        public List<TelemetryEvent> snapshot() {
            return events;
        }

        public SessionMetadata metadata() {
            return new SessionMetadata("test-session", "student-id", 0, 3000);
        }
    }

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

    private TelemetryEvent makeClassEvent(EventType type, long time, String className) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("className", className);
        return new TelemetryEvent(type, time, time, data);
    }

    private List<TelemetryEvent> mixedSession() {
        List<TelemetryEvent> events = new ArrayList<TelemetryEvent>();
        events.add(makeCompileFinish(0, false, Arrays.asList("missing_semicolon")));
        events.add(makeCompileFinish(1000, true, new ArrayList<String>()));
        events.add(makeExecution(2000));
        events.add(makeClassEvent(EventType.CLASS_CREATED, 3000, "Dog"));
        return events;
    }

    @Test
    public void build_mixedSession_Metrics() {
        ReportDataBuilder builder = new ReportDataBuilder();

        ReportData data = builder.build(new FakeEventSource(mixedSession()));

        assertEquals(0.5, data.getCompileSuccessRate(), 0.0001);
        assertEquals(1, data.getErrorFrequency().size());
        assertEquals(3000, data.getActiveTimeMs());
        assertEquals(0, data.getIdleTimeMs());
        assertEquals(2.0, data.getCompileToRunRatio(), 0.0001);
        assertEquals(1, data.getClassTimeline().size());
    }

    @Test
    public void build_mixedSession_CountsAndSessionId() {
        ReportDataBuilder builder = new ReportDataBuilder();

        ReportData data = builder.build(new FakeEventSource(mixedSession()));

        assertEquals("test-session", data.getSessionId());
        assertEquals(4, data.getEventCount());
        assertEquals(2, data.getTotalCompiles());
        assertEquals(1, data.getPassedCompiles());
        assertEquals(1, data.getFailedCompiles());
        assertEquals(1, data.getTotalRuns());
        assertFalse(data.isEmpty());
    }

    @Test
    public void build_emptySession() {
        ReportDataBuilder builder = new ReportDataBuilder();

        ReportData data = builder.build(new FakeEventSource(new ArrayList<TelemetryEvent>()));

        assertTrue(data.isEmpty());
        assertEquals(0.0, data.getCompileSuccessRate(), 0.0001);
        assertEquals(0, data.getErrorFrequency().size());
        assertEquals(0, data.getActiveTimeMs());
        assertEquals(0, data.getIdleTimeMs());
        assertEquals(0.0, data.getCompileToRunRatio(), 0.0001);
        assertEquals(0, data.getClassTimeline().size());
    }
}