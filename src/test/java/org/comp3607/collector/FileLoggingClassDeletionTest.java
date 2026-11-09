package org.comp3607.collector;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.*;
import java.util.Map;

import org.comp3607.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileLoggingClassDeletionTest {
    private static final long DELETE_TIMESTAMP_MS = 2_500L;
    private static final long DELETE_SESSION_OFFSET_MS = 1_700L;

    @TempDir Path directory;

    @Test
    void writesClassDeletionToLog() throws Exception {
        Path logFile = directory.resolve("bluej-telemetry.log");
        BufferedEventSink eventSource = FileLoggingTestSupport.eventSource();
        FileLoggingEventSink eventSink = FileLoggingTestSupport.eventSink(eventSource, logFile);
        Map<String, Object> eventData = Map.of("className", "Example");
        TelemetryEvent event = new TelemetryEvent(
            EventType.CLASS_DELETED, DELETE_TIMESTAMP_MS, DELETE_SESSION_OFFSET_MS, eventData
        );
        eventSink.accept(event);

        String log = Files.readString(logFile);
        assertTrue(log.contains("event=CLASS_DELETED timestampMs=" + DELETE_TIMESTAMP_MS));
        assertTrue(log.contains("sessionOffsetMs=" + DELETE_SESSION_OFFSET_MS));
        assertTrue(log.contains("className=\"Example\""));
    }
}
