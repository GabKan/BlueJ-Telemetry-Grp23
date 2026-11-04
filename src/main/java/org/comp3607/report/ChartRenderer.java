package org.comp3607.report;

import java.io.IOException;
import java.nio.file.Path;

import org.comp3607.analytics.ReportData;

public interface ChartRenderer {
    void render(ReportData reportData, Path target) throws IOException;
}
