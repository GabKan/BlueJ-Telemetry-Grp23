package org.comp3607.bluej;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class BlueJEditorActivityTrackerTest {

    @Test
    void recordsTypingPasteDeletionAndIdleSegments() {
        BlueJEditorActivityTracker tracker = new BlueJEditorActivityTracker(null);
        tracker.recordSample("Example", 10, 1000);
        tracker.recordSample("Example", 15, 1250);
        tracker.recordSample("Example", 40, 1500);
        tracker.recordSample("Example", 30, 1750);
        tracker.recordSample("Example", 30, 2000);
        tracker.recordSample("Example", 30, 4251);
        tracker.stop();

        Map<String, Object> activity = tracker.getEditorActivity();
        List<?> segments = (List<?>) activity.get("segments");
        assertEquals(4, segments.size());
        assertEquals("typing", ((Map<?, ?>) segments.get(0)).get("label"));
        assertEquals("paste", ((Map<?, ?>) segments.get(1)).get("label"));
        assertEquals("deletion", ((Map<?, ?>) segments.get(2)).get("label"));
        assertEquals("idle", ((Map<?, ?>) segments.get(3)).get("label"));
        assertEquals(-10, ((Map<?, ?>) segments.get(2)).get("chars"));

        Map<?, ?> summary = (Map<?, ?>) activity.get("summary");
        assertEquals(3251L, summary.get("observedMs"));
        assertEquals(3L, summary.get("editCount"));
        assertEquals(10L, ((Map<?, ?>) summary.get("deletion")).get("totalChars"));
        assertEquals(2501L, ((Map<?, ?>) summary.get("idle")).get("longestMs"));
    }
}
