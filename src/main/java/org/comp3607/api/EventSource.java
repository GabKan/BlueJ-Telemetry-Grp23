package org.comp3607.api;

import java.util.List;

import org.comp3607.model.*;

public interface EventSource {
    List<TelemetryEvent> snapshot();

    SessionMetadata metadata();
}
