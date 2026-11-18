package org.comp3607.analytics;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.comp3607.report.PdfReportGenerator;
import org.junit.jupiter.api.Test;

public class PdfReportGeneratorTest {

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
    public void generate_sampleData_createsPdfFile() throws IOException {
        long three = 3;
        long one = 1;
        Map<String, Long> errors = new HashMap<String, Long>();
        errors.put("';' expected", three);
        errors.put("cannot find symbol", one);

        List<String> timeline = new ArrayList<String>();
        timeline.add("0m 05s - Created Dog");
        timeline.add("2m 05s - Modified Dog");

        ReportData data = new ReportData("sample-session", 12, 4, 2, 0.5, errors,
                2, 180000, 60000, 2.0, timeline, new EditorActivity(makeActivity()));
        Path target = Paths.get("target", "sample-report.pdf");
        Files.createDirectories(target.getParent());

        PdfReportGenerator generator = new PdfReportGenerator();
        generator.generate(data, target);

        assertTrue(Files.exists(target));
        assertTrue(Files.size(target) > 0);
    }

    @Test
    public void generate_noEditorActivity_createsPdfFile() throws IOException {
        ReportData data = new ReportData("empty-session", 0, 0, 0, 0.0,
                new HashMap<String, Long>(), 0, 0, 0, 0.0, new ArrayList<String>(), null);
        Path target = Paths.get("target", "empty-report.pdf");
        Files.createDirectories(target.getParent());

        PdfReportGenerator generator = new PdfReportGenerator();
        generator.generate(data, target);

        assertTrue(Files.exists(target));
        assertTrue(Files.size(target) > 0);
    }
}