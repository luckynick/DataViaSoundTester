package com.luckynick.behaviours;

import com.luckynick.behaviours.addDevice.DeviceAdditionBehaviour;
import com.luckynick.behaviours.showReport.ShowReportBehaviour;

public class ShowReport extends ProgramBehaviour {
    @Override
    public void performProgramTasks() {
        System.out.println("Showing report");
        new ShowReportBehaviour().performProgramTasks();
    }
}
