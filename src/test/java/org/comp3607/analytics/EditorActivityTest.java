package org.comp3607.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;
import org.junit.jupiter.api.Test;

public class EditorActivityTest {

    private Map<String, Object> makeSection(long count, long totalMs, String key, long value) {
        Map<String, Object> section = new HashMap<String, Object>();
        section.put("count", count);
        section.put("totalMs", totalMs);
        section.put(key, value);
        return section;
    }

    private Map<String, Object> makeActivity() {
        long editCount = 64;
        Map<String, Object> summary = new HashMap<String, Object>();
        summary.put("typing", makeSection(10, 21266, "totalChars", 76));
        summary.put("deletion", makeSection(7, 6541, "totalChars", 305));
        summary.put("paste", makeSection(1, 234, "totalChars", 110));
        summary.put("idle", makeSection(11, 53658, "longestMs", 8248));
        summary.put("editCount", editCount);

        Map<String, Object> activity = new HashMap<String, Object>();
        activity.put("summary", summary);
        return activity;
    }

    @Test
    public void editorActivity_summaryMap() {
        EditorActivity editor = new EditorActivity(makeActivity());

        assertTrue(editor.isAvailable());
        assertEquals(21266, editor.getTypingMs());
        assertEquals(76, editor.getTypingChars());
        assertEquals(6541, editor.getDeletionMs());
        assertEquals(305, editor.getDeletionChars());
        assertEquals(234, editor.getPasteMs());
        assertEquals(1, editor.getPasteCount());
        assertEquals(53658, editor.getIdleMs());
        assertEquals(8248, editor.getLongestIdleMs());
        assertEquals(64, editor.getEditCount());
        assertEquals(28041, editor.getActiveMs());
    }

    @Test
    public void editorActivity_typingSpeed() {
        EditorActivity editor = new EditorActivity(makeActivity());

        assertEquals(214.4, editor.getTypingSpeed(), 0.1);
    }

    @Test
    public void editorActivity_noMap() {
        EditorActivity editor = new EditorActivity();

        assertFalse(editor.isAvailable());
        assertEquals(0, editor.getTypingMs());
        assertEquals(0, editor.getActiveMs());
        assertEquals(0.0, editor.getTypingSpeed(), 0.0001);
    }

    @Test
    public void editorActivity_emptyMap() {
        EditorActivity editor = new EditorActivity(new HashMap<String, Object>());

        assertFalse(editor.isAvailable());
        assertEquals(0, editor.getEditCount());
    }
}