package org.comp3607.bluej;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;

import org.comp3607.collector.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import bluej.extensions2.event.*;

class BlueJApiAdapterCompileSuccessTest {
    @TempDir Path project;

    @Test
    void recordsSuccessfulCompilationToTheLog() throws Exception {
        Path sourceFile = BlueJApiAdapterTestSupport.sourceFile(project);
        BufferedEventSink source = BlueJApiAdapterTestSupport.eventSource();
        FileLoggingEventSink sink = BlueJApiAdapterTestSupport.eventSink(source, project);
        BlueJApiAdapter adapter = BlueJApiAdapterTestSupport.adapter(sink);
        BlueJCompileTestSupport.startCompilation(adapter, sourceFile);
        BlueJCompileTestSupport.finishSuccessfulCompilation(adapter, sourceFile);

        String log = Files.readString(project.resolve("bluej-telemetry.log"));
        assertTrue(log.contains("event=COMPILE_START"));
        assertTrue(log.contains("className=\"Example\""));
        assertTrue(log.contains("status=SUCCEEDED"));
        assertTrue(log.contains("success=true"));
    }
}
