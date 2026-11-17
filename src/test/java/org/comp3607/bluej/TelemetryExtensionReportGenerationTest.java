package org.comp3607.bluej;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.comp3607.api.EventSource;
import org.comp3607.model.EventType;
import org.comp3607.model.SessionMetadata;
import org.comp3607.model.TelemetryEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TelemetryExtensionReportGenerationTest {

    private static final class FakeEventSource implements EventSource {
        private final List<TelemetryEvent> events;
        private final SessionMetadata metadata;

        FakeEventSource(List<TelemetryEvent> events, SessionMetadata metadata) {
            this.events = events;
            this.metadata = metadata;
        }

        @Override
        public List<TelemetryEvent> snapshot() {
            return new ArrayList<>(events);
        }

        @Override
        public SessionMetadata metadata() {
            return metadata;
        }
    }

    @Test
    public void generateReportsForSession_usesCurrentTelemetryData(@TempDir Path tempDir) throws IOException {
        List<TelemetryEvent> events = new ArrayList<>();
        Map<String, Object> compileData = new HashMap<>();
        compileData.put("success", true);
        events.add(new TelemetryEvent(EventType.COMPILE_FINISH, 1000L, 1000L, compileData));

        Map<String, Object> runData = new HashMap<>();
        runData.put("className", "Example");
        events.add(new TelemetryEvent(EventType.EXECUTION, 2000L, 2000L, runData));

        SessionMetadata metadata = new SessionMetadata("session-live", "student-1", 0L, 3000L);
        EventSource source = new FakeEventSource(events, metadata);

        TelemetryExtension.generateReportsForSession(source, tempDir, metadata.getSessionId());

        assertTrue(Files.exists(tempDir.resolve("json_reports").resolve("report_session-live.json")));
        assertTrue(Files.exists(tempDir.resolve("pdf_reports").resolve("report_session-live.pdf")));
    }
}
