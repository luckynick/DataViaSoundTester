package com.luckynick.behaviours;

import com.luckynick.behaviours.addDevice.DeviceAdditionBehaviour;
import com.luckynick.behaviours.createProfile.ModelCreationBehaviour;
import com.luckynick.behaviours.showReport.ShowCumulatedReportBehaviour;
import com.luckynick.behaviours.showReport.ShowReportBehaviour;

public class ShowReport extends ProgramBehaviour {
    @Override
    public void performProgramTasks() {
        System.out.println("Showing report");
        //new ShowReportBehaviour().performProgramTasks();

        ProgramBehaviour[] menuActions = new ProgramBehaviour[]{
                new ShowReportBehaviour(),
                new ShowCumulatedReportBehaviour(),
        };

        doMenuSelection(menuActions).performProgramTasks();
    }
}
