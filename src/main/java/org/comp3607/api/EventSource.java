package org.comp3607.api;

import java.util.List;

import org.comp3607.model.*;

/**
 * Read-only access contract for session events and their metadata.
 */
public interface EventSource {
    /**
     * Returns the events currently available from this source.
     *
     * @return list of telemetry events
     */
    List<TelemetryEvent> snapshot();

    /**
     * Returns the metadata associated with the event session.
     *
     * @return session metadata
     */
    SessionMetadata metadata();
}
