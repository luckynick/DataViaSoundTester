package com.luckynick.behaviours.showReport;

import com.luckynick.behaviours.ProgramBehaviour;
import com.luckynick.models.ModelIO;
import com.luckynick.models.ModelSelector;
import com.luckynick.models.results.CumulatedReport;
import com.luckynick.models.results.TestsReport;

import java.util.List;

import static com.luckynick.custom.Utils.Log;
//TODO: window is too wide if description is long
public class ShowCumulatedReportBehaviour extends ProgramBehaviour {

    public static final String LOG_TAG = "ShowCumulatedReportBehaviour";

    ModelIO<CumulatedReport> reportModelIO = new ModelIO<>(CumulatedReport.class);

    @Override
    public void performProgramTasks() {
        List<CumulatedReport> reps = ModelSelector.requireSelection(reportModelIO, false);
        Log(LOG_TAG, "Reports size: " + reps.size());
        if(reps.size() == 1) {
            CumulatedReport report = reps.get(0);
            report.showSummary();
        }
    }
}
