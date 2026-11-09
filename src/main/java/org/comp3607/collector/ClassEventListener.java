package org.comp3607.collector;

import java.util.*;

import org.comp3607.api.EventSink;
import org.comp3607.model.EventType;

public class ClassEventListener {
    private final EventSink eventSink;
    private final TelemetryEventFactory eventFactory;

    public ClassEventListener(EventSink eventSink, TelemetryEventFactory eventFactory) {
        this.eventSink = eventSink;
        this.eventFactory = eventFactory;
    }

    public void onClassDeleted(String className, long timestampMs, long sessionOffsetMs) {
        Map<String, Object> data = new HashMap<>();
        data.put("className", className);
        eventSink.accept(
            eventFactory.createClassEvent(
                EventType.CLASS_DELETED, timestampMs, sessionOffsetMs, data
            )
        );
    }

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
