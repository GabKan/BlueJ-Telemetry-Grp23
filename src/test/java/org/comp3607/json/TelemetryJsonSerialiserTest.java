package org.comp3607.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.comp3607.collector.BufferedEventSink;
import org.comp3607.export.ExportFileNamer;
import org.comp3607.export.JsonFileExporter;
import org.comp3607.model.EventType;
import org.comp3607.model.SessionMetadata;
import org.comp3607.model.TelemetryEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

class TelemetryJsonSerialiserTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void writesEditorSamplesToRawTelemetryAndReadsSessionEvents() throws IOException {
        BufferedEventSink source = new BufferedEventSink(
            new SessionMetadata("session-1", "unknown", 100L, 300L)
        );
        source.accept(new TelemetryEvent(
            EventType.EXECUTION, 250L, 150L, Map.of("className", "Example")
        ));

        Map<String, Object> activity = new HashMap<>();
        activity.put("segments", List.of(Map.of(
            "class", "Example", "label", "typing", "chars", 4
        )));
        activity.put("summary", Map.of("editCount", 1));

        Path telemetryFile = temporaryDirectory.resolve("telemetry.json");
        JsonFileExporter exporter = new JsonFileExporter(
            new TelemetryJsonSerialiser(),
            new ExportFileNamer(),
            temporaryDirectory
        );
        exporter.export(source, telemetryFile, activity);

        JsonObject root = JsonParser.parseString(
            java.nio.file.Files.readString(telemetryFile)
        ).getAsJsonObject();
        JsonObject savedActivity = root.getAsJsonObject("gui")
            .getAsJsonObject("editorActivity");
        assertEquals(4, savedActivity.getAsJsonArray("segments").get(0)
            .getAsJsonObject().get("chars").getAsInt());
        assertTrue(root.has("events"));

        BufferedEventSink loaded = new TelemetryJsonReader().read(telemetryFile);
        assertEquals("session-1", loaded.metadata().getSessionId());
        assertEquals(1, loaded.snapshot().size());
        assertEquals(EventType.EXECUTION, loaded.snapshot().get(0).getType());
    }

    @Test
    void writesEmptyEventsWhenThereIsNoSessionActivity() {
        BufferedEventSink source = new BufferedEventSink(
            new SessionMetadata("empty", "unknown", 100L, 100L)
        );

        JsonObject root = JsonParser.parseString(
            new TelemetryJsonSerialiser().serialise(source)
        ).getAsJsonObject();

        assertEquals(0, root.getAsJsonArray("events").size());
        assertTrue(!root.has("gui"));
    }
}
