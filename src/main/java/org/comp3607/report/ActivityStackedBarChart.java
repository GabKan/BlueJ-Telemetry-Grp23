package org.comp3607.report;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;

import org.comp3607.analytics.ReportData;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.category.DefaultCategoryDataset;


// Bar chart with one bar for active time (blue) and one for idle time (grey) in minutes.

public class ActivityStackedBarChart implements ChartRenderer {

    private static final int IMAGE_WIDTH = 700;
    private static final int IMAGE_HEIGHT = 380;
    private static final double MILLIS_PER_MINUTE = 60000.0;
    private static final Color ACTIVE_COLOR = Color.BLUE;
    private static final Color IDLE_COLOR = Color.GRAY;
    private static final Color TEXT_COLOR = Color.DARK_GRAY;
    private static final Color GRID_COLOR = Color.LIGHT_GRAY;
    private static final Font TITLE_FONT = new Font("Calibri", Font.BOLD, 22);
    private static final Font TEXT_FONT = new Font("Calibri", Font.PLAIN, 16);
    private static final double UPPER_MARGIN = 0.15;

    public void render(ReportData reportData, Path target) throws IOException {
        double activeMinutes = reportData.getActiveTimeMs() / MILLIS_PER_MINUTE;
        double idleMinutes = reportData.getIdleTimeMs() / MILLIS_PER_MINUTE;

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(activeMinutes, "Active", "Session");
        dataset.addValue(idleMinutes, "Idle", "Session");

        JFreeChart chart = ChartFactory.createBarChart("Active vs Idle Time", "",
                "Minutes", dataset, PlotOrientation.VERTICAL, true, false, false);
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);
        chart.getTitle().setFont(TITLE_FONT);
        chart.getTitle().setPaint(TEXT_COLOR);

        chart.getLegend().setPosition(RectangleEdge.BOTTOM);
        chart.getLegend().setFrame(BlockBorder.NONE);
        chart.getLegend().setItemFont(TEXT_FONT);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(GRID_COLOR);

        plot.getDomainAxis().setVisible(false);
        plot.getRangeAxis().setTickLabelFont(TEXT_FONT);
        plot.getRangeAxis().setLabelFont(TEXT_FONT);
        plot.getRangeAxis().setUpperMargin(UPPER_MARGIN);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(false);
        renderer.setMaximumBarWidth(0.15);
        renderer.setItemMargin(0.1);
        renderer.setSeriesPaint(0, ACTIVE_COLOR);
        renderer.setSeriesPaint(1, IDLE_COLOR);
        renderer.setDefaultItemLabelGenerator(
                new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("0.0")));
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(TEXT_FONT);

        ChartUtils.saveChartAsPNG(target.toFile(), chart, IMAGE_WIDTH, IMAGE_HEIGHT);
    }
}