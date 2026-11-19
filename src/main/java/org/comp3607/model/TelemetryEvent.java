package org.comp3607.model;

import java.util.*;

/**
 * Represents one timestamped telemetry occurrence and its event-specific
 * attributes.
 */
public class TelemetryEvent {
    /** Category identifying the kind of telemetry occurrence. */
    private final EventType type;
    /** Wall-clock event time in milliseconds since the epoch. */
    private final long timestampMs;
    /** Elapsed time in milliseconds from the beginning of the session. */
    private final long sessionOffsetMs;
    /** Event-specific attributes, stored in a defensive copy of the input map. */
    private final Map<String, Object> data;

    /**
     * Creates a telemetry event with the supplied type, timestamps, and
     * event-specific attributes.
     *
     * @param type category of the event
     * @param timestampMs wall-clock time in milliseconds since the epoch
     * @param sessionOffsetMs elapsed milliseconds from the session start
     * @param data event-specific attributes; the map itself is copied
     */
    public TelemetryEvent(EventType type, long timestampMs, long sessionOffsetMs, Map<String, Object> data) {
        this.type = type;
        this.timestampMs = timestampMs;
        this.sessionOffsetMs = sessionOffsetMs;
        this.data = new HashMap<>(data);
    }

    /**
     * Returns the category of this event.
     *
     * @return event category
     */
    public EventType getType() {
        return type;
    }

    /**
     * Returns the wall-clock time at which this event occurred.
     *
     * @return event time in milliseconds since the epoch
     */
    public long getTimestampMs() {
        return timestampMs;
    }

    /**
     * Returns this event's elapsed time from the beginning of its session.
     *
     * @return session-relative time in milliseconds
     */
    public long getSessionOffsetMs() {
        return sessionOffsetMs;
    }

    /**
     * Returns a copy of the event attributes map.
     *
     * @return a new map containing the event-specific attributes
     */
    public Map<String, Object> getData() {
        return new HashMap<>(data);
    }
}
