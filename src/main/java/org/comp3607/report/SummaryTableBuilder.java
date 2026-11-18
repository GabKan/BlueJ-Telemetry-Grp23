package org.comp3607.report;

import org.comp3607.analytics.ReportData;

//  Builds the text of the compile summary table 

public class SummaryTableBuilder {

    private static final double PERCENT = 100.0;

    public String build(ReportData reportData) {
        String successText = String.format("%.1f%%", reportData.getCompileSuccessRate() * PERCENT);

        String table = "";
        table = table + "Total compiles|" + reportData.getTotalCompiles() + "\n";
        table = table + "Passed compiles|" + reportData.getPassedCompiles() + "\n";
        table = table + "Failed compiles|" + reportData.getFailedCompiles() + "\n";
        table = table + "Compile success rate|" + successText;
        return table;
    }
}