package org.comp3607.collector;

import java.util.Map;

import org.comp3607.model.EventType;
import org.comp3607.model.TelemetryEvent;

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
        throw new UnsupportedOperationException("TODO Member A");
    }

    public TelemetryEvent createClassEvent(EventType type, long timestampMs, long sessionOffsetMs, Map<String, Object> data) {
        throw new UnsupportedOperationException("TODO Member A");
    }
}
