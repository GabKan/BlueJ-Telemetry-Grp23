package org.comp3607.export;

import org.comp3607.model.SessionMetadata;

public class ExportFileNamer {

    public String nameFor(SessionMetadata metadata) {
        return "telemetry_" + metadata.getSessionId() + ".json";
    }
}
