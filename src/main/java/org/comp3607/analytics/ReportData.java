package org.comp3607.analytics;

import java.util.*;

public class ReportData {

    private final double compileSuccessRate;
    private final Map<String, Long> errorFrequency;
    private final long activeTimeMs;
    private final long idleTimeMs;
    private final double compileToRunRatio;
    private final List<String> classTimeline;

    public ReportData(
        double compileSuccessRate,
        Map<String, Long> errorFrequency,
        long activeTimeMs,
        long idleTimeMs,
        double compileToRunRatio,
        List<String> classTimeline
    ) {
        this.compileSuccessRate = compileSuccessRate;
        this.errorFrequency = errorFrequency;
        this.activeTimeMs = activeTimeMs;
        this.idleTimeMs = idleTimeMs;
        this.compileToRunRatio = compileToRunRatio;
        this.classTimeline = classTimeline;
    }

    public double getCompileSuccessRate() {
        return compileSuccessRate;
    }

    public Map<String, Long> getErrorFrequency() {
        return errorFrequency;
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
}
