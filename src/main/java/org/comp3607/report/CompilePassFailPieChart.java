package org.comp3607.report;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.Path;

import org.comp3607.analytics.ReportData;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

// Pie chart of passed compiles (green) and failed compiles (red).

public class CompilePassFailPieChart implements ChartRenderer {

    private static final int IMAGE_WIDTH = 700;
    private static final int IMAGE_HEIGHT = 380;
    private static final Color PASSED_COLOR = Color.GREEN;
    private static final Color FAILED_COLOR = Color.RED;
    private static final Color TEXT_COLOR = Color.DARK_GRAY;
    private static final Font TITLE_FONT = new Font("Calibri", Font.BOLD, 22);
    private static final Font TEXT_FONT = new Font("Calibri", Font.PLAIN, 16);

    public void render(ReportData reportData, Path target) throws IOException {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Passed", reportData.getPassedCompiles());
        dataset.setValue("Failed", reportData.getFailedCompiles());

        JFreeChart chart = ChartFactory.createPieChart("Compile Results", dataset,
                false, false, false);
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        chart.getTitle().setFont(TITLE_FONT);
        chart.getTitle().setPaint(TEXT_COLOR);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);
        plot.setSectionOutlinesVisible(false);
        plot.setIgnoreZeroValues(true);
        plot.setSectionPaint("Passed", PASSED_COLOR);
        plot.setSectionPaint("Failed", FAILED_COLOR);

        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {1} ({2})"));
        plot.setLabelFont(TEXT_FONT);
        plot.setLabelBackgroundPaint(null);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setNoDataMessage("No compiles recorded");

        ChartUtils.saveChartAsPNG(target.toFile(), chart, IMAGE_WIDTH, IMAGE_HEIGHT);
    }
}