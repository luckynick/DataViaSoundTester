package com.luckynick.behaviours;

import com.luckynick.behaviours.addDevice.DeviceAdditionBehaviour;

public class AddDevice extends ProgramBehaviour {
    @Override
    public void performProgramTasks() {
        System.out.println("Adding device");
        new DeviceAdditionBehaviour().performProgramTasks();
    }
}
