package org.comp3607.export;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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

    @Override
    public void export(EventSource source) throws IOException {
        String json = serialiser.serialise(source);
        Path file = outputDirectory.resolve(fileNamer.nameFor(source.getSessionMetadata()));
        Files.writeString(file, json, StandardCharsets.UTF_8);
    }
}
