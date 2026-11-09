package org.comp3607.model;

import java.util.*;

public class TelemetryEvent {
    private final EventType type;
    private final long timestampMs;
    private final long sessionOffsetMs;
    private final Map<String, Object> data;

    public TelemetryEvent(EventType type, long timestampMs, long sessionOffsetMs, Map<String, Object> data) {
        this.type = type;
        this.timestampMs = timestampMs;
        this.sessionOffsetMs = sessionOffsetMs;
        this.data = new HashMap<>(data);
    }

    public EventType getType() {
        return type;
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public long getSessionOffsetMs() {
        return sessionOffsetMs;
    }

    public Map<String, Object> getData() {
        return new HashMap<>(data);
    }
}
