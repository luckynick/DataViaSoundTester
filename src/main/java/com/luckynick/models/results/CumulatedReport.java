package com.luckynick.models.results;

import com.luckynick.CustomJFrame;
import com.luckynick.custom.Device;
import com.luckynick.models.ManageableField;
import com.luckynick.models.profiles.SequentialTestProfile;
import com.luckynick.shared.IOClassHandling;
import com.luckynick.shared.SharedUtils;
import com.luckynick.shared.ValidationException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicBorders;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.luckynick.custom.Utils.Log;

@IOClassHandling(dataStorage = SharedUtils.DataStorage.CUMULATED_REPORT)
public class CumulatedReport extends TestResult {

    public static final String LOG_TAG = "CumulatedReport";

    @ManageableField(required = true)
    public List<TestsReport> testsReports = new ArrayList<>();
    @ManageableField()
    public transient String filename = "cumulated_" + SharedUtils.getDateStringForFileName();
    @ManageableField(required = true)
    public String description;

    @Override
    public void setFilename() {
        setFilename(filename + SharedUtils.JSON_EXTENSION);
    }

    private boolean validate() {
        TestsReport comparison = null;
        for(TestsReport rep : testsReports) {
            if(comparison == null) {
                comparison = rep;
            }
            else {
                if(!Objects.equals(comparison.seqTestProfile.peer1.macAddress, rep.seqTestProfile.peer1.macAddress) &&
                        !Objects.equals(comparison.seqTestProfile.peer1.macAddress, rep.seqTestProfile.peer2.macAddress))
                    throw new ValidationException("Peer1 ("+comparison.seqTestProfile.peer1.macAddress+") doesn't match neither "
                            + rep.seqTestProfile.peer1.macAddress + " nor " + rep.seqTestProfile.peer2.macAddress);
                if(!Objects.equals(comparison.seqTestProfile.peer2.macAddress, rep.seqTestProfile.peer1.macAddress) &&
                        !Objects.equals(comparison.seqTestProfile.peer2.macAddress, rep.seqTestProfile.peer2.macAddress))
                    throw new ValidationException("Peer2 ("+comparison.seqTestProfile.peer1.macAddress+") doesn't match neither "
                            + rep.seqTestProfile.peer1.macAddress + " nor " + rep.seqTestProfile.peer2.macAddress);
                if(comparison.resultsOfTests.size() != rep.resultsOfTests.size())
                    throw new ValidationException("Size of test results don't match.");
            }
        }
        return true;
    }

    public void showSummary() {
        validate();
        CustomJFrame cFrame = new CustomJFrame("Cumulated report") {
            @Override
            public void addElements() {
                JPanel chartsPanel = new JPanel();
                chartsPanel.setLayout(new BoxLayout(chartsPanel, BoxLayout.X_AXIS));
                chartsPanel.add(createPlot(testsReports.get(0).seqTestProfile.peer1));
                chartsPanel.add(createPlot(testsReports.get(0).seqTestProfile.peer2));
                getContentPane().add(chartsPanel);
                JPanel infoPanel = new JPanel();
                infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
                infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
                infoPanel.add(new JLabel("Description: " + description));
                infoPanel.add(new JLabel("File name: " + CumulatedReport.super.filename));
                getContentPane().add(infoPanel);
            }
        };
        cFrame.addElements();
        cFrame.displayWindow();
    }

    private ChartPanel createPlot(Device forDevice) {

        DefaultCategoryDataset datasetPeer = null;
        for(TestsReport one : testsReports) {
            DefaultCategoryDataset tempDataset = one.getChartDataSet(forDevice);
            if(datasetPeer == null) {
                datasetPeer = tempDataset;
            }
            else { // make an increment
                for(Object rowK : tempDataset.getRowKeys()) {
                    for(Object colK : tempDataset.getColumnKeys()) {
                        Number incValue = tempDataset.getValue((Comparable) rowK, (Comparable) colK);
                        datasetPeer.incrementValue(incValue.doubleValue(), (Comparable) rowK, (Comparable) colK);
                    }
                }
            }
        }
        for(Object rowK : datasetPeer.getRowKeys()) {
            for(Object colK : datasetPeer.getColumnKeys()) {
                Number value = datasetPeer.getValue((Comparable) rowK, (Comparable) colK);
                datasetPeer.setValue(value.doubleValue() / testsReports.size(), (Comparable) rowK, (Comparable) colK);
            }
        }
        // Create chart
        JFreeChart chart = ChartFactory.createLineChart(
                "Device " + forDevice.vendor + " (sender)", // Chart title
                "Loudness", // X-Axis Label
                "Success", // Y-Axis Label
                datasetPeer,
                PlotOrientation.VERTICAL, true, false, false
        );

        return new ChartPanel(chart);
    }
}
