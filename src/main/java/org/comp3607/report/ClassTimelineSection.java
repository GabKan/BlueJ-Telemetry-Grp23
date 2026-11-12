package org.comp3607.report;

import java.util.List;

import org.comp3607.analytics.ReportData;


// Generates the class timeline text.

public class ClassTimelineSection {

    private static final int MAX_LINES = 12;

    public String build(ReportData reportData) {
        List<String> timeline = reportData.getClassTimeline();

        if (timeline.isEmpty()) {
            return "No classes were created, modified or deleted.";
        }

        String section = "";
        for (int i = 0; i < timeline.size() && i < MAX_LINES; i++) {
            section = section + timeline.get(i) + "\n";
        }

        if (timeline.size() > MAX_LINES) {
            int hidden = timeline.size() - MAX_LINES;
            section = section + "... and " + hidden + " more events";
        }
        return section;
    }
}