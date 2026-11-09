package org.comp3607.collector;

import java.util.*;

import org.comp3607.api.*;
import org.comp3607.model.*;

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
