package org.comp3607.analytics;

import java.util.HashMap;
import java.util.Map;

/**
 * Parsed summary of editor activity collected from editor text-length samples.
 * It exposes aggregate totals for reports rather than source text or keystrokes.
 */
public class EditorActivity {

    /** Number of milliseconds in a minute, used to calculate typing speed. */
    private static final double MILLIS_PER_MINUTE = 60000.0;

    /** Whether editor activity summary data was supplied. */
    private final boolean available;
    /** Total time classified as typing, in milliseconds. */
    private final long typingMs;
    /** Total positive text-length change classified as typing. */
    private final long typingChars;
    /** Total time classified as deletion, in milliseconds. */
    private final long deletionMs;
    /** Total characters removed during deletion segments. */
    private final long deletionChars;
    /** Total time classified as pasting, in milliseconds. */
    private final long pasteMs;
    /** Number of paste segments. */
    private final long pasteCount;
    /** Total time classified as idle, in milliseconds. */
    private final long idleMs;
    /** Longest recorded idle segment, in milliseconds. */
    private final long longestIdleMs;
    /** Number of sampled edit intervals classified as typing, deletion, or paste. */
    private final long editCount;

    /**
     * Creates an unavailable, zero-valued editor-activity summary.
     */
    public EditorActivity() {
        this(null);
    }

    /**
     * Creates a summary from the editor activity map's {@code summary}
     * section. Missing or nonnumeric totals default to zero.
     *
     * @param activity serialized editor-activity data, or {@code null} when
     *                  no editor summary is available
     */
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

    /**
     * Indicates whether a summary section was present in the input data.
     *
     * @return {@code true} when editor activity summary data is available
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Returns total typing time.
     *
     * @return typing time in milliseconds
     */
    public long getTypingMs() {
        return typingMs;
    }

    /**
     * Returns the total text-length increase classified as typing.
     *
     * @return typed character count
     */
    public long getTypingChars() {
        return typingChars;
    }

    /**
     * Returns total deletion time.
     *
     * @return deletion time in milliseconds
     */
    public long getDeletionMs() {
        return deletionMs;
    }

    /**
     * Returns the total number of characters removed.
     *
     * @return deleted character count
     */
    public long getDeletionChars() {
        return deletionChars;
    }

    /**
     * Returns total time classified as pasting.
     *
     * @return paste time in milliseconds
     */
    public long getPasteMs() {
        return pasteMs;
    }

    /**
     * Returns the number of paste segments.
     *
     * @return paste count
     */
    public long getPasteCount() {
        return pasteCount;
    }

    /**
     * Returns total editor idle time.
     *
     * @return idle time in milliseconds
     */
    public long getIdleMs() {
        return idleMs;
    }

    /**
     * Returns the duration of the longest idle segment.
     *
     * @return longest idle duration in milliseconds
     */
    public long getLongestIdleMs() {
        return longestIdleMs;
    }

    /**
     * Returns the number of sampled edit intervals.
     *
     * @return edit interval count
     */
    public long getEditCount() {
        return editCount;
    }

    /**
     * Returns the combined typing, deletion, and paste durations.
     *
     * @return active editing time in milliseconds
     */
    public long getActiveMs() {
        return typingMs + deletionMs + pasteMs;
    }

    /**
     * Calculates average typing speed from typed characters and typing time.
     *
     * @return characters per minute, or zero when typing time is zero
     */
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