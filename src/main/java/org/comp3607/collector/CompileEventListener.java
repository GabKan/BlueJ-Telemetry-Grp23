package org.comp3607.collector;

import java.util.*;

import org.comp3607.api.EventSink;
import org.comp3607.model.EventType;

public class CompileEventListener {
    private final EventSink eventSink;
    private final TelemetryEventFactory eventFactory;

    public CompileEventListener(EventSink eventSink, TelemetryEventFactory eventFactory) {
        this.eventSink = eventSink;
        this.eventFactory = eventFactory;
    }

    public void onCompileStarted(String className, long timestampMs, long sessionOffsetMs) {
        Map<String, Object> data = new HashMap<>();
        data.put("className", className);
        eventSink.accept(
            eventFactory.createCompileEvent(
                EventType.COMPILE_START, timestampMs, sessionOffsetMs, data
            )
        );
    }

    public void onCompileFinished(
        boolean success,
        List<Map<String, Object>> errors,
        long timestampMs,
        long sessionOffsetMs
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("success", success);
        data.put("errors", new ArrayList<>(errors));
        eventSink.accept(
            eventFactory.createCompileEvent(
                EventType.COMPILE_FINISH, timestampMs, sessionOffsetMs, data
            )
        );
    }
}
