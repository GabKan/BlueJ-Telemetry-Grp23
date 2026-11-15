package org.comp3607.export;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.comp3607.collector.BufferedEventSink;
import org.comp3607.json.TelemetryJsonSerialiser;
import org.comp3607.model.EventType;
import org.comp3607.model.SessionMetadata;
import org.comp3607.model.TelemetryEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonFileExporterTest {
    @TempDir
    Path directory;

    @Test
    void writesOneJsonFileForTheSession() throws Exception {
        SessionMetadata metadata = new SessionMetadata("session-42", "unknown", 100L, 300L);
        BufferedEventSink source = new BufferedEventSink(metadata);
        source.accept(new TelemetryEvent(
            EventType.EXECUTION,
            250L,
            150L,
            Map.of("className", "Example", "exception", "EXCEPTION_EXIT")
        ));

        Path outputDirectory = directory.resolve("project").resolve(".telemetry");
        JsonFileExporter exporter = new JsonFileExporter(
            new TelemetryJsonSerialiser(),
            new ExportFileNamer(),
            outputDirectory
        );
        exporter.export(source);

        Path jsonFile = outputDirectory.resolve("telemetry_session-42.json");
        assertTrue(Files.isDirectory(outputDirectory));
        assertTrue(Files.isRegularFile(jsonFile));
        String json = Files.readString(jsonFile);
        assertTrue(json.contains("\"sessionId\": \"session-42\""));
        assertTrue(json.contains("\"type\": \"EXECUTION\""));
        assertTrue(json.contains("\"exception\": \"EXCEPTION_EXIT\""));
    }

    @Test
    void exportCanWriteToAnExplicitTarget() throws Exception {
        BufferedEventSink source = new BufferedEventSink(
            new SessionMetadata("session-43", "unknown", 100L, 200L)
        );
        Path target = directory.resolve("new-folder").resolve("session.json");
        JsonFileExporter exporter = new JsonFileExporter(
            new TelemetryJsonSerialiser(),
            new ExportFileNamer(),
            directory
        );

        exporter.export(source, target);

        assertTrue(Files.isRegularFile(target));
    }
}
