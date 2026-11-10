package org.comp3607.analytics;

import java.util.List;
import java.util.Map;


//  Holds every calculated value the PDF needs
 
public class ReportData {

    private final String sessionId;
    private final int eventCount;
    private final int totalCompiles;
    private final int passedCompiles;
    private final double compileSuccessRate;
    private final Map<String, Long> errorFrequency;
    private final int totalRuns;
    private final long activeTimeMs;
    private final long idleTimeMs;
    private final double compileToRunRatio;
    private final List<String> classTimeline;

    // Creates the ReportData object with all the calculated values. The ReportDataBuilder calls this constructor.

    public ReportData(String sessionId, int eventCount, int totalCompiles, int passedCompiles,double compileSuccessRate, Map<String, Long> errorFrequency, int totalRuns,
    long activeTimeMs, long idleTimeMs, double compileToRunRatio,List<String> classTimeline) {
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
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getEventCount() {
        return eventCount;
    }

    public int getTotalCompiles() {
        return totalCompiles;
    }

    public int getPassedCompiles() {
        return passedCompiles;
    }

    public int getFailedCompiles() {
        return totalCompiles - passedCompiles;
    }

    public double getCompileSuccessRate() {
        return compileSuccessRate;
    }

    public Map<String, Long> getErrorFrequency() {
        return errorFrequency;
    }

    public int getTotalRuns() {
        return totalRuns;
    }

    public long getActiveTimeMs() {
        return activeTimeMs;
    }

    public long getIdleTimeMs() {
        return idleTimeMs;
    }

    public double getCompileToRunRatio() {
        return compileToRunRatio;
    }

    public List<String> getClassTimeline() {
        return classTimeline;
    }

    public boolean isEmpty() {
        return eventCount == 0;
    }
}