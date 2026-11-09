package org.comp3607.collector;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.*;
import java.util.*;

import org.comp3607.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileLoggingExecutionTest {
    private static final long RUN_TIMESTAMP_MS = 2_000L;
    private static final long RUN_OFFSET_MS = 1_200L;

    @TempDir Path directory;

    @Test
    void logsNormalRunWithoutAnException() throws Exception {
        Path logFile = directory.resolve("execution.log");
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("className", "Example");
        eventData.put("exception", null);

        BufferedEventSink eventSource = FileLoggingTestSupport.eventSource();
        FileLoggingEventSink eventSink = FileLoggingTestSupport.eventSink(eventSource, logFile);
        TelemetryEvent event = new TelemetryEvent(
            EventType.EXECUTION, RUN_TIMESTAMP_MS, RUN_OFFSET_MS, eventData
        );
        eventSink.accept(event);

        String log = Files.readString(logFile);
        assertTrue(log.contains("event=EXECUTION timestampMs=" + RUN_TIMESTAMP_MS));
        assertTrue(log.contains("sessionOffsetMs=" + RUN_OFFSET_MS));
        assertTrue(log.contains("exception=null"));
    }
}
