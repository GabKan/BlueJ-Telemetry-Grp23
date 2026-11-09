package org.comp3607.collector;

import java.util.*;

import org.comp3607.api.EventSink;
import org.comp3607.model.EventType;

public class ExecutionEventListener {
    private final EventSink eventSink;
    private final TelemetryEventFactory eventFactory;

    public ExecutionEventListener(EventSink eventSink, TelemetryEventFactory eventFactory) {
        this.eventSink = eventSink;
        this.eventFactory = eventFactory;
    }

    public void onExecution(
        String className,
        String exception,
        long timestampMs,
        long sessionOffsetMs
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("className", className);
        data.put("exception", exception);
        eventSink.accept(
            eventFactory.createExecutionEvent(timestampMs, sessionOffsetMs, data)
        );
    }
}
