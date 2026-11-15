package org.comp3607.report;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.comp3607.analytics.ReportData;
import org.comp3607.analytics.ReportDataBuilder;
import org.comp3607.api.EventSource;
import org.comp3607.api.Exporter;
import org.comp3607.json.TelemetryJsonSerialiser;
import org.comp3607.model.SessionMetadata;

public class JsonReportGenerator implements Exporter {

    public static String reportFileName(String sessionId) {
        return "report_" + sessionId + ".json";
    }

    @Override
    public void export(EventSource source, Path target) throws IOException {
        SessionMetadata metadata = source.metadata();
        ReportData reportData = new ReportDataBuilder().build(source);

        Map<String, Object> session = new HashMap<>();
        session.put("sessionId", metadata.getSessionId());
        session.put("studentId", metadata.getStudentId());
        session.put("startMs", metadata.getStartMs());
        session.put("endMs", metadata.getEndMs());
        session.put("durationMs", metadata.getEndMs() - metadata.getStartMs());

        Map<String, Object> summary = new HashMap<>();
        summary.put("eventCount", reportData.getEventCount());
        summary.put("totalCompiles", reportData.getTotalCompiles());
        summary.put("passedCompiles", reportData.getPassedCompiles());
        summary.put("failedCompiles", reportData.getFailedCompiles());
        summary.put("compileSuccessRate", reportData.getCompileSuccessRate());
        summary.put("errorFrequency", reportData.getErrorFrequency());
        summary.put("totalRuns", reportData.getTotalRuns());
        summary.put("compileToRunRatio", reportData.getCompileToRunRatio());
        summary.put("activeTimeMs", reportData.getActiveTimeMs());
        summary.put("idleTimeMs", reportData.getIdleTimeMs());
        summary.put("classTimeline", reportData.getClassTimeline());

        Map<String, Object> report = new HashMap<>();
        report.put("session", session);
        report.put("summary", summary);

        String json = new TelemetryJsonSerialiser().serialise(report);
        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(target, json, StandardCharsets.UTF_8);
    }
}
