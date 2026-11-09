package org.comp3607.collector;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.comp3607.model.*;
import org.junit.jupiter.api.Test;

class BufferedEventSinkTest {
    private static final long SESSION_START_MS = 100L;
    private static final long FIRST_EVENT_TIME_MS = 110L;
    private static final long FIRST_EVENT_OFFSET_MS = 10L;
    private static final long SESSION_END_MS = 0L;

    @Test
    void acceptsEventsAndReturnsAnIndependentSnapshot() {
        SessionMetadata metadata = new SessionMetadata(
            "session-1", "student-1", SESSION_START_MS, SESSION_END_MS
        );
        BufferedEventSink sink = new BufferedEventSink(metadata);
        Map<String, Object> eventData = Map.of("className", "Example");
        TelemetryEvent event = new TelemetryEvent(
            EventType.COMPILE_START, FIRST_EVENT_TIME_MS, FIRST_EVENT_OFFSET_MS, eventData
        );
        sink.accept(event);
        List<TelemetryEvent> snapshot = sink.snapshot();
        assertEquals(List.of(event), snapshot);
        assertNotSame(snapshot, sink.snapshot());
        snapshot.clear();
        assertEquals(List.of(event), sink.snapshot());
        assertEquals(metadata, sink.metadata());
    }
}
