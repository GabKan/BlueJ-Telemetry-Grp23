package org.comp3607.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import org.comp3607.report.EditorActivityTableBuilder;
import org.junit.jupiter.api.Test;

public class EditorActivityTableBuilderTest {

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

    private ReportData makeReportData(EditorActivity editor) {
        return new ReportData("session", 0, 0, 0, 0.0, new HashMap<String, Long>(),
                0, 0, 0, 0.0, new ArrayList<String>(), editor);
    }

    @Test
    public void build_editorActivity() {
        ReportData data = makeReportData(new EditorActivity(makeActivity()));

        String table = new EditorActivityTableBuilder().build(data);

        String expected = "Typing|21.3 s, 76 chars\n"
                + "Deleting|6.5 s, 305 chars\n"
                + "Pasting|0.2 s, 1 paste\n"
                + "Active|28.0 s\n"
                + "Idle|53.7 s, longest pause 8.2 s\n"
                + "Edits|64 edits, typing speed 214 chars/min";
        assertEquals(expected, table);
    }

    @Test
    public void build_noEditorActivity() {
        ReportData data = makeReportData(new EditorActivity());

        String table = new EditorActivityTableBuilder().build(data);

        assertEquals("Editor activity|No data recorded", table);
    }
}