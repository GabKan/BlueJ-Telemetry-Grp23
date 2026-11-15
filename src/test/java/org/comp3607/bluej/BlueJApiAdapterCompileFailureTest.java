package org.comp3607.bluej;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.*;

import org.comp3607.collector.*;
import org.comp3607.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import bluej.extensions2.event.*;

class BlueJApiAdapterCompileFailureTest {
    @TempDir Path project;

    @Test
    void recordsCompileFailureAndDiagnostics() throws Exception {
        Path sourceFile = BlueJApiAdapterTestSupport.sourceFile(project);
        BufferedEventSink source = BlueJApiAdapterTestSupport.eventSource();
        BlueJApiAdapter adapter = BlueJApiAdapterTestSupport.adapter(source);
        BlueJCompileTestSupport.startCompilation(adapter, sourceFile);
        BlueJCompileTestSupport.recordCompileError(adapter, sourceFile);
        BlueJCompileTestSupport.finishFailedCompilation(adapter, sourceFile);

        TelemetryEvent finishEvent = source.snapshot().stream()
            .filter(event -> event.getType() == EventType.COMPILE_FINISH)
            .findFirst()
            .orElseThrow();
        Map<String, Object> data = finishEvent.getData();
        List<?> errors = assertInstanceOf(List.class, data.get("errors"));
        Map<?, ?> diagnostic = assertInstanceOf(Map.class, errors.get(0));
        assertEquals(Boolean.FALSE, data.get("success"));
        assertNull(diagnostic.get("errorCode"));
        assertEquals(BlueJApiAdapterTestSupport.COMPILE_ERROR_LINE, diagnostic.get("lineNumber"));
        assertEquals(
            BlueJApiAdapterTestSupport.COMPILE_ERROR_MESSAGE, diagnostic.get("message")
        );
    }
}
