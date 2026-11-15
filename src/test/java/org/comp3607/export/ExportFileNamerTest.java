package org.comp3607.export;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ExportFileNamerTest {

    @Test
    void nameFor_returnsTelemetryPlusSessionId() {
        ExportFileNamer namer = new ExportFileNamer();

        String name = namer.nameFor(SampleSession.goodSession().getSessionMetadata());

        assertEquals("telemetry_good_session.json", name);
    }
}
