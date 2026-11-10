package org.comp3607.analytics;

import java.util.List;
import java.util.Map;

import org.comp3607.api.EventSource;
import org.comp3607.model.TelemetryEvent;


//  Runs every metric aggregator and puts the answers into one ReportData object.
 
public class ReportDataBuilder {

    // Reads the events from the source, calculates all metrics and returns the results

    public ReportData build(EventSource source) {
        
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

        return new ReportData(sessionId, events.size(), totalCompiles, passedCompiles,
                successRate, errors, totalRuns, activeTime, idleTime, ratio, timeline);
    }
}