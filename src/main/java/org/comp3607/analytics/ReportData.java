package org.comp3607.analytics;

import java.util.List;
import java.util.Map;

/**
 * Aggregated session metrics consumed by the JSON and PDF report generators.
 */
public class ReportData {

    /** Identifier of the summarized session. */
    private final String sessionId;
    /** Number of telemetry events in the session. */
    private final int eventCount;
    /** Number of compile attempts. */
    private final int totalCompiles;
    /** Number of successful compile attempts. */
    private final int passedCompiles;
    /** Fraction of compile attempts that succeeded. */
    private final double compileSuccessRate;
    /** Counts of compilation errors grouped by message or type. */
    private final Map<String, Long> errorFrequency;
    /** Number of execution events. */
    private final int totalRuns;
    /** Estimated active time in milliseconds. */
    private final long activeTimeMs;
    /** Estimated idle time in milliseconds. */
    private final long idleTimeMs;
    /** Calculated ratio of compilation attempts to runs. */
    private final double compileToRunRatio;
    /** Ordered class lifecycle descriptions for the session. */
    private final List<String> classTimeline;
    /** Summary of editor activity, if available. */
    private final EditorActivity editorActivity;

    /**
     * Creates report data from precomputed session metrics.
     *
     * @param sessionId identifier of the session
     * @param eventCount number of collected events
     * @param totalCompiles number of compile attempts
     * @param passedCompiles number of successful compile attempts
     * @param compileSuccessRate fraction of successful compile attempts
     * @param errorFrequency error counts by message or type
     * @param totalRuns number of execution events
     * @param activeTimeMs estimated active time in milliseconds
     * @param idleTimeMs estimated idle time in milliseconds
     * @param compileToRunRatio calculated compile-to-run ratio
     * @param classTimeline ordered class lifecycle descriptions
     * @param editorActivity editor activity summary, or {@code null} when unavailable
     */
    public ReportData(
        String sessionId,
        int eventCount,
        int totalCompiles,
        int passedCompiles,
        double compileSuccessRate,
        Map<String, Long> errorFrequency,
        int totalRuns,
        long activeTimeMs,
        long idleTimeMs,
        double compileToRunRatio,
        List<String> classTimeline,
        EditorActivity editorActivity
    ) {
        this.sessionId = sessionId;
        this.eventCount = eventCount;
        this.totalCompiles = totalCompiles;
        this.passedCompiles = passedCompiles;
        this.compileSuccessRate = compileSuccessRate;
        this.errorFrequency = errorFrequency;
        this.totalRuns = totalRuns;
        this.activeTimeMs = activeTimeMs;
        this.idleTimeMs = idleTimeMs;
        this.compileToRunRatio = compileToRunRatio;
        this.classTimeline = classTimeline;

        if (editorActivity == null) {
            this.editorActivity = new EditorActivity();
        } else {
            this.editorActivity = editorActivity;
        }
    }

    /**
     * Returns the session identifier.
     *
     * @return session identifier
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Returns the number of telemetry events.
     *
     * @return event count
     */
    public int getEventCount() {
        return eventCount;
    }

    /**
     * Returns the number of compile attempts.
     *
     * @return total compile count
     */
    public int getTotalCompiles() {
        return totalCompiles;
    }

    /**
     * Returns the number of successful compile attempts.
     *
     * @return successful compile count
     */
    public int getPassedCompiles() {
        return passedCompiles;
    }

    /**
     * Returns the number of unsuccessful compile attempts.
     *
     * @return total compile count minus successful compile count
     */
    public int getFailedCompiles() {
        return totalCompiles - passedCompiles;
    }

    /**
     * Returns the fraction of compile attempts that succeeded.
     *
     * @return compile success rate, represented as a value from 0 to 1
     */
    public double getCompileSuccessRate() {
        return compileSuccessRate;
    }

    /**
     * Returns error counts grouped by message or type.
     *
     * @return error frequency map
     */
    public Map<String, Long> getErrorFrequency() {
        return errorFrequency;
    }

    /**
     * Returns the number of execution events.
     *
     * @return run count
     */
    public int getTotalRuns() {
        return totalRuns;
    }

    /**
     * Returns estimated active time.
     *
     * @return active time in milliseconds
     */
    public long getActiveTimeMs() {
        return activeTimeMs;
    }

    /**
     * Returns estimated idle time.
     *
     * @return idle time in milliseconds
     */
    public long getIdleTimeMs() {
        return idleTimeMs;
    }

    /**
     * Returns the calculated compile-to-run ratio.
     *
     * @return compile-to-run ratio
     */
    public double getCompileToRunRatio() {
        return compileToRunRatio;
    }

    /**
     * Returns the ordered class lifecycle descriptions.
     *
     * @return class timeline
     */
    public List<String> getClassTimeline() {
        return classTimeline;
    }

    /**
     * Returns the editor activity summary.
     *
     * @return editor activity summary, which is unavailable when no summary
     *         data was supplied
     */
    public EditorActivity getEditorActivity() {
        return editorActivity;
    }

    /**
     * Indicates whether the session has neither telemetry events nor editor
     * activity summary data.
     *
     * @return {@code true} when both event and editor activity data are empty
     */
    public boolean isEmpty() {
        return eventCount == 0 && !editorActivity.isAvailable();
    }
}