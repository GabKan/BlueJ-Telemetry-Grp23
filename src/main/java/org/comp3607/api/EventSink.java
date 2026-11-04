package org.comp3607.api;

import org.comp3607.model.TelemetryEvent;

public interface EventSink {
    void accept(TelemetryEvent event);
}
