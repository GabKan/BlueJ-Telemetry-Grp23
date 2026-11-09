package org.comp3607.collector;

import java.util.Map;

import org.comp3607.model.*;

public class TelemetryEventFactory {

    public TelemetryEvent createProjectEvent(EventType type, long timestampMs, long sessionOffsetMs, Map<String, Object> data) {
        throw new UnsupportedOperationException("TODO Member A");
    }

    public TelemetryEvent createEditorEvent(EventType type, long timestampMs, long sessionOffsetMs, Map<String, Object> data) {
        throw new UnsupportedOperationException("TODO Member A");
    }

    public TelemetryEvent createCompileEvent(EventType type, long timestampMs, long sessionOffsetMs, Map<String, Object> data) {
        if (type != EventType.COMPILE_START && type != EventType.COMPILE_FINISH) {
            throw new IllegalArgumentException("Not a compilation event type: " + type);
        }
        return new TelemetryEvent(type, timestampMs, sessionOffsetMs, data);
    }

    public TelemetryEvent createExecutionEvent(long timestampMs, long sessionOffsetMs, Map<String, Object> data) {
        return new TelemetryEvent(EventType.EXECUTION, timestampMs, sessionOffsetMs, data);
    }

    public TelemetryEvent createInteractionEvent(
        EventType type,
        long timestampMs,
        long sessionOffsetMs,
        Map<String, Object> data
    ) {
        if (type != EventType.EDIT_ACTIVITY && type != EventType.IDLE_PERIOD) {
            throw new IllegalArgumentException("Unsupported interaction event type: " + type);
        }
        return new TelemetryEvent(type, timestampMs, sessionOffsetMs, data);
    }

    public TelemetryEvent createClassEvent(EventType type, long timestampMs, long sessionOffsetMs, Map<String, Object> data) {
        if (type != EventType.CLASS_CREATED
            && type != EventType.CLASS_DELETED
            && type != EventType.CLASS_MODIFIED) {
            throw new IllegalArgumentException("Unsupported class event type: " + type);
        }
        return new TelemetryEvent(type, timestampMs, sessionOffsetMs, data);
    }
}
