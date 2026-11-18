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
        Files.createFile(project.resolve("package.bluej"));
        BufferedEventSink source = BlueJApiAdapterTestSupport.eventSource();
        BlueJApiAdapter adapter = BlueJApiAdapterTestSupport.adapter(source);
        adapter.rememberExistingClasses(project.toFile());
        Path sourceFile = Files.createFile(project.resolve("Example.java"));
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
        assertTrue(source.snapshot().stream()
            .anyMatch(event -> event.getType() == EventType.CLASS_CREATED));
    }

    @Test
    void baselinesExistingSourcesWhenProjectIsFirstFoundAtCompileTime() throws Exception {
        Path sourceFile = BlueJApiAdapterTestSupport.sourceFile(project);
        BufferedEventSink source = BlueJApiAdapterTestSupport.eventSource();
        BlueJApiAdapter adapter = BlueJApiAdapterTestSupport.adapter(source);

        BlueJCompileTestSupport.startCompilation(adapter, sourceFile);
        BlueJCompileTestSupport.finishSuccessfulCompilation(adapter, sourceFile);

        assertEquals(0, source.snapshot().stream()
            .filter(event -> event.getType() == EventType.CLASS_CREATED)
            .count());
    }

    @Test
    void recordsSourceAddedAfterTheStartupSnapshotAsCreated() throws Exception {
        BlueJApiAdapterTestSupport.sourceFile(project);
        BufferedEventSink source = BlueJApiAdapterTestSupport.eventSource();
        BlueJApiAdapter adapter = BlueJApiAdapterTestSupport.adapter(source);
        adapter.rememberExistingClasses(project.toFile());
        Path newSourceFile = Files.createFile(project.resolve("NewExample.java"));

        BlueJCompileTestSupport.startCompilation(adapter, newSourceFile);
        BlueJCompileTestSupport.finishSuccessfulCompilation(adapter, newSourceFile);

        assertEquals(EXPECTED_CREATION_COUNT, source.snapshot().stream()
            .filter(event -> event.getType() == EventType.CLASS_CREATED)
            .count());
    }
}
