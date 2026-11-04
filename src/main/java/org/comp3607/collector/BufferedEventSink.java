package org.comp3607.collector;

import java.util.ArrayList;
import java.util.List;

import org.comp3607.api.EventSink;
import org.comp3607.api.EventSource;
import org.comp3607.model.SessionMetadata;
import org.comp3607.model.TelemetryEvent;

public class BufferedEventSink implements EventSink, EventSource {
    private final List<TelemetryEvent> events = new ArrayList<>();
    private SessionMetadata metadata;

    public BufferedEventSink(SessionMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public void accept(TelemetryEvent event) {
        events.add(event);
    }

    @Override
    public List<TelemetryEvent> snapshot() {
        return new ArrayList<>(events);
    }

    @Override
    public SessionMetadata metadata() {
        return metadata;
    }

    public void updateMetadata(SessionMetadata updatedMetadata) {
        metadata = updatedMetadata;
    }
}
