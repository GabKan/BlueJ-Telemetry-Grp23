package org.comp3607.json;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.comp3607.collector.BufferedEventSink;
import org.comp3607.model.EventType;
import org.comp3607.model.SessionMetadata;
import org.comp3607.model.TelemetryEvent;
import org.junit.jupiter.api.Test;

class TelemetryJsonSerialiserTest {

    @Test
    void serialisesSessionAndFullEventDataAsJson() {
        SessionMetadata metadata = new SessionMetadata("session-1", "unknown", 100L, 300L);
        BufferedEventSink source = new BufferedEventSink(metadata);

        Map<String, Object> diagnostic = new HashMap<>();
        diagnostic.put("errorCode", null);
        diagnostic.put("lineNumber", 12);
        diagnostic.put("message", "Expected \"value\"\nnext line");

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("success", false);
        eventData.put("errors", List.of(diagnostic));
        source.accept(new TelemetryEvent(EventType.COMPILE_FINISH, 250L, 150L, eventData));

        String json = new TelemetryJsonSerialiser().serialise(source);

        assertTrue(json.contains("\"sessionId\": \"session-1\""));
        assertTrue(json.contains("\"durationMs\": 200"));
        assertTrue(json.contains("\"totalCompilationAttempts\": 1"));
        assertTrue(json.contains("\"type\": \"COMPILE_FINISH\""));
        assertTrue(json.contains("\"timestampMs\": 250"));
        assertTrue(json.contains("\"errorCode\": null"));
        assertTrue(json.contains("\"message\": \"Expected \\\"value\\\"\\nnext line\""));
    }

    @Test
    void serialisesAnEmptySessionWithAnEmptyEventsArray() {
        BufferedEventSink source = new BufferedEventSink(
            new SessionMetadata("empty", "unknown", 100L, 100L)
        );

        String json = new TelemetryJsonSerialiser().serialise(source);

        assertTrue(json.contains("\"totalCompilationAttempts\": 0"));
        assertTrue(json.contains("\"events\": []"));
    }
}
