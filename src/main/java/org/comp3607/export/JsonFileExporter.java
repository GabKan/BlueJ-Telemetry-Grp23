package org.comp3607.export;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.comp3607.api.EventSource;
import org.comp3607.api.Exporter;
import org.comp3607.json.TelemetryJsonSerialiser;

public class JsonFileExporter implements Exporter {

    private final TelemetryJsonSerialiser serialiser;
    private final ExportFileNamer fileNamer;
    private final Path outputDirectory;

    public JsonFileExporter(TelemetryJsonSerialiser serialiser, ExportFileNamer fileNamer, Path outputDirectory) {
        this.serialiser = serialiser;
        this.fileNamer = fileNamer;
        this.outputDirectory = outputDirectory;
    }

    public void export(EventSource source) throws IOException {
        export(source, outputDirectory.resolve(fileNamer.nameFor(source.metadata())));
    }

    @Override
    public void export(EventSource source, Path target) throws IOException {
        write(source, target, null);
    }

    public void export(
        EventSource source,
        Path target,
        Map<String, Object> editorActivity
    ) throws IOException {
        write(source, target, editorActivity);
    }

    private void write(
        EventSource source,
        Path target,
        Map<String, Object> editorActivity
    ) throws IOException {
        String json = serialiser.serialise(source, editorActivity);
        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(target, json, StandardCharsets.UTF_8);
    }
}
