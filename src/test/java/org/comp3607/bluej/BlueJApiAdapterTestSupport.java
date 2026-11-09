package org.comp3607.bluej;

import java.io.*;
import java.nio.file.*;

import org.comp3607.collector.*;
import org.comp3607.model.*;

final class BlueJApiAdapterTestSupport {
    static final long SESSION_START_MS = System.currentTimeMillis();
    private static final long SESSION_END_MS = 0L;
    static final int COMPILE_ERROR_LINE = 12;
    static final String COMPILE_ERROR_MESSAGE = "Missing return statement";

    static BufferedEventSink eventSource() {
        return new BufferedEventSink(
            new SessionMetadata("test-session", "unknown", SESSION_START_MS, SESSION_END_MS)
        );
    }

    static FileLoggingEventSink eventSink(BufferedEventSink source, Path directory) {
        FileLoggingEventSink sink = new FileLoggingEventSink(source);
        sink.setLogFile(directory.resolve("bluej-telemetry.log").toFile());
        return sink;
    }

    static BlueJApiAdapter adapter(FileLoggingEventSink sink) {
        TelemetryEventFactory factory = new TelemetryEventFactory();
        return new BlueJApiAdapter(
            null,
            new CompileEventListener(sink, factory),
            new ExecutionEventListener(sink, factory),
            new ClassEventListener(sink, factory),
            new InteractionEventListener(sink, factory, SESSION_START_MS),
            sink,
            SESSION_START_MS
        );
    }

    static Path sourceFile(Path directory) throws IOException {
        Files.createFile(directory.resolve("package.bluej"));
        return Files.createFile(directory.resolve("Example.java"));
    }
}
