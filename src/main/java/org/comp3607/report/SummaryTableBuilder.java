package org.comp3607.report;

import org.comp3607.analytics.ReportData;
import org.comp3607.analytics.TimeFormatter;

// Generates the text of the compile summary 

public class SummaryTableBuilder {

    private static final double PERCENT = 100.0;

    public String build(ReportData reportData) {
        String successText = String.format("%.1f%%", reportData.getCompileSuccessRate() * PERCENT);
        String ratioText = String.format("%.2f", reportData.getCompileToRunRatio());
        String activeText = TimeFormatter.TimeFormat(reportData.getActiveTimeMs());
        String idleText = TimeFormatter.TimeFormat(reportData.getIdleTimeMs());

        String table = "";
        table = table + "Total compiles|" + reportData.getTotalCompiles() + "\n";
        table = table + "Passed compiles|" + reportData.getPassedCompiles() + "\n";
        table = table + "Failed compiles|" + reportData.getFailedCompiles() + "\n";
        table = table + "Compile success rate|" + successText + "\n";
        table = table + "Total program runs|" + reportData.getTotalRuns() + "\n";
        table = table + "Compile-to-run ratio|" + ratioText + "\n";
        table = table + "Active time|" + activeText + "\n";
        table = table + "Idle time|" + idleText;
        return table;
    }
}