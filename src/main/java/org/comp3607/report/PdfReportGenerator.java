package org.comp3607.report;

import java.io.IOException;
import java.nio.file.Path;

import org.comp3607.analytics.ReportData;
import org.comp3607.api.EventSource;
import org.comp3607.api.Exporter;

public class PdfReportGenerator implements Exporter {

    public void export(EventSource source, Path target) throws IOException {
        throw new UnsupportedOperationException("TODO Member C");
    }

    public void generate(ReportData reportData, Path target) throws IOException {
        throw new UnsupportedOperationException("TODO Member C");
    }
}
