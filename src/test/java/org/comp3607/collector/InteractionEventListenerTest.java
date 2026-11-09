package org.comp3607.collector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.comp3607.model.EventType;
import org.comp3607.model.TelemetryEvent;
import org.junit.jupiter.api.Test;

class InteractionEventListenerTest {
    private static final long SESSION_START_MS = 1_000L;
    private static final long COMPILE_TIME_MS = 1_001L;
    private static final long COMPILE_SESSION_OFFSET_MS = 1L;
    private static final long RESUME_SESSION_OFFSET_MS = 60_001L;
    private static final long RESUME_TIME_MS =
        COMPILE_TIME_MS + InteractionEventListener.DEFAULT_IDLE_THRESHOLD_MS;

    @Test
    void recordsCompileActivityAndLongIdleGap() {
        BufferedEventSink sink = CollectorTestSupport.newEventSink();
        InteractionEventListener listener = new InteractionEventListener(
            sink, new TelemetryEventFactory(), SESSION_START_MS
        );
        listener.onCompileStarted(List.of("Example"), COMPILE_TIME_MS,
            COMPILE_SESSION_OFFSET_MS);
        listener.onActivity("EXECUTION", RESUME_TIME_MS, RESUME_SESSION_OFFSET_MS);

        List<TelemetryEvent> events = sink.snapshot();
        assertEquals(EventType.EDIT_ACTIVITY, events.get(0).getType());
        assertEquals("COMPILE", events.get(0).getData().get("activitySource"));
        assertEquals(List.of("Example"), events.get(0).getData().get("classNames"));
        assertEquals(EventType.IDLE_PERIOD, events.get(1).getType());
        Map<String, Object> idleData = events.get(1).getData();
        assertEquals(
            InteractionEventListener.DEFAULT_IDLE_THRESHOLD_MS, idleData.get("durationMs")
        );
        assertEquals("COMPILE", idleData.get("previousActivity"));
        assertEquals("EXECUTION", idleData.get("resumedActivity"));
    }

}
