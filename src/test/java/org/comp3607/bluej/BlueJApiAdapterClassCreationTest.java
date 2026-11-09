package org.comp3607.bluej;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.comp3607.collector.BufferedEventSink;
import org.comp3607.model.EventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BlueJApiAdapterClassCreationTest {
    private static final long EXPECTED_CREATION_COUNT = 1L;

    @TempDir Path project;

    @Test
    void recordsCreationOnFirstSuccessfulCompileOnly() throws Exception {
        Path sourceFile = BlueJApiAdapterTestSupport.sourceFile(project);
        BufferedEventSink source = BlueJApiAdapterTestSupport.eventSource();
        BlueJApiAdapter adapter = BlueJApiAdapterTestSupport.adapter(
            BlueJApiAdapterTestSupport.eventSink(source, project)
        );
        BlueJCompileTestSupport.startCompilation(adapter, sourceFile);
        BlueJCompileTestSupport.finishFailedCompilation(adapter, sourceFile);
        BlueJCompileTestSupport.startCompilation(adapter, sourceFile);
        BlueJCompileTestSupport.finishSuccessfulCompilation(adapter, sourceFile);
        BlueJCompileTestSupport.startCompilation(adapter, sourceFile);
        BlueJCompileTestSupport.finishSuccessfulCompilation(adapter, sourceFile);

        List<?> creations = source.snapshot().stream()
            .filter(event -> event.getType() == EventType.CLASS_CREATED)
            .toList();
        assertEquals(EXPECTED_CREATION_COUNT, creations.size());
        String log = Files.readString(project.resolve("bluej-telemetry.log"));
        assertTrue(log.contains("event=CLASS_CREATED"));
    }
}
