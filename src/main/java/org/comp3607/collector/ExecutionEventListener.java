package org.comp3607.collector;

import java.util.*;

import org.comp3607.api.EventSink;
import org.comp3607.model.EventType;

/**
 * Converts completed BlueJ invocations into execution telemetry events.
 */
public class ExecutionEventListener {
    /** Destination that stores the generated execution events. */
    private final EventSink eventSink;
    /** Factory that creates execution event objects. */
    private final TelemetryEventFactory eventFactory;

    /**
     * Creates a listener that writes execution events to the supplied sink.
     *
     * @param eventSink destination for generated events
     * @param eventFactory factory used to create execution events
     */
    public ExecutionEventListener(EventSink eventSink, TelemetryEventFactory eventFactory) {
        this.eventSink = eventSink;
        this.eventFactory = eventFactory;
    }

    /**
     * Records a completed invocation.
     *
     * @param className name of the invoked class
     * @param exception exception exit marker, or {@code null} for other exits
     * @param timestampMs callback time in milliseconds since the epoch
     * @param sessionOffsetMs elapsed milliseconds from the session start
     */
    public void onExecution(
        String className,
        String exception,
        long timestampMs,
        long sessionOffsetMs
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("className", className);
        data.put("exception", exception);
        eventSink.accept(
            eventFactory.createExecutionEvent(timestampMs, sessionOffsetMs, data)
        );
    }
}
