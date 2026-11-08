package org.comp3607.bluej;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.comp3607.collector.BufferedEventSink;
import org.comp3607.collector.CompileEventListener;
import org.comp3607.collector.FileLoggingEventSink;
import org.comp3607.collector.TelemetryEventFactory;
import org.comp3607.model.EventType;
import org.comp3607.model.SessionMetadata;
import org.comp3607.model.TelemetryEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import bluej.extensions2.event.CompileEvent;

class BlueJApiAdapterTest {
    @TempDir
    Path projectDirectory;

    @Test
    void recordsCompileFailureAndWritesItToTheLog() throws IOException {
        Files.createFile(projectDirectory.resolve("package.bluej"));
        Path sourceFile = Files.createFile(projectDirectory.resolve("Example.java"));

        long sessionStartMs = System.currentTimeMillis();
        BufferedEventSink eventSource = new BufferedEventSink(
            new SessionMetadata("test-session", "unknown", sessionStartMs, 0L)
        );
        FileLoggingEventSink eventSink = new FileLoggingEventSink(eventSource);
        CompileEventListener listener = new CompileEventListener(
            eventSink, new TelemetryEventFactory()
        );
        BlueJApiAdapter adapter = new BlueJApiAdapter(
            null, listener, eventSink, sessionStartMs
        );

        adapter.compileStarted(new CompileEvent(
            CompileEvent.EventType.COMPILE_START_EVENT, new File[] { sourceFile.toFile() }
        ));

        CompileEvent compileError = new CompileEvent(
            CompileEvent.EventType.COMPILE_ERROR_EVENT, new File[] { sourceFile.toFile() }
        );
        compileError.setErrorLineNumber(12);
        compileError.setErrorMessage("Missing return statement");
        adapter.compileError(compileError);

        adapter.compileFailed(new CompileEvent(
            CompileEvent.EventType.COMPILE_FAILED_EVENT, new File[] { sourceFile.toFile() }
        ));

        List<TelemetryEvent> events = eventSource.snapshot();
        assertEquals(2, events.size());
        assertEquals(EventType.COMPILE_FINISH, events.get(1).getType());

        Map<String, Object> finishData = events.get(1).getData();
        assertEquals(Boolean.FALSE, finishData.get("success"));
        List<?> errors = assertInstanceOf(List.class, finishData.get("errors"));
        Map<?, ?> error = assertInstanceOf(Map.class, errors.get(0));
        assertNull(error.get("errorCode"));
        assertEquals("Missing return statement", error.get("message"));
        assertEquals(12, error.get("lineNumber"));

        String log = Files.readString(projectDirectory.resolve("bluej-telemetry.log"));
        assertTrue(log.contains("event=COMPILE_START"));
        assertTrue(log.contains("event=COMPILE_FINISH"));
        assertTrue(log.contains("status=FAILED"));
        assertTrue(log.contains("errorCode=null"));
        assertTrue(log.contains("message=\"Missing return statement\""));
        assertTrue(log.contains("lineNumber=12"));
    }

    @Test
    void recordsSuccessfulCompilationAndWritesItToTheLog() throws IOException {
        Files.createFile(projectDirectory.resolve("package.bluej"));
        Path sourceFile = Files.createFile(projectDirectory.resolve("Example.java"));

        long sessionStartMs = System.currentTimeMillis();
        BufferedEventSink eventSource = new BufferedEventSink(
            new SessionMetadata("test-session", "unknown", sessionStartMs, 0L)
        );
        FileLoggingEventSink eventSink = new FileLoggingEventSink(eventSource);
        CompileEventListener listener = new CompileEventListener(
            eventSink, new TelemetryEventFactory()
        );
        BlueJApiAdapter adapter = new BlueJApiAdapter(
            null, listener, eventSink, sessionStartMs
        );

        adapter.compileStarted(new CompileEvent(
            CompileEvent.EventType.COMPILE_START_EVENT, new File[] { sourceFile.toFile() }
        ));
        adapter.compileSucceeded(new CompileEvent(
            CompileEvent.EventType.COMPILE_DONE_EVENT, new File[] { sourceFile.toFile() }
        ));

        String log = Files.readString(projectDirectory.resolve("bluej-telemetry.log"));
        assertTrue(log.contains("event=COMPILE_START"));
        assertTrue(log.contains("className=\"Example\""));
        assertTrue(log.contains("event=COMPILE_FINISH"));
        assertTrue(log.contains("status=SUCCEEDED"));
        assertTrue(log.contains("success=true"));
    }

    @Test
    void startsTheNextCompilationWithNoOldErrors() throws IOException {
        Files.createFile(projectDirectory.resolve("package.bluej"));
        Path sourceDirectory = Files.createDirectory(projectDirectory.resolve("src"));
        Path sourceFile = Files.createFile(sourceDirectory.resolve("Example.java"));

        long sessionStartMs = System.currentTimeMillis();
        BufferedEventSink eventSource = new BufferedEventSink(
            new SessionMetadata("test-session", "unknown", sessionStartMs, 0L)
        );
        FileLoggingEventSink eventSink = new FileLoggingEventSink(eventSource);
        CompileEventListener listener = new CompileEventListener(
            eventSink, new TelemetryEventFactory()
        );
        BlueJApiAdapter adapter = new BlueJApiAdapter(
            null, listener, eventSink, sessionStartMs
        );

        adapter.compileStarted(new CompileEvent(
            CompileEvent.EventType.COMPILE_START_EVENT, new File[] { sourceFile.toFile() }
        ));
        CompileEvent firstError = new CompileEvent(
            CompileEvent.EventType.COMPILE_ERROR_EVENT, new File[] { sourceFile.toFile() }
        );
        firstError.setErrorLineNumber(3);
        firstError.setErrorMessage("First compilation error");
        adapter.compileError(firstError);
        adapter.compileFailed(new CompileEvent(
            CompileEvent.EventType.COMPILE_FAILED_EVENT, new File[] { sourceFile.toFile() }
        ));

        adapter.compileStarted(new CompileEvent(
            CompileEvent.EventType.COMPILE_START_EVENT, new File[] { sourceFile.toFile() }
        ));
        adapter.compileSucceeded(new CompileEvent(
            CompileEvent.EventType.COMPILE_DONE_EVENT, new File[] { sourceFile.toFile() }
        ));

        List<TelemetryEvent> events = eventSource.snapshot();
        Map<String, Object> successfulFinish = events.get(events.size() - 1).getData();
        assertEquals(Boolean.TRUE, successfulFinish.get("success"));
        assertEquals(List.of(), successfulFinish.get("errors"));

        String log = Files.readString(projectDirectory.resolve("bluej-telemetry.log"));
        assertTrue(log.contains("First compilation error"));
        assertTrue(log.contains("status=SUCCEEDED"));
    }

    @Test
    void usesUnknownClassWhenBlueJSuppliesNoFiles() {
        long sessionStartMs = System.currentTimeMillis();
        BufferedEventSink eventSource = new BufferedEventSink(
            new SessionMetadata("test-session", "unknown", sessionStartMs, 0L)
        );
        FileLoggingEventSink eventSink = new FileLoggingEventSink(eventSource);
        CompileEventListener listener = new CompileEventListener(
            eventSink, new TelemetryEventFactory()
        );
        BlueJApiAdapter adapter = new BlueJApiAdapter(
            null, listener, eventSink, sessionStartMs
        );
        adapter.compileStarted(new CompileEvent(
            CompileEvent.EventType.COMPILE_START_EVENT, new File[0]
        ));

        TelemetryEvent event = eventSource.snapshot().get(0);
        assertEquals(EventType.COMPILE_START, event.getType());
        assertEquals("unknown", event.getData().get("className"));
    }
}
