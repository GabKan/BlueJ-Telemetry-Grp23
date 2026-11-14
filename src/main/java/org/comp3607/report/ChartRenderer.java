package org.comp3607.report;

import java.io.IOException;
import java.nio.file.Path;

import org.comp3607.analytics.ReportData;

// Defines the rendering contract for report charts

public interface ChartRenderer {

// Draws the chart from the report data and saves it as a PNG at the target path.

    void render(ReportData reportData, Path target) throws IOException;
}