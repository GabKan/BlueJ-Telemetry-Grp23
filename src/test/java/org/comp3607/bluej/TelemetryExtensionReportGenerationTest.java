package org.comp3607.bluej;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
        Map<String, Object> editorActivity = Map.of(
            "summary", Map.of(
                "typing", Map.of("totalMs", 20000L, "totalChars", 50L),
                "deletion", Map.of("totalMs", 5000L, "totalChars", 10L),
                "paste", Map.of("totalMs", 1000L, "count", 1L),
                "idle", Map.of("totalMs", 30000L, "longestMs", 15000L),
                "editCount", 4L
            )
        );

        TelemetryExtension.generateReportsForSession(
            source, tempDir, metadata.getSessionId(), editorActivity
        );

        Path jsonReport = tempDir.resolve("json_reports")
            .resolve("report_session-live.json");
        assertTrue(Files.exists(jsonReport));
        assertTrue(Files.exists(tempDir.resolve("pdf_reports").resolve("report_session-live.pdf")));

        JsonObject editorSummary = JsonParser.parseString(Files.readString(jsonReport))
            .getAsJsonObject()
            .getAsJsonObject("summary")
            .getAsJsonObject("editorActivity");
        assertTrue(editorSummary.get("available").getAsBoolean());
        assertEquals(20000L, editorSummary.get("typingMs").getAsLong());
        assertEquals(50L, editorSummary.get("typingChars").getAsLong());
        assertEquals(5000L, editorSummary.get("deletionMs").getAsLong());
        assertEquals(10L, editorSummary.get("deletionChars").getAsLong());
        assertEquals(1000L, editorSummary.get("pasteMs").getAsLong());
        assertEquals(1L, editorSummary.get("pasteCount").getAsLong());
        assertEquals(26000L, editorSummary.get("activeMs").getAsLong());
        assertEquals(30000L, editorSummary.get("idleMs").getAsLong());
        assertEquals(15000L, editorSummary.get("longestIdleMs").getAsLong());
        assertEquals(4L, editorSummary.get("editCount").getAsLong());
        assertEquals(150.0, editorSummary.get("typingSpeedCharsPerMinute").getAsDouble());
    }
}
