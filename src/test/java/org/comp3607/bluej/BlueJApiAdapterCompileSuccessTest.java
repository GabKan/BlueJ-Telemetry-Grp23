package org.comp3607.bluej;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.*;

import org.comp3607.collector.*;
import org.comp3607.model.EventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import bluej.extensions2.event.*;

class BlueJApiAdapterCompileSuccessTest {
    @TempDir Path project;

    @Test
    void recordsSuccessfulCompilationAndFindsTheProjectDirectory() throws Exception {
        Path sourceFile = BlueJApiAdapterTestSupport.sourceFile(project);
        BufferedEventSink source = BlueJApiAdapterTestSupport.eventSource();
        BlueJApiAdapter adapter = BlueJApiAdapterTestSupport.adapter(source);
        BlueJCompileTestSupport.startCompilation(adapter, sourceFile);
        BlueJCompileTestSupport.finishSuccessfulCompilation(adapter, sourceFile);

        assertEquals(project.toFile().getAbsoluteFile(), adapter.getProjectDirectory());
        assertTrue(source.snapshot().stream()
            .anyMatch(event -> event.getType() == EventType.COMPILE_START));
        assertTrue(source.snapshot().stream()
            .anyMatch(event -> event.getType() == EventType.COMPILE_FINISH
                && Boolean.TRUE.equals(event.getData().get("success"))));
    }

    @Test
    void countsOnlyCompileCompletionsThatFollowACompileStart() throws Exception {
        Path sourceFile = BlueJApiAdapterTestSupport.sourceFile(project);
        BufferedEventSink source = BlueJApiAdapterTestSupport.eventSource();
        BlueJApiAdapter adapter = BlueJApiAdapterTestSupport.adapter(source);

        BlueJCompileTestSupport.finishSuccessfulCompilation(adapter, sourceFile);
        BlueJCompileTestSupport.startCompilation(adapter, sourceFile);
        BlueJCompileTestSupport.finishSuccessfulCompilation(adapter, sourceFile);
        BlueJCompileTestSupport.finishSuccessfulCompilation(adapter, sourceFile);

        assertEquals(1, source.snapshot().stream()
            .filter(event -> event.getType() == EventType.COMPILE_FINISH)
            .count());
    }
}
