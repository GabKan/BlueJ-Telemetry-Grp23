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

    @Test
    public void generate_sampleData_createsPdfFile() throws IOException {
        long three = 3;
        long one = 1;
        Map<String, Long> errors = new HashMap<String, Long>();
        errors.put("missing_semicolon", three);
        errors.put("undefined_variable", one);

        List<String> timeline = new ArrayList<String>();
        timeline.add("0m 05s - Created Dog");
        timeline.add("2m 05s - Modified Dog");

        ReportData data = new ReportData("sample-session", 12, 4, 2, 0.5, errors,
                2, 180000, 60000, 2.0, timeline);
        Path target = Paths.get("target", "sample-report.pdf");
        Files.createDirectories(target.getParent());

        PdfReportGenerator generator = new PdfReportGenerator();
        generator.generate(data, target);

        assertTrue(Files.exists(target));
        assertTrue(Files.size(target) > 0);
    }
}