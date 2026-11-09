package org.comp3607.collector;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.comp3607.model.*;
import org.junit.jupiter.api.Test;

class ClassEventListenerTest {
    private static final long DELETE_TIME_MS = 1_000L;
    private static final long DELETE_OFFSET_MS = 200L;
    private static final long RENAME_TIME_MS = 1_100L;
    private static final long RENAME_OFFSET_MS = 300L;

    @Test
    void mapsClassDeletionToAnEventWithTiming() {
        BufferedEventSink sink = CollectorTestSupport.newEventSink();
        ClassEventListener listener = new ClassEventListener(sink, new TelemetryEventFactory());
        listener.onClassDeleted("Example", DELETE_TIME_MS, DELETE_OFFSET_MS);

        TelemetryEvent event = sink.snapshot().get(0);
        assertEquals(EventType.CLASS_DELETED, event.getType());
        assertEquals(DELETE_TIME_MS, event.getTimestampMs());
        assertEquals(DELETE_OFFSET_MS, event.getSessionOffsetMs());
        assertEquals(Map.of("className", "Example"), event.getData());
    }

    @Test
    void mapsClassRenameToAModificationEvent() {
        BufferedEventSink sink = CollectorTestSupport.newEventSink();
        ClassEventListener listener = new ClassEventListener(sink, new TelemetryEventFactory());
        listener.onClassRenamed(
            "RenamedExample", "Example", RENAME_TIME_MS, RENAME_OFFSET_MS
        );

        TelemetryEvent event = sink.snapshot().get(0);
        assertEquals(EventType.CLASS_MODIFIED, event.getType());
        assertEquals(RENAME_TIME_MS, event.getTimestampMs());
        assertEquals(RENAME_OFFSET_MS, event.getSessionOffsetMs());
        assertEquals(Map.of("className", "RenamedExample", "oldName", "Example"),
            event.getData());
    }
}
