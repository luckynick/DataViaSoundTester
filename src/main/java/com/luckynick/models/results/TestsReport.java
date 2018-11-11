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
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.ArrayList;
import java.util.List;

import static com.luckynick.custom.Utils.Log;

@IOClassHandling(dataStorage = SharedUtils.DataStorage.SEQUENTIAL_REPORT)
public class TestsReport extends TestResult {

    public static final String LOG_TAG = "TestsReport";

    public List<SingleTestResult> resultsOfTests = new ArrayList<>();
    public SequentialTestProfile seqTestProfile;

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

    public void showPlot(Device forDevice) {
        DefaultCategoryDataset datasetPeer1 = getChartDataSet(forDevice);
        // Create chart
        JFreeChart chart = ChartFactory.createLineChart(
                "Device " + forDevice.vendor + " (message sender)", // Chart title
                "Nth", // X-Axis Label
                "Success", // Y-Axis Label
                datasetPeer1,
                PlotOrientation.VERTICAL, true, false, false
        );

        /*JFrame frame = new JFrame("Test chart");
        ChartPanel chartPanel = new ChartPanel(chart);
        JPanel chartWrapper = new JPanel();
        chartWrapper.add(chartPanel);
        JPanel globalPanel = new JPanel();
        globalPanel.add(chartWrapper);
        globalPanel.add(new JLabel("Report file: " + filename));
        frame.setContentPane(globalPanel);

        SwingUtilities.invokeLater(() -> {
            frame.setAlwaysOnTop(true);
            frame.pack();
            frame.setSize(600, 400);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });*/

        CustomJFrame cFrame = new CustomJFrame("Test chart: " + filename) {
            @Override
            public void addElements() {
                JPanel chartsPanel = new JPanel();
                chartsPanel.setLayout(new BoxLayout(chartsPanel, BoxLayout.X_AXIS));
                chartsPanel.add(new ChartPanel(chart));
                getContentPane().add(chartsPanel);
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

    private float[][] getChartData() {
        if(resultsOfTests == null)
            throw new IllegalStateException("Chart data is missing.");
        float[][] result = new float[resultsOfTests.size()][];
        for(int i = 0; i < resultsOfTests.size(); i++){
            float[] x = new float[2];
            x[0] = i;
            x[1] = resultsOfTests.get(i).valueForPlot();
            result[i] = x;
        }
        return result;
    }

    protected DefaultCategoryDataset getChartDataSet(Device filter) {
        DefaultCategoryDataset result = new DefaultCategoryDataset();
        Comparable c = new Comparable() {
            @Override
            public int compareTo(Object o) {
                return 1;
            }
        };
        for(int i = 0; i < resultsOfTests.size(); i++){
            if(filter.macAddress.equals(resultsOfTests.get(i).senderSessionSummary.summarySource.macAddress)) {
                result.addValue(resultsOfTests.get(i).messageMatchPecrentage,
                        resultsOfTests.get(i).senderSessionSummary.sendParameters.message,
                        ""+resultsOfTests.get(i).senderSessionSummary.sendParameters.loudnessLevel);
                Log(LOG_TAG, i + ". Match: " + resultsOfTests.get(i).messageMatchPecrentage + "; send message: "
                        + resultsOfTests.get(i).senderSessionSummary.sendParameters.message + "; loudness: "
                        + resultsOfTests.get(i).senderSessionSummary.sendParameters.loudnessLevel+ "; recived message: "
                        + resultsOfTests.get(i).receiverSessionSummary.message);
            }
        }
        return result;
    }
}
