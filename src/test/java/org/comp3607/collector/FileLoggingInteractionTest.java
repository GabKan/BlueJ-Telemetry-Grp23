package org.comp3607.collector;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.*;
import java.util.*;

import org.comp3607.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileLoggingInteractionTest {
    private static final long EVENT_TIME_MS = 61_000L;
    private static final long SESSION_OFFSET_MS = 60_000L;

    @TempDir Path directory;

    @Test
    void writesEditAndIdleTelemetry() throws Exception {
        Path logFile = directory.resolve("bluej-telemetry.log");
        BufferedEventSink source = FileLoggingTestSupport.eventSource();
        FileLoggingEventSink sink = FileLoggingTestSupport.eventSink(source, logFile);
        sink.accept(new TelemetryEvent(EventType.EDIT_ACTIVITY, EVENT_TIME_MS,
            SESSION_OFFSET_MS, Map.of("activitySource", "COMPILE",
                "classNames", List.of("Example"))));
        sink.accept(new TelemetryEvent(EventType.IDLE_PERIOD, EVENT_TIME_MS,
            SESSION_OFFSET_MS, Map.of("durationMs", SESSION_OFFSET_MS,
                "previousActivity", "COMPILE", "resumedActivity", "EXECUTION")));

        String log = Files.readString(logFile);
        assertTrue(log.contains("event=EDIT_ACTIVITY"));
        assertTrue(log.contains("activitySource=COMPILE"));
        assertTrue(log.contains("event=IDLE_PERIOD"));
        assertTrue(log.contains("durationMs=60000"));
    }
}
