package org.comp3607.collector;

import java.nio.file.*;

import org.comp3607.model.*;

final class FileLoggingTestSupport {
    static final long SESSION_START_MS = 800L;
    private static final long SESSION_END_MS = 0L;

    static BufferedEventSink eventSource() {
        SessionMetadata metadata = new SessionMetadata(
            "session-1", "unknown", SESSION_START_MS, SESSION_END_MS
        );
        return new BufferedEventSink(metadata);
    }

    static FileLoggingEventSink eventSink(BufferedEventSink source, Path logFile) {
        FileLoggingEventSink sink = new FileLoggingEventSink(source);
        sink.setLogFile(logFile.toFile());
        return sink;
    }
}
