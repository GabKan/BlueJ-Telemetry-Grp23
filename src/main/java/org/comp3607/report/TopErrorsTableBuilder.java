package org.comp3607.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.comp3607.analytics.ReportData;

// Generates the text of the most frequent compile errors table.

public class TopErrorsTableBuilder {

    private static final int TOP_ERROR_COUNT = 5;

    public String build(ReportData reportData) {
        String table = "Error type|Count";

        Map<String, Long> errors = reportData.getErrorFrequency();
        List<String> alreadyUsed = new ArrayList<String>();

        for (int round = 0; round < TOP_ERROR_COUNT; round++) {
            String bestType = null;
            long bestCount = 0;

            for (String type : errors.keySet()) {
                long count = errors.get(type);
                if (!alreadyUsed.contains(type) && count > bestCount) {
                    bestType = type;
                    bestCount = count;
                }
            }

            if (bestType != null) {
                alreadyUsed.add(bestType);
                table = table + "\n" + bestType + "|" + bestCount;
            }
        }

        if (alreadyUsed.isEmpty()) {
            table = table + "\nNo compile errors recorded|0";
        }
        return table;
    }
}