package org.comp3607.collector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.comp3607.model.EventType;
import org.comp3607.model.SessionMetadata;
import org.comp3607.model.TelemetryEvent;
import org.junit.jupiter.api.Test;

class BufferedEventSinkTest {
    @Test
    void acceptsEventsAndReturnsAnIndependentSnapshot() {
        SessionMetadata metadata = new SessionMetadata("session-1", "student-1", 100L, 0L);
        BufferedEventSink eventSink = new BufferedEventSink(metadata);
        TelemetryEvent event = new TelemetryEvent(
            EventType.COMPILE_START, 110L, 10L, Map.of("className", "Example")
        );

        eventSink.accept(event);

        List<TelemetryEvent> snapshot = eventSink.snapshot();
        assertEquals(List.of(event), snapshot);
        assertNotSame(snapshot, eventSink.snapshot());
        snapshot.clear();
        assertEquals(List.of(event), eventSink.snapshot());
        assertEquals(metadata, eventSink.metadata());
    }

    @Test
    void updatesSessionMetadataWithoutChangingStoredEvents() {
        BufferedEventSink eventSink = new BufferedEventSink(
            new SessionMetadata("session-1", "student-1", 100L, 0L)
        );
        TelemetryEvent event = new TelemetryEvent(
            EventType.COMPILE_FINISH, 150L, 50L, Map.of("success", true)
        );
        eventSink.accept(event);

        SessionMetadata updatedMetadata = new SessionMetadata(
            "session-1", "student-1", 100L, 200L
        );
        eventSink.updateMetadata(updatedMetadata);

        assertEquals(updatedMetadata, eventSink.metadata());
        assertEquals("session-1", eventSink.metadata().getSessionId());
        assertEquals("student-1", eventSink.metadata().getStudentId());
        assertEquals(100L, eventSink.metadata().getStartMs());
        assertEquals(200L, eventSink.metadata().getEndMs());
        assertEquals(List.of(event), eventSink.snapshot());
    }

    @Test
    void telemetryEventCopiesItsDataMapOnInputAndOutput() {
        Map<String, Object> initialData = new HashMap<>();
        initialData.put("success", true);
        TelemetryEvent event = new TelemetryEvent(
            EventType.COMPILE_FINISH, 150L, 50L, initialData
        );
        initialData.put("success", false);

        Map<String, Object> returnedData = event.getData();
        returnedData.put("success", false);

        assertEquals(true, event.getData().get("success"));
        assertEquals(EventType.COMPILE_FINISH, event.getType());
        assertEquals(150L, event.getTimestampMs());
        assertEquals(50L, event.getSessionOffsetMs());
    }
}
