package org.comp3607.collector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.comp3607.model.EventType;
import org.comp3607.model.SessionMetadata;
import org.comp3607.model.TelemetryEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileLoggingEventSinkTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void storesEventsAndWritesThemToTheLog() throws Exception {
        BufferedEventSink eventSource = newEventSource();
        FileLoggingEventSink eventSink = new FileLoggingEventSink(eventSource);
        Path logFile = temporaryDirectory.resolve("bluej-telemetry.log");
        eventSink.setLogFile(logFile.toFile());

        Map<String, Object> startData = new HashMap<>();
        startData.put("className", "Example");
        eventSink.accept(new TelemetryEvent(
            EventType.COMPILE_START, 1_000L, 200L, startData
        ));

        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", "compiler-error");
        error.put("lineNumber", 12);
        error.put("message", "Missing return statement");
        List<Map<String, Object>> errors = new ArrayList<>();
        errors.add(error);

        Map<String, Object> finishData = new HashMap<>();
        finishData.put("success", false);
        finishData.put("errors", errors);
        eventSink.accept(new TelemetryEvent(
            EventType.COMPILE_FINISH,
            1_500L,
            700L,
            finishData
        ));

        String log = Files.readString(logFile);
        assertEquals(2, eventSource.snapshot().size());
        assertTrue(log.contains("event=COMPILE_START timestampMs=1000 sessionOffsetMs=200"));
        assertTrue(log.contains("className=\"Example\""));
        assertTrue(log.contains("event=COMPILE_FINISH timestampMs=1500 sessionOffsetMs=700"));
        assertTrue(log.contains("status=FAILED success=false"));
        assertTrue(log.contains("errorCode=compiler-error lineNumber=12"));
        assertTrue(log.contains("message=\"Missing return statement\""));
    }

    @Test
    void keepsEventInMemoryIfTheLogCannotBeWritten() throws Exception {
        BufferedEventSink eventSource = newEventSource();
        FileLoggingEventSink eventSink = new FileLoggingEventSink(eventSource);
        Path directoryAtLogPath = Files.createDirectory(
            temporaryDirectory.resolve("not-a-file")
        );
        eventSink.setLogFile(directoryAtLogPath.toFile());

        Map<String, Object> data = new HashMap<>();
        data.put("className", "Example");
        TelemetryEvent event = new TelemetryEvent(
            EventType.COMPILE_START, 1_000L, 200L, data
        );
        eventSink.accept(event);

        assertEquals(event, eventSource.snapshot().get(0));
    }

    @Test
    void keepsEventInMemoryWhenThereIsNoLogFile() {
        BufferedEventSink eventSource = newEventSource();
        FileLoggingEventSink eventSink = new FileLoggingEventSink(eventSource);
        Map<String, Object> data = new HashMap<>();
        data.put("className", "Example");
        TelemetryEvent event = new TelemetryEvent(
            EventType.COMPILE_START, 1_000L, 200L, data
        );
        eventSink.accept(event);

        assertEquals(event, eventSource.snapshot().get(0));
    }

    private BufferedEventSink newEventSource() {
        return new BufferedEventSink(
            new SessionMetadata("session-1", "unknown", 800L, 0L)
        );
    }
}
