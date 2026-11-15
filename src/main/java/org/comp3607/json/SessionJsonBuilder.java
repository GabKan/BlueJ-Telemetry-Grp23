package org.comp3607.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.comp3607.api.EventSource;
import org.comp3607.model.EventType;
import org.comp3607.model.SessionMetadata;
import org.comp3607.model.TelemetryEvent;

public class SessionJsonBuilder {

    public Map<String, Object> build(EventSource source) {
        SessionMetadata metadata = source.metadata();
        List<TelemetryEvent> events = source.snapshot();
        int compilationAttempts = 0;

        for (TelemetryEvent event : events) {
            if (event.getType() == EventType.COMPILE_FINISH) {
                compilationAttempts++;
            }
        }

        Map<String, Object> session = new HashMap<>();
        session.put("sessionId", metadata.getSessionId());
        session.put("studentId", metadata.getStudentId());
        session.put("startMs", metadata.getStartMs());
        session.put("endMs", metadata.getEndMs());
        session.put("durationMs", metadata.getEndMs() - metadata.getStartMs());
        session.put("totalCompilationAttempts", compilationAttempts);

        Map<String, Object> result = new HashMap<>();
        result.put("session", session);
        return result;
    }
}
