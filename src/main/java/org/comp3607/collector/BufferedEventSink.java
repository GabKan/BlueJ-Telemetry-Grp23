package org.comp3607.collector;

import java.util.*;

import org.comp3607.api.*;
import org.comp3607.model.*;

/**
 * In-memory implementation of both the event sink and event source contracts.
 * It collects events during a session and exposes snapshots for export and
 * report generation.
 */
public class BufferedEventSink implements EventSink, EventSource {
    /** Events accepted so far, in the order they were received. */
    private final List<TelemetryEvent> events = new ArrayList<>();
    /** Metadata describing the session associated with the collected events. */
    private SessionMetadata metadata;

    /**
     * Creates an empty event buffer associated with the supplied session
     * metadata.
     *
     * @param metadata metadata describing the session
     */
    public BufferedEventSink(SessionMetadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Appends an event to this buffer.
     *
     * @param event event to store
     */
    @Override
    public void accept(TelemetryEvent event) {
        events.add(event);
    }

    /**
     * Returns a new list containing the events currently in the buffer.
     *
     * @return a snapshot list in event-receipt order
     */
    @Override
    public List<TelemetryEvent> snapshot() {
        return new ArrayList<>(events);
    }

    /**
     * Returns the metadata currently associated with this buffer.
     *
     * @return session metadata
     */
    @Override
    public SessionMetadata metadata() {
        return metadata;
    }

    /**
     * Replaces the session metadata associated with this buffer.
     *
     * @param updatedMetadata new session metadata
     */
    public void updateMetadata(SessionMetadata updatedMetadata) {
        metadata = updatedMetadata;
    }
}
