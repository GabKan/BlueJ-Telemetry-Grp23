package org.comp3607.collector;

import java.util.Map;

import org.comp3607.model.EventType;
import org.comp3607.model.TelemetryEvent;

/**
 * Creates telemetry events while validating that each event category is
 * appropriate for its collection channel.
 */
public class TelemetryEventFactory {

    /**
     * Creates a factory for telemetry event objects.
     */
    public TelemetryEventFactory() {
    }

    /**
     * Creates a compilation event.
     *
     * @param type compile start or finish event type
     * @param timestampMs event time in milliseconds since the epoch
     * @param sessionOffsetMs elapsed milliseconds from the session start
     * @param data event-specific attributes
     * @return the created compile event
     * @throws IllegalArgumentException if {@code type} is not a compile event
     */
    public TelemetryEvent createCompileEvent(EventType type, long timestampMs, long sessionOffsetMs, Map<String, Object> data) {
        if (type != EventType.COMPILE_START && type != EventType.COMPILE_FINISH) {
            throw new IllegalArgumentException("Not a compilation event type: " + type);
        }
        return new TelemetryEvent(type, timestampMs, sessionOffsetMs, data);
    }

    /**
     * Creates an execution event.
     *
     * @param timestampMs event time in milliseconds since the epoch
     * @param sessionOffsetMs elapsed milliseconds from the session start
     * @param data execution-specific attributes
     * @return the created execution event
     */
    public TelemetryEvent createExecutionEvent(long timestampMs, long sessionOffsetMs, Map<String, Object> data) {
        return new TelemetryEvent(EventType.EXECUTION, timestampMs, sessionOffsetMs, data);
    }

    /**
     * Creates an edit-activity or idle-period event.
     *
     * @param type interaction event type
     * @param timestampMs event time in milliseconds since the epoch
     * @param sessionOffsetMs elapsed milliseconds from the session start
     * @param data interaction-specific attributes
     * @return the created interaction event
     * @throws IllegalArgumentException if {@code type} is not an interaction event
     */
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

    /**
     * Creates a class creation, deletion, or modification event.
     *
     * @param type class lifecycle event type
     * @param timestampMs event time in milliseconds since the epoch
     * @param sessionOffsetMs elapsed milliseconds from the session start
     * @param data class-specific attributes
     * @return the created class event
     * @throws IllegalArgumentException if {@code type} is not a class event
     */
    public TelemetryEvent createClassEvent(EventType type, long timestampMs, long sessionOffsetMs, Map<String, Object> data) {
        if (type != EventType.CLASS_CREATED
            && type != EventType.CLASS_DELETED
            && type != EventType.CLASS_MODIFIED) {
            throw new IllegalArgumentException("Unsupported class event type: " + type);
        }
        return new TelemetryEvent(type, timestampMs, sessionOffsetMs, data);
    }
}
