package org.comp3607.analytics;

import java.util.List;
import java.util.Map;

import org.comp3607.api.EventSource;
import org.comp3607.model.TelemetryEvent;

/**
 * Coordinates the analytics aggregators and combines their results into one
 * {@link ReportData} instance for report generation.
 */
public class ReportDataBuilder {

    /**
     * Builds report data from the events and metadata exposed by an event
     * source, without editor-activity details.
     *
     * @param source source of session events and metadata
     * @return aggregated report data
     */
    public ReportData build(EventSource source) {
        return build(source, null);
    }

    /**
     * Builds report data by aggregating compile results, errors, executions,
     * active and idle time, class history, and optional editor activity.
     *
     * @param source source of session events and metadata
     * @param editorActivity optional editor-activity data to include
     * @return aggregated report data for the source session
     */
    public ReportData build(EventSource source, Map<String, Object> editorActivity) {
        List<TelemetryEvent> events = source.snapshot();
        String sessionId = source.metadata().getSessionId();

        CompileSuccessAggregator successAggregator = new CompileSuccessAggregator();
        ErrorFrequencyAggregator errorAggregator = new ErrorFrequencyAggregator();
        ActiveIdleTimeAggregator timeAggregator = new ActiveIdleTimeAggregator();
        CompileToRunRatioAggregator ratioAggregator = new CompileToRunRatioAggregator();
        ClassTimelineAggregator timelineAggregator = new ClassTimelineAggregator();

        int totalCompiles = successAggregator.countTotal(events);
        int passedCompiles = successAggregator.countPassed(events);
        double successRate = successAggregator.calculate(events);
        Map<String, Long> errors = errorAggregator.calculate(events);
        int totalRuns = ratioAggregator.countRuns(events);
        double ratio = ratioAggregator.calculate(events);
        List<String> timeline = timelineAggregator.calculate(events);

        Map<String, Long> times = timeAggregator.calculate(events);
        long activeTime = times.get(ActiveIdleTimeAggregator.ACTIVE_KEY);
        long idleTime = times.get(ActiveIdleTimeAggregator.IDLE_KEY);

        EditorActivity editor = new EditorActivity(editorActivity);

        return new ReportData(sessionId, events.size(), totalCompiles, passedCompiles,
                successRate, errors, totalRuns, activeTime, idleTime, ratio, timeline, editor);
    }
}