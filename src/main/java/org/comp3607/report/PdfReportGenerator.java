package org.comp3607.report;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.comp3607.analytics.ReportData;
import org.comp3607.analytics.ReportDataBuilder;
import org.comp3607.analytics.TimeFormatter;
import org.comp3607.api.EventSource;
import org.comp3607.api.Exporter;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;


//  Single entry point for the PDF report: calculates the data, draws the charts and tables and writes the two-page PDF. 

public class PdfReportGenerator implements Exporter {

    private static final Logger LOGGER = Logger.getLogger(PdfReportGenerator.class.getName());

    private static final float CHART_WIDTH = 350;
    private static final float CHART_HEIGHT = 190;
    private static final int TITLE_SIZE = 18;
    private static final int HEADING_SIZE = 13;
    private static final int TEXT_SIZE = 11;
    private static final int TIMELINE_SIZE = 10;
    private static final int TABLE_COLUMNS = 2;
    private static final double PERCENT = 100.0;

    public static String reportFileName(String sessionId) {
        return "report_" + sessionId + ".pdf";
    }

// Calculates the report data from the event source and writes the PDF to the target.

    public void export(EventSource source, Path target) throws IOException {
        ReportDataBuilder builder = new ReportDataBuilder();
        ReportData reportData = builder.build(source);
        generate(reportData, target);
    }

    // Writes PDF for the given report data.

    public void generate(ReportData reportData, Path target) throws IOException {
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(target.toFile()));
            document.open();
            addPageOne(document, reportData);
            document.newPage();
            addPageTwo(document, reportData);
        } catch (DocumentException e) {
            throw new IOException("Could not create the PDF report", e);
        } finally {
            document.close();
        }
    }

    // Page 1

    private void addPageOne(Document document, ReportData reportData) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, TITLE_SIZE);
        Font textFont = FontFactory.getFont(FontFactory.HELVETICA, TEXT_SIZE);

        document.add(new Paragraph("BlueJ Telemetry Report", titleFont));
        document.add(new Paragraph("Session: " + reportData.getSessionId(), textFont));
        document.add(new Paragraph(buildSummaryText(reportData), textFont));

        addChart(document, new CompilePassFailPieChart(), reportData, "pie");
        addChart(document, new ErrorTypeBarChart(), reportData, "bar");
        addChart(document, new ActivityStackedBarChart(), reportData, "stacked");
    }

    // Page 2

    private void addPageTwo(Document document, ReportData reportData) throws DocumentException {
        Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, HEADING_SIZE);
        Font textFont = FontFactory.getFont(FontFactory.HELVETICA, TIMELINE_SIZE);

        document.add(new Paragraph("Compile Summary", headingFont));
        addTextTable(document, new SummaryTableBuilder().build(reportData));

        document.add(new Paragraph("\nMost Frequent Errors", headingFont));
        addTextTable(document, new TopErrorsTableBuilder().build(reportData));

        document.add(new Paragraph("\nClass Timeline", headingFont));
        document.add(new Paragraph(new ClassTimelineSection().build(reportData), textFont));
    }

    private void addTextTable(Document document, String tableText) throws DocumentException {
        PdfPTable table = new PdfPTable(TABLE_COLUMNS);
        String[] lines = tableText.split("\n");

        for (String line : lines) {
            String[] cells = line.split("\\|", TABLE_COLUMNS);
            table.addCell(new Phrase(cells[0]));
            table.addCell(new Phrase(cells[1]));
        }
        document.add(table);
    }

    private String buildSummaryText(ReportData reportData) {
        if (reportData.isEmpty()) {
            return "No activity was recorded in this session.";
        }
        String rateText = String.format("%.1f%%", reportData.getCompileSuccessRate() * PERCENT);
        String activeText = TimeFormatter.TimeFormat(reportData.getActiveTimeMs());
        String idleText = TimeFormatter.TimeFormat(reportData.getIdleTimeMs());
        return "The student compiled " + reportData.getTotalCompiles() + " times and "
                + reportData.getPassedCompiles() + " passed (" + rateText + "). Active time was "
                + activeText + " and idle time was " + idleText + ".";
    }

    // Error handling if chart doesn't appear log it and the pdf still generates

    private void addChart(Document document, ChartRenderer renderer, ReportData reportData,
            String name) throws DocumentException {
        Path imageFile = null;
        try {
            imageFile = Files.createTempFile(name, ".png");
            renderer.render(reportData, imageFile);

            Image image = Image.getInstance(imageFile.toString());
            image.scaleToFit(CHART_WIDTH, CHART_HEIGHT);
            image.setAlignment(Element.ALIGN_CENTER);
            document.add(image);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not draw the " + name + " chart", e);
            document.add(new Paragraph("The " + name + " chart could not be drawn."));
        } finally {
            deleteQuietly(imageFile);
        }
    }

    private void deleteQuietly(Path file) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not delete the temporary chart file", e);
        }
    }
}