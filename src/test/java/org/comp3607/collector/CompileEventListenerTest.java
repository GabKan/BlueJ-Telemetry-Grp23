package org.comp3607.collector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.comp3607.model.EventType;
import org.comp3607.model.SessionMetadata;
import org.comp3607.model.TelemetryEvent;
import org.junit.jupiter.api.Test;

class CompileEventListenerTest {
    @Test
    void mapsCompileStartToAnEventWithClassNameAndTiming() {
        BufferedEventSink eventSink = newEventSink();
        CompileEventListener listener = new CompileEventListener(
            eventSink, new TelemetryEventFactory()
        );

        listener.onCompileStarted("Example", 1_000L, 200L);

        TelemetryEvent event = eventSink.snapshot().get(0);
        assertEquals(EventType.COMPILE_START, event.getType());
        assertEquals(1_000L, event.getTimestampMs());
        assertEquals(200L, event.getSessionOffsetMs());
        assertEquals(Map.of("className", "Example"), event.getData());
    }

    @Test
    void mapsCompileFinishToSuccessAndStructuredErrors() {
        BufferedEventSink eventSink = newEventSink();
        CompileEventListener listener = new CompileEventListener(
            eventSink, new TelemetryEventFactory()
        );
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", null);
        error.put("message", "Missing return statement");
        error.put("lineNumber", 12);
        List<Map<String, Object>> errors = new ArrayList<>(List.of(error));

        listener.onCompileFinished(false, errors, 1_500L, 700L);
        errors.clear();

        TelemetryEvent event = eventSink.snapshot().get(0);
        assertEquals(EventType.COMPILE_FINISH, event.getType());
        assertEquals(1_500L, event.getTimestampMs());
        assertEquals(700L, event.getSessionOffsetMs());
        Map<String, Object> data = event.getData();
        assertFalse((Boolean) data.get("success"));
        assertEquals(List.of(error), data.get("errors"));
    }

    @Test
    void mapsSuccessfulCompileFinishWithEmptyErrors() {
        BufferedEventSink eventSink = newEventSink();
        CompileEventListener listener = new CompileEventListener(
            eventSink, new TelemetryEventFactory()
        );

        listener.onCompileFinished(true, List.of(), 2_000L, 900L);

        Map<String, Object> data = eventSink.snapshot().get(0).getData();
        assertTrue((Boolean) data.get("success"));
        assertEquals(List.of(), data.get("errors"));
    }

    private BufferedEventSink newEventSink() {
        return new BufferedEventSink(
            new SessionMetadata("session-1", "unknown", 800L, 0L)
        );
    }
}
