package org.comp3607.collector;

import org.comp3607.api.EventSink;

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
        String consoleSummary,
        long timestampMs,
        long sessionOffsetMs
    ) {
        throw new UnsupportedOperationException("TODO Member A");
    }
}
