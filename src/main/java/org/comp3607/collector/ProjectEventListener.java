package org.comp3607.collector;

import org.comp3607.api.EventSink;

public class ProjectEventListener {
    private final EventSink eventSink;
    private final TelemetryEventFactory eventFactory;

    public ProjectEventListener(EventSink eventSink, TelemetryEventFactory eventFactory) {
        this.eventSink = eventSink;
        this.eventFactory = eventFactory;
    }

    public void onProjectOpened(String projectName, long timestampMs, long sessionOffsetMs) {
        throw new UnsupportedOperationException("TODO Member A");
    }

    public void onProjectClosed(String projectName, long timestampMs, long sessionOffsetMs) {
        throw new UnsupportedOperationException("TODO Member A");
    }
}
