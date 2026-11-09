package org.comp3607.bluej;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.comp3607.collector.*;
import org.comp3607.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import bluej.extensions2.event.*;

class BlueJApiAdapterCompileFailureTest {
    private static final int COMPILE_FINISH_EVENT_INDEX = 1;

    @TempDir Path project;

    @Test
    void recordsCompileFailureAndDiagnostics() throws Exception {
        Path sourceFile = BlueJApiAdapterTestSupport.sourceFile(project);
        BufferedEventSink source = BlueJApiAdapterTestSupport.eventSource();
        FileLoggingEventSink sink = BlueJApiAdapterTestSupport.eventSink(source, project);
        BlueJApiAdapter adapter = BlueJApiAdapterTestSupport.adapter(sink);
        BlueJCompileTestSupport.startCompilation(adapter, sourceFile);
        BlueJCompileTestSupport.recordCompileError(adapter, sourceFile);
        BlueJCompileTestSupport.finishFailedCompilation(adapter, sourceFile);

        Map<String, Object> data = source.snapshot().get(COMPILE_FINISH_EVENT_INDEX).getData();
        List<?> errors = assertInstanceOf(List.class, data.get("errors"));
        Map<?, ?> diagnostic = assertInstanceOf(Map.class, errors.get(0));
        String log = Files.readString(project.resolve("bluej-telemetry.log"));
        assertEquals(Boolean.FALSE, data.get("success"));
        assertNull(diagnostic.get("errorCode"));
        assertEquals(BlueJApiAdapterTestSupport.COMPILE_ERROR_LINE, diagnostic.get("lineNumber"));
        assertEquals(
            BlueJApiAdapterTestSupport.COMPILE_ERROR_MESSAGE, diagnostic.get("message")
        );
        assertTrue(log.contains("status=FAILED"));
        String errorLine = "lineNumber=" + BlueJApiAdapterTestSupport.COMPILE_ERROR_LINE;
        assertTrue(log.contains(errorLine));
    }
}
