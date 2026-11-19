package org.comp3607.collector;

import java.util.*;

import org.comp3607.api.EventSink;
import org.comp3607.model.EventType;

/**
 * Converts class lifecycle observations into class telemetry events.
 */
public class ClassEventListener {
    /** Destination that stores the generated class events. */
    private final EventSink eventSink;
    /** Factory that creates validated class event objects. */
    private final TelemetryEventFactory eventFactory;

    /**
     * Creates a listener that writes class events to the supplied sink.
     *
     * @param eventSink destination for generated events
     * @param eventFactory factory used to create class events
     */
    public ClassEventListener(EventSink eventSink, TelemetryEventFactory eventFactory) {
        this.eventSink = eventSink;
        this.eventFactory = eventFactory;
    }

    /**
     * Records a class inferred to have been created.
     *
     * @param className name of the created class
     * @param timestampMs event time in milliseconds since the epoch
     * @param sessionOffsetMs elapsed milliseconds from the session start
     */
    public void onClassCreated(String className, long timestampMs, long sessionOffsetMs) {
        Map<String, Object> data = new HashMap<>();
        data.put("className", className);
        eventSink.accept(
            eventFactory.createClassEvent(
                EventType.CLASS_CREATED, timestampMs, sessionOffsetMs, data
            )
        );
    }

    /**
     * Records removal of a class from the project.
     *
     * @param className name of the removed class
     * @param timestampMs event time in milliseconds since the epoch
     * @param sessionOffsetMs elapsed milliseconds from the session start
     */
    public void onClassDeleted(String className, long timestampMs, long sessionOffsetMs) {
        Map<String, Object> data = new HashMap<>();
        data.put("className", className);
        eventSink.accept(
            eventFactory.createClassEvent(
                EventType.CLASS_DELETED, timestampMs, sessionOffsetMs, data
            )
        );
    }

    /**
     * Records a class rename as a class-modification event.
     *
     * @param className new class name
     * @param oldName previous class name
     * @param timestampMs event time in milliseconds since the epoch
     * @param sessionOffsetMs elapsed milliseconds from the session start
     */
    public void onClassRenamed(
        String className,
        String oldName,
        long timestampMs,
        long sessionOffsetMs
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("className", className);
        data.put("oldName", oldName);
        eventSink.accept(
            eventFactory.createClassEvent(
                EventType.CLASS_MODIFIED, timestampMs, sessionOffsetMs, data
            )
        );
    }
}
