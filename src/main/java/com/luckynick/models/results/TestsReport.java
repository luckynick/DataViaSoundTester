package com.luckynick.models.results;

import com.luckynick.CustomJFrame;
import com.luckynick.custom.Device;
import com.luckynick.models.profiles.SequentialTestProfile;
import com.luckynick.shared.IOClassHandling;
import com.luckynick.shared.SharedUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.luckynick.custom.Utils.Log;

@IOClassHandling(dataStorage = SharedUtils.DataStorage.SEQUENTIAL_REPORT)
public class TestsReport extends TestResult {

    public static final String LOG_TAG = "TestsReport";

    public List<SingleTestResult> resultsOfTests = new ArrayList<>();
    public SequentialTestProfile seqTestProfile;

    public static final String WEIGHTED_AVG_ROW_KEY = "Σ (success * string weight)";

    private TestsReport() {
        super();
    }

    protected TestsReport(SequentialTestProfile seqTestProfile) {
        super();
        this.seqTestProfile = seqTestProfile;
    }

    @Override
    public void setFilename() {
        setFilename("report_" + SharedUtils.getDateStringForFileName() + SharedUtils.JSON_EXTENSION);
    }

    public void showPlot() {
        CustomJFrame cFrame = new CustomJFrame("Single test: " + filename) {
            @Override
            public void addElements() {
                JPanel chartsPanelUp = new JPanel();
                chartsPanelUp.setLayout(new BoxLayout(chartsPanelUp, BoxLayout.X_AXIS));
                chartsPanelUp.add(createOneChart(seqTestProfile.peer1, false));
                chartsPanelUp.add(createOneChart(seqTestProfile.peer2, false));
                getContentPane().add(chartsPanelUp);
                if(!TestsReport.this.seqTestProfile.spectralAnalysis) {
                    JPanel chartsPanelDown = new JPanel();
                    chartsPanelDown.setLayout(new BoxLayout(chartsPanelDown, BoxLayout.X_AXIS));
                    chartsPanelDown.add(createOneChart(seqTestProfile.peer1, true));
                    chartsPanelDown.add(createOneChart(seqTestProfile.peer2, true));
                    getContentPane().add(chartsPanelDown);
                }
                JPanel infoPanel = new JPanel();
                infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
                infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
                infoPanel.add(new JLabel("Report file: " + TestsReport.super.filename));
                getContentPane().add(infoPanel);
            }
        };
        cFrame.addElements();
        cFrame.displayWindow();
    }

    private ChartPanel createOneChart(Device forDevice, boolean weightedAvg) {

        DefaultCategoryDataset resultDataset = getChartDataSet(forDevice, weightedAvg);

        // Create chart
        JFreeChart chart = ChartFactory.createLineChart(
                "Device " + forDevice.vendor + " (sender peer)", // Chart title
                "Loudness", // X-Axis Label
                "Success", // Y-Axis Label
                resultDataset,
                PlotOrientation.VERTICAL, true, false, false
        );
        chart.getCategoryPlot().getRangeAxis().setRange(0, 100);
        ((LineAndShapeRenderer)chart.getCategoryPlot().getRenderer()).setBaseShapesVisible(true);

        return new ChartPanel(chart);
    }

    DefaultCategoryDataset getChartDataSet(Device filter, boolean weightedAvg) {
        DefaultCategoryDataset result = new DefaultCategoryDataset();
        final String weightedAvgName = WEIGHTED_AVG_ROW_KEY;
        for(int i = 0; i < resultsOfTests.size(); i++){
            if(filter.macAddress.equals(resultsOfTests.get(i).senderSessionSummary.summarySource.macAddress)) {
                /* For spectral analysis rowKey is frequenciesBindingShift,
                   For usual test rowKey is message's text,
                 */
                if(seqTestProfile.spectralAnalysis) {
                    result.addValue(resultsOfTests.get(i).messageMatchPecrentage,
                            resultsOfTests.get(i).senderSessionSummary.sendParameters.frequenciesBindingShift + "",
                            ""+resultsOfTests.get(i).senderSessionSummary.sendParameters.loudnessLevel);
                    Log(LOG_TAG, i + ". Match: " + resultsOfTests.get(i).messageMatchPecrentage + "; send message: "
                            + resultsOfTests.get(i).senderSessionSummary.sendParameters.message + "; loudness: "
                            + resultsOfTests.get(i).senderSessionSummary.sendParameters.loudnessLevel+ "; recived message: "
                            + resultsOfTests.get(i).receiverSessionSummary.message);
                }
                else {
                    result.addValue(resultsOfTests.get(i).messageMatchPecrentage,
                            resultsOfTests.get(i).senderSessionSummary.sendParameters.message,
                            ""+resultsOfTests.get(i).senderSessionSummary.sendParameters.loudnessLevel);
                    Log(LOG_TAG, i + ". Match: " + resultsOfTests.get(i).messageMatchPecrentage + "; send message: "
                            + resultsOfTests.get(i).senderSessionSummary.sendParameters.message + "; loudness: "
                            + resultsOfTests.get(i).senderSessionSummary.sendParameters.loudnessLevel+ "; recived message: "
                            + resultsOfTests.get(i).receiverSessionSummary.message);
                }
            }
        }
        if(weightedAvg) {
            DefaultCategoryDataset resultWeightedAvg = new DefaultCategoryDataset();
            int lengthsOfDictionaryStrings = 0;
            for(Object rowKey: result.getRowKeys()) {
                lengthsOfDictionaryStrings += rowKey.toString().length();
            }
            for(Object colKey: result.getColumnKeys()) {
                resultWeightedAvg.setValue(0,
                        (Comparable) weightedAvgName, (Comparable) colKey);
            }
            for(Object rowKey: result.getRowKeys()) {
                for(Object colKey: result.getColumnKeys()) {
                    Number successLevel = result.getValue((Comparable) rowKey, (Comparable) colKey);
                    resultWeightedAvg.incrementValue(successLevel.doubleValue() * rowKey.toString().length(),
                            (Comparable) weightedAvgName, (Comparable) colKey);
                }
            }
            for(Object colKey: result.getColumnKeys()) {
                Number aggregatedSuccessLevel = resultWeightedAvg.getValue((Comparable) weightedAvgName, (Comparable) colKey);
                resultWeightedAvg.setValue(aggregatedSuccessLevel.doubleValue()/lengthsOfDictionaryStrings,
                        (Comparable) weightedAvgName, (Comparable) colKey);
            }
            Log(LOG_TAG, "lengthsOfDictionaryStrings: " + lengthsOfDictionaryStrings);
            result = resultWeightedAvg;
        }
        return result;
    }
}
