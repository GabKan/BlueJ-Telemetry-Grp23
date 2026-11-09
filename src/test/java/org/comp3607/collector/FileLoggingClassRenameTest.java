package org.comp3607.collector;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.*;
import java.util.Map;

import org.comp3607.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileLoggingClassRenameTest {
    private static final long RENAME_TIMESTAMP_MS = 2_600L;
    private static final long RENAME_SESSION_OFFSET_MS = 1_800L;

    @TempDir Path directory;

    @Test
    void writesClassRenameToLog() throws Exception {
        Path logFile = directory.resolve("rename.log");
        BufferedEventSink eventSource = FileLoggingTestSupport.eventSource();
        FileLoggingEventSink eventSink = FileLoggingTestSupport.eventSink(eventSource, logFile);
        Map<String, Object> eventData = Map.of(
            "className", "RenamedExample", "oldName", "Example"
        );
        TelemetryEvent event = new TelemetryEvent(
            EventType.CLASS_MODIFIED, RENAME_TIMESTAMP_MS, RENAME_SESSION_OFFSET_MS, eventData
        );
        eventSink.accept(event);

        String log = Files.readString(logFile);
        assertTrue(log.contains("event=CLASS_MODIFIED timestampMs=" + RENAME_TIMESTAMP_MS));
        assertTrue(log.contains("sessionOffsetMs=" + RENAME_SESSION_OFFSET_MS));
        assertTrue(log.contains("oldName=\"Example\""));
    }
}
