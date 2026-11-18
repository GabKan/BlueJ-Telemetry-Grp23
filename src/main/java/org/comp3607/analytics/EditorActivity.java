package org.comp3607.analytics;

import java.util.HashMap;
import java.util.Map;

//  Holds the editor activity totals (typing, deleting, pasting and idle) for the report

public class EditorActivity {

    private static final double MILLIS_PER_MINUTE = 60000.0;

    private final boolean available;
    private final long typingMs;
    private final long typingChars;
    private final long deletionMs;
    private final long deletionChars;
    private final long pasteMs;
    private final long pasteCount;
    private final long idleMs;
    private final long longestIdleMs;
    private final long editCount;

    public EditorActivity() {
        this(null);
    }

    public EditorActivity(Map<String, Object> activity) {
        Map summary = getMap(activity, "summary");
        Map typing = getMap(summary, "typing");
        Map deletion = getMap(summary, "deletion");
        Map paste = getMap(summary, "paste");
        Map idle = getMap(summary, "idle");

        this.available = !summary.isEmpty();
        this.typingMs = getLong(typing, "totalMs");
        this.typingChars = getLong(typing, "totalChars");
        this.deletionMs = getLong(deletion, "totalMs");
        this.deletionChars = getLong(deletion, "totalChars");
        this.pasteMs = getLong(paste, "totalMs");
        this.pasteCount = getLong(paste, "count");
        this.idleMs = getLong(idle, "totalMs");
        this.longestIdleMs = getLong(idle, "longestMs");
        this.editCount = getLong(summary, "editCount");
    }

    public boolean isAvailable() {
        return available;
    }

    public long getTypingMs() {
        return typingMs;
    }

    public long getTypingChars() {
        return typingChars;
    }

    public long getDeletionMs() {
        return deletionMs;
    }

    public long getDeletionChars() {
        return deletionChars;
    }

    public long getPasteMs() {
        return pasteMs;
    }

    public long getPasteCount() {
        return pasteCount;
    }

    public long getIdleMs() {
        return idleMs;
    }

    public long getLongestIdleMs() {
        return longestIdleMs;
    }

    public long getEditCount() {
        return editCount;
    }

    public long getActiveMs() {
        return typingMs + deletionMs + pasteMs;
    }

    public double getTypingSpeed() {
        if (typingMs == 0) {
            return 0.0;
        }
        return typingChars / (typingMs / MILLIS_PER_MINUTE);
    }

    private static Map getMap(Map map, String key) {
        if (map != null) {
            Object value = map.get(key);
            if (value instanceof Map) {
                return (Map) value;
            }
        }
        return new HashMap();
    }

    private static long getLong(Map map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0;
    }
}