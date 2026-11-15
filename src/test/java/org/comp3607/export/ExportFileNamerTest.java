package org.comp3607.export;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.comp3607.model.SessionMetadata;
import org.junit.jupiter.api.Test;

class ExportFileNamerTest {

    @Test
    void nameFor_returnsTelemetryPlusSessionId() {
        SessionMetadata metadata = new SessionMetadata("session-42", "unknown", 100L, 300L);

        ExportFileNamer namer = new ExportFileNamer();

        assertEquals("telemetry_session-42.json", namer.nameFor(metadata));
    }
}
