package org.comp3607.collector;

import java.util.*;

import org.comp3607.api.EventSink;
import org.comp3607.model.*;

public class InteractionEventListener {
    public static final long DEFAULT_IDLE_THRESHOLD_MS = 60_000L;

    private final EventSink eventSink;
    private final TelemetryEventFactory eventFactory;
    private long lastActivityTimestampMs;
    private String lastActivityType = "SESSION_START";

    public InteractionEventListener(
        EventSink eventSink,
        TelemetryEventFactory eventFactory,
        long sessionStartMs
    ) {
        this.eventSink = eventSink;
        this.eventFactory = eventFactory;
        this.lastActivityTimestampMs = sessionStartMs;
    }

    public void onCompileStarted(
        List<String> classNames,
        long timestampMs,
        long sessionOffsetMs
    ) {
        recordIdlePeriod("COMPILE", timestampMs, sessionOffsetMs);

        Map<String, Object> data = new HashMap<>();
        data.put("activitySource", "COMPILE");
        data.put("classNames", new ArrayList<>(classNames));
        eventSink.accept(eventFactory.createInteractionEvent(
            EventType.EDIT_ACTIVITY, timestampMs, sessionOffsetMs, data
        ));
    }

    public void onActivity(String activityType, long timestampMs, long sessionOffsetMs) {
        recordIdlePeriod(activityType, timestampMs, sessionOffsetMs);
    }

    public void onCompileFinished(long timestampMs) {
        lastActivityTimestampMs = timestampMs;
        lastActivityType = "COMPILE";
    }

    private void recordIdlePeriod(String activityType, long timestampMs, long sessionOffsetMs) {
        long idleDurationMs = timestampMs - lastActivityTimestampMs;
        if (idleDurationMs >= DEFAULT_IDLE_THRESHOLD_MS) {
            Map<String, Object> data = new HashMap<>();
            data.put("durationMs", idleDurationMs);
            data.put("previousActivity", lastActivityType);
            data.put("resumedActivity", activityType);
            eventSink.accept(eventFactory.createInteractionEvent(
                EventType.IDLE_PERIOD, timestampMs, sessionOffsetMs, data
            ));
        }
        lastActivityTimestampMs = timestampMs;
        lastActivityType = activityType;
    }
}
