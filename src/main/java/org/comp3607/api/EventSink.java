package org.comp3607.api;

import org.comp3607.model.TelemetryEvent;

/**
 * Destination contract for telemetry producers to submit events.
 */
public interface EventSink {
    /**
     * Accepts a telemetry event for storage or downstream processing.
     *
     * @param event event to submit
     */
    void accept(TelemetryEvent event);
}
