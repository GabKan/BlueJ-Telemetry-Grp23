package org.comp3607.collector;

import java.util.*;

import org.comp3607.api.EventSink;
import org.comp3607.model.*;

/**
 * Creates compile-activity and idle-period events from callback timing.
 * Compile activity is an interaction proxy; this listener does not capture
 * keystrokes or source text.
 */
public class InteractionEventListener {
    /** Minimum gap between tracked callbacks that is recorded as an idle period. */
    public static final long DEFAULT_IDLE_THRESHOLD_MS = 60_000L;

    /** Destination that stores generated interaction events. */
    private final EventSink eventSink;
    /** Factory that creates validated interaction event objects. */
    private final TelemetryEventFactory eventFactory;
    /** Time of the most recently tracked activity callback. */
    private long lastActivityTimestampMs;
    /** Type of the most recently tracked activity callback. */
    private String lastActivityType = "SESSION_START";

    /**
     * Creates an interaction listener and initializes its activity clock.
     *
     * @param eventSink destination for generated events
     * @param eventFactory factory used to create interaction events
     * @param sessionStartMs session start time in milliseconds since the epoch
     */
    public InteractionEventListener(
        EventSink eventSink,
        TelemetryEventFactory eventFactory,
        long sessionStartMs
    ) {
        this.eventSink = eventSink;
        this.eventFactory = eventFactory;
        this.lastActivityTimestampMs = sessionStartMs;
    }

    /**
     * Records compile activity and checks whether the preceding callback gap
     * qualifies as an idle period.
     *
     * @param classNames class names included in the compilation
     * @param timestampMs callback time in milliseconds since the epoch
     * @param sessionOffsetMs elapsed milliseconds from the session start
     */
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

    /**
     * Updates activity timing and emits an idle-period event when the gap
     * since the previous callback reaches the configured threshold.
     *
     * @param activityType label for the activity resuming after the gap
     * @param timestampMs callback time in milliseconds since the epoch
     * @param sessionOffsetMs elapsed milliseconds from the session start
     */
    public void onActivity(String activityType, long timestampMs, long sessionOffsetMs) {
        recordIdlePeriod(activityType, timestampMs, sessionOffsetMs);
    }

    /**
     * Marks compile completion as the latest activity for future idle-gap
     * calculations.
     *
     * @param timestampMs compile completion time in milliseconds since the epoch
     */
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
