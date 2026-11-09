package org.comp3607.collector;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class InteractionEventListenerThresholdTest {
    private static final long SESSION_START_MS = 1_000L;
    private static final long COMPILE_TIME_MS = 1_001L;
    private static final long COMPILE_OFFSET_MS = 1L;

    @Test
    void doesNotRecordShortGapAsIdle() {
        BufferedEventSink sink = CollectorTestSupport.newEventSink();
        InteractionEventListener listener = new InteractionEventListener(
            sink, new TelemetryEventFactory(), SESSION_START_MS
        );
        listener.onCompileStarted(List.of("Example"), COMPILE_TIME_MS, COMPILE_OFFSET_MS);
        long shortGapMs = InteractionEventListener.DEFAULT_IDLE_THRESHOLD_MS - 1L;
        listener.onActivity("EXECUTION", COMPILE_TIME_MS + shortGapMs,
            COMPILE_OFFSET_MS + shortGapMs);

        assertEquals(1, sink.snapshot().size());
    }
}
