package org.comp3607.collector;

import org.comp3607.model.SessionMetadata;

final class CollectorTestSupport {
    private static final long SESSION_START_MS = 800L;
    private static final long SESSION_END_MS = 0L;

    static BufferedEventSink newEventSink() {
        return new BufferedEventSink(
            new SessionMetadata("session-1", "unknown", SESSION_START_MS, SESSION_END_MS)
        );
    }
}
