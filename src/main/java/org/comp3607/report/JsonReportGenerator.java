package org.comp3607.report;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.comp3607.analytics.EditorActivity;
import org.comp3607.analytics.ReportData;
import org.comp3607.analytics.ReportDataBuilder;
import org.comp3607.api.EventSource;
import org.comp3607.api.Exporter;
import org.comp3607.json.TelemetryJsonSerialiser;
import org.comp3607.model.SessionMetadata;

/**
 * Builds a calculated session summary and writes it as a JSON report.
 */
public class JsonReportGenerator implements Exporter {

    /**
     * Creates a JSON report generator.
     */
    public JsonReportGenerator() {
    }

    /**
     * Builds the standard JSON report filename for a session.
     *
     * @param sessionId session identifier
     * @return filename in the form {@code report_<sessionId>.json}
     */
    public static String reportFileName(String sessionId) {
        return "report_" + sessionId + ".json";
    }

    /**
     * Exports a JSON summary without editor-activity data.
     *
     * @param source source of events and session metadata
     * @param target destination file
     * @throws IOException if the report cannot be written
     */
    @Override
    public void export(EventSource source, Path target) throws IOException {
        export(source, null, target);
    }

    /**
     * Builds and writes a JSON summary of the session, including optional
     * editor-activity totals.
     *
     * @param source source of events and session metadata
     * @param editorActivity optional editor-activity data
     * @param target destination file
     * @throws IOException if the report cannot be written
     */
    public void export(
        EventSource source,
        Map<String, Object> editorActivity,
        Path target
    ) throws IOException {
        SessionMetadata metadata = source.metadata();
        ReportData reportData = new ReportDataBuilder().build(source, editorActivity);

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
        summary.put("editorActivity", buildEditorActivitySummary(
            reportData.getEditorActivity()
        ));

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

    private Map<String, Object> buildEditorActivitySummary(EditorActivity editorActivity) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("available", editorActivity.isAvailable());
        summary.put("typingMs", editorActivity.getTypingMs());
        summary.put("typingChars", editorActivity.getTypingChars());
        summary.put("deletionMs", editorActivity.getDeletionMs());
        summary.put("deletionChars", editorActivity.getDeletionChars());
        summary.put("pasteMs", editorActivity.getPasteMs());
        summary.put("pasteCount", editorActivity.getPasteCount());
        summary.put("activeMs", editorActivity.getActiveMs());
        summary.put("idleMs", editorActivity.getIdleMs());
        summary.put("longestIdleMs", editorActivity.getLongestIdleMs());
        summary.put("editCount", editorActivity.getEditCount());
        summary.put("typingSpeedCharsPerMinute", editorActivity.getTypingSpeed());
        return summary;
    }
}
