package org.comp3607.json;

import java.util.HashMap;
import java.util.Map;

import org.comp3607.api.EventSource;
import org.comp3607.model.SessionMetadata;

public class SessionJsonBuilder {

    private static final double ROUND = 10000.0;

    public Map<String, Object> build(EventSource source) {
        SessionMetadata meta = source.metadata();

        double duration = (meta.getEndMs() - meta.getStartMs()) / 1000.0;

        Map<String, Object> session = new HashMap<>();
        session.put("session_id", meta.getSessionId());
        session.put("user_id", meta.getStudentId());
        session.put("start_time", meta.getStartMs());
        session.put("end_time", meta.getEndMs());
        session.put("session_duration_seconds", Math.round(duration * ROUND) / ROUND);
        return session;
    }
}
