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
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.luckynick.custom.Utils.Log;

@IOClassHandling(dataStorage = SharedUtils.DataStorage.CUMULATED_REPORT)
public class CumulatedReport extends TestResult {

    /*
    Assumption: test results could be worse because of battery level.
     */

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
                JPanel chartsPanelUp = new JPanel();
                chartsPanelUp.setLayout(new BoxLayout(chartsPanelUp, BoxLayout.X_AXIS));
                chartsPanelUp.add(createPlot(testsReports.get(0).seqTestProfile.peer1, false));
                chartsPanelUp.add(createPlot(testsReports.get(0).seqTestProfile.peer2, false));
                getContentPane().add(chartsPanelUp);
                if(!CumulatedReport.this.testsReports.get(0).seqTestProfile.spectralAnalysis) {
                    JPanel chartsPanelDown = new JPanel();
                    chartsPanelDown.setLayout(new BoxLayout(chartsPanelDown, BoxLayout.X_AXIS));
                    chartsPanelDown.add(createPlot(testsReports.get(0).seqTestProfile.peer1, true));
                    chartsPanelDown.add(createPlot(testsReports.get(0).seqTestProfile.peer2, true));
                    getContentPane().add(chartsPanelDown);
                }
                JPanel infoPanel = new JPanel();
                infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
                infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
                String descriptionTest = "Description: " + description + "\n"
                        + "File name: " + CumulatedReport.super.filename;
                JTextArea descriptionArea = new JTextArea(descriptionTest, 2, 30);
                descriptionArea.setLineWrap(true);
                descriptionArea.setWrapStyleWord(true);
                descriptionArea.setOpaque(false);
                descriptionArea.setEditable(false);
                infoPanel.add(descriptionArea);
                getContentPane().add(infoPanel);
            }
        };
        cFrame.addElements();
        cFrame.displayWindow();
        //cFrame.setSize(new Dimension());
    }

    private ChartPanel createPlot(Device forDevice, boolean oneLinePlot) {

        DefaultCategoryDataset resultDataset = null;
        //scatter values to dataset for future averaging
        if(!oneLinePlot) {
            for(TestsReport one : testsReports) {
                DefaultCategoryDataset tempDataset = one.getChartDataSet(forDevice);
                if(resultDataset == null) {
                    resultDataset = tempDataset;
                }
                else { // make an increment
                    for(Object rowK : tempDataset.getRowKeys()) {
                        for(Object colK : tempDataset.getColumnKeys()) {
                            Number incValue = tempDataset.getValue((Comparable) rowK, (Comparable) colK);
                            resultDataset.incrementValue(incValue.doubleValue(), (Comparable) rowK, (Comparable) colK);
                        }
                    }
                }
            }
            for(Object rowK : resultDataset.getRowKeys()) {
                for(Object colK : resultDataset.getColumnKeys()) {
                    Number value = resultDataset.getValue((Comparable) rowK, (Comparable) colK);
                    resultDataset.setValue(value.doubleValue() / testsReports.size(), (Comparable) rowK, (Comparable) colK);
                }
            }
        }
        else {
            final String oneLineName = "Σ (success * string weight)";
            int lengthsOfDictionaryStrings = 0;
            for(TestsReport one : testsReports) {
                DefaultCategoryDataset tempDataset = one.getChartDataSet(forDevice);
                if(resultDataset == null) {
                    resultDataset = new DefaultCategoryDataset();
                    for(Object rowKey: tempDataset.getRowKeys()){
                        lengthsOfDictionaryStrings += rowKey.toString().length();
                    }
                    for(Object rowK : tempDataset.getRowKeys()) {
                        for(Object colK : tempDataset.getColumnKeys()) {
                            resultDataset.setValue(0,
                                    (Comparable) oneLineName, (Comparable) colK);
                        }
                    }
                }
                for(Object rowK : tempDataset.getRowKeys()) {
                    for(Object colK : tempDataset.getColumnKeys()) {
                        Number incValue = tempDataset.getValue((Comparable) rowK, (Comparable) colK);
                        resultDataset.incrementValue(incValue.doubleValue() * rowK.toString().length(),
                                (Comparable) oneLineName, (Comparable) colK);
                    }
                }
            }
            for(Object rowK : resultDataset.getRowKeys()) {
                for(Object colK : resultDataset.getColumnKeys()) {
                    Number value = resultDataset.getValue((Comparable) rowK, (Comparable) colK);
                    resultDataset.setValue(value.doubleValue() / testsReports.size() / lengthsOfDictionaryStrings, (Comparable) oneLineName, (Comparable) colK);
                }
            }
            Log(LOG_TAG, "lengthsOfDictionaryStrings: " + lengthsOfDictionaryStrings);
        }


        // Create chart
        JFreeChart chart = ChartFactory.createLineChart(
                "Device " + forDevice.vendor + " (sender)", // Chart title
                "Loudness", // X-Axis Label
                "Success", // Y-Axis Label
                resultDataset,
                PlotOrientation.VERTICAL, true, false, false
        );
        chart.getCategoryPlot().getRangeAxis().setRange(0, 100);

        return new ChartPanel(chart);
    }
}
