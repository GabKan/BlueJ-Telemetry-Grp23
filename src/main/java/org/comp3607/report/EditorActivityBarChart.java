package org.comp3607.report;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;

import org.comp3607.analytics.EditorActivity;
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

// Bar chart with one bar each for the time spent typing, deleting and pasting, in seconds.

public class EditorActivityBarChart implements ChartRenderer {

    private static final int IMAGE_WIDTH = 700;
    private static final int IMAGE_HEIGHT = 380;
    private static final double MILLIS_PER_SECOND = 1000.0;
    private static final double UPPER_MARGIN = 0.15;
    private static final Color TYPING_COLOR = Color.BLUE;
    private static final Color DELETING_COLOR = Color.ORANGE;
    private static final Color PASTING_COLOR = Color.CYAN;
    private static final Color TEXT_COLOR = Color.DARK_GRAY;
    private static final Color GRID_COLOR = Color.LIGHT_GRAY;
    private static final Font TITLE_FONT = new Font("Calibri", Font.BOLD, 22);
    private static final Font TEXT_FONT = new Font("Calibri", Font.PLAIN, 16);

    public void render(ReportData reportData, Path target) throws IOException {
        EditorActivity editor = reportData.getEditorActivity();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        if (editor.isAvailable()) {
            dataset.addValue(editor.getTypingMs() / MILLIS_PER_SECOND, "Typing", "Editor");
            dataset.addValue(editor.getDeletionMs() / MILLIS_PER_SECOND, "Deleting", "Editor");
            dataset.addValue(editor.getPasteMs() / MILLIS_PER_SECOND, "Pasting", "Editor");
        }

        JFreeChart chart = ChartFactory.createBarChart("Editor Activity", "",
                "Seconds", dataset, PlotOrientation.VERTICAL, true, false, false);
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
        plot.setNoDataMessage("No editor activity recorded");

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
        renderer.setSeriesPaint(0, TYPING_COLOR);
        renderer.setSeriesPaint(1, DELETING_COLOR);
        renderer.setSeriesPaint(2, PASTING_COLOR);
        renderer.setDefaultItemLabelGenerator(
                new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("0.0")));
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(TEXT_FONT);

        ChartUtils.saveChartAsPNG(target.toFile(), chart, IMAGE_WIDTH, IMAGE_HEIGHT);
    }
}