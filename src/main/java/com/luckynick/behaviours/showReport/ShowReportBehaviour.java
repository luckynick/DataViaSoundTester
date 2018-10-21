package com.luckynick.behaviours.showReport;

import com.luckynick.behaviours.ProgramBehaviour;
import com.luckynick.models.ModelIO;
import com.luckynick.models.ModelSelector;
import com.luckynick.models.results.TestsReport;

import java.util.List;

import static com.luckynick.custom.Utils.Log;

public class ShowReportBehaviour extends ProgramBehaviour {

    public static final String LOG_TAG = "ShowReportBehaviour";

    ModelIO<TestsReport> reportModelIO = new ModelIO<>(TestsReport.class);

    @Override
    public void performProgramTasks() {
        List<TestsReport> profile = ModelSelector.requireSelection(reportModelIO, false);
        TestsReport report = profile.get(0);
        Log(LOG_TAG, "Size of test results: " + report.resultsOfTests.size());
        report.showPlot(report.seqTestProfile.peer1);
        report.showPlot(report.seqTestProfile.peer2);
    }
}
