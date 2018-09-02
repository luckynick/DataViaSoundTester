package com.luckynick.models.results;

import com.luckynick.CustomJFrame;
import com.luckynick.Utils;
import com.luckynick.models.IOClassHandling;
import com.luckynick.models.ManageableField;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;

@IOClassHandling(dataStorage = Utils.DataStorage.RESULTS)
public class TestsReport extends TestResult {

    @ManageableField(editable = false)
    String testDateString = Utils.getDateStringForFileName();

    SingleTestResult[] resultsOfTests;

    protected TestsReport(SingleTestResult[] resultsOfTests) {
        super();
        this.resultsOfTests = resultsOfTests;
    }

    public void drawPlot() {
        FastScatterPlot plot = new FastScatterPlot();
        plot.setData(getChartData());
        JFreeChart chart = ChartFactory.createLineChart("Decoding accuracy", "Precission",
                "Test number", getChartDataSet(),
                PlotOrientation.HORIZONTAL, false, false, false);
        CustomJFrame frame = new CustomJFrame("Statistics") {
            @Override
            protected void addElements() {
                Canvas imageCanvas = new Canvas();
                imageCanvas.paint(chart.createBufferedImage(600, 400).createGraphics());
                getContentPane().add(imageCanvas);
            }
        };

        frame.displayWindow();
    }

    private float[][] getChartData() {
        if(resultsOfTests == null)
            throw new IllegalStateException("Chart data is missing.");
        float[][] result = new float[resultsOfTests.length][];
        for(int i = 0; i < resultsOfTests.length; i++){
            float[] x = new float[2];
            x[0] = i;
            x[1] = resultsOfTests[i].valueForPlot();
            result[i] = x;
        }
        return result;
    }

    private CategoryDataset getChartDataSet() {
        DefaultCategoryDataset result = new DefaultCategoryDataset();
        Comparable c = new Comparable() {
            @Override
            public int compareTo(Object o) {
                return 1;
            }
        };
        for(int i = 0; i < resultsOfTests.length; i++){
            result.addValue(resultsOfTests[i].valueForPlot(), c, c);
        }
        return result;
    }
}
