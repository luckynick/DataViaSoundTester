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
        List<TestsReport> profiles = ModelSelector.requireSelection(reportModelIO, false);
        Log(LOG_TAG, "Profiles size: " + profiles.size());
        if(profiles.size() == 1) {
            TestsReport report = profiles.get(0);
            report.showPlot(report.seqTestProfile.peer1);
            report.showPlot(report.seqTestProfile.peer2);
        }
    }
}
