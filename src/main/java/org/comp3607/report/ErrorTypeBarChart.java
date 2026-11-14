package org.comp3607.report;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.comp3607.analytics.ReportData;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

//  Bar chart of how many times each compile error type happened.

public class ErrorTypeBarChart implements ChartRenderer {

    private static final int IMAGE_WIDTH = 700;
    private static final int IMAGE_HEIGHT = 380;
    private static final Color BAR_COLOR = Color.RED;
    private static final Color TEXT_COLOR = Color.DARK_GRAY;
    private static final Color GRID_COLOR = Color.LIGHT_GRAY;
    private static final Font TITLE_FONT = new Font("Calibri", Font.BOLD, 22);
    private static final Font TEXT_FONT = new Font("Calibri", Font.PLAIN, 16);
    private static final double UPPER_MARGIN = 0.15;

    public void render(ReportData reportData, Path target) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Long> errors = reportData.getErrorFrequency();
        for (String type : errors.keySet()) {
            dataset.addValue(errors.get(type), "Errors", type);
        }

        JFreeChart chart = ChartFactory.createBarChart("Compile Errors by Type", "",
                "Count", dataset, PlotOrientation.VERTICAL, false, false, false);
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        chart.getTitle().setFont(TITLE_FONT);
        chart.getTitle().setPaint(TEXT_COLOR);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(GRID_COLOR);
        plot.setNoDataMessage("No compile errors recorded");

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(false);
        renderer.setMaximumBarWidth(0.2);
        renderer.setSeriesPaint(0, BAR_COLOR);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(TEXT_FONT);

        NumberAxis countAxis = (NumberAxis) plot.getRangeAxis();
        countAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        countAxis.setTickLabelFont(TEXT_FONT);
        countAxis.setLabelFont(TEXT_FONT);
        countAxis.setUpperMargin(UPPER_MARGIN);
        plot.getDomainAxis().setTickLabelFont(TEXT_FONT);

        ChartUtils.saveChartAsPNG(target.toFile(), chart, IMAGE_WIDTH, IMAGE_HEIGHT);
    }
}