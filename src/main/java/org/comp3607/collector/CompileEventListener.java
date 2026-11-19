package org.comp3607.collector;

import java.util.*;

import org.comp3607.api.EventSink;
import org.comp3607.model.EventType;

/**
 * Converts normalized compilation callbacks into telemetry events and sends
 * them to the configured event sink.
 */
public class CompileEventListener {
    /** Destination that stores the generated compile events. */
    private final EventSink eventSink;
    /** Factory that creates validated compile event objects. */
    private final TelemetryEventFactory eventFactory;

    /**
     * Creates a listener that writes compile events to the supplied sink.
     *
     * @param eventSink destination for generated events
     * @param eventFactory factory used to create compile events
     */
    public CompileEventListener(EventSink eventSink, TelemetryEventFactory eventFactory) {
        this.eventSink = eventSink;
        this.eventFactory = eventFactory;
    }

    /**
     * Records the start of a compilation.
     *
     * @param className name of the source class being compiled
     * @param timestampMs callback time in milliseconds since the epoch
     * @param sessionOffsetMs elapsed milliseconds from the session start
     */
    public void onCompileStarted(String className, long timestampMs, long sessionOffsetMs) {
        Map<String, Object> data = new HashMap<>();
        data.put("className", className);
        eventSink.accept(
            eventFactory.createCompileEvent(
                EventType.COMPILE_START, timestampMs, sessionOffsetMs, data
            )
        );
    }

    /**
     * Records the result of a compilation and its collected diagnostics.
     *
     * @param success whether compilation succeeded
     * @param errors diagnostics collected during this compilation
     * @param timestampMs callback time in milliseconds since the epoch
     * @param sessionOffsetMs elapsed milliseconds from the session start
     */
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
