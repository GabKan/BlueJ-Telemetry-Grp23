package org.comp3607.collector;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.*;
import java.util.*;

import org.comp3607.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileLoggingCompileTest {
    private static final long COMPILE_START_TIME_MS = 1_000L;
    private static final long COMPILE_START_OFFSET_MS = 200L;
    private static final long COMPILE_FINISH_TIME_MS = 1_500L;
    private static final long COMPILE_FINISH_OFFSET_MS = 700L;
    private static final int ERROR_LINE_NUMBER = 12;
    private static final int EXPECTED_EVENT_COUNT = 2;

    @TempDir Path directory;
    @Test
    void logsCompilationStartAndFailureDetails() throws Exception {
        BufferedEventSink source = FileLoggingTestSupport.eventSource();
        Path logFile = directory.resolve("bluej-telemetry.log");
        FileLoggingEventSink sink = FileLoggingTestSupport.eventSink(source, logFile);
        Map<String, Object> diagnostic = Map.of(
            "errorCode", "compiler-error", "lineNumber", ERROR_LINE_NUMBER,
            "message", "Missing return statement"
        );
        Map<String, Object> startData = Map.of("className", "Example");
        TelemetryEvent startEvent = new TelemetryEvent(
            EventType.COMPILE_START, COMPILE_START_TIME_MS, COMPILE_START_OFFSET_MS, startData
        );
        sink.accept(startEvent);
        Map<String, Object> finishData = Map.of(
            "success", false, "errors", List.of(diagnostic)
        );
        TelemetryEvent finishEvent = new TelemetryEvent(
            EventType.COMPILE_FINISH, COMPILE_FINISH_TIME_MS, COMPILE_FINISH_OFFSET_MS, finishData
        );
        sink.accept(finishEvent);

        String log = Files.readString(logFile);
        assertEquals(EXPECTED_EVENT_COUNT, source.snapshot().size());
        assertTrue(log.contains("status=FAILED success=false"));
        String errorLine = "errorCode=compiler-error lineNumber=" + ERROR_LINE_NUMBER;
        assertTrue(log.contains(errorLine));
        assertTrue(log.contains("message=\"Missing return statement\""));
    }
}
