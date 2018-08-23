package com.luckynick.behaviours;

import com.luckynick.behaviours.createProfiles.CreateSequentialProfile;
import com.luckynick.behaviours.createProfiles.CreateSingularProfile;

public class CreateNewProfile extends ProgramBehaviour {
    @Override
    public void performProgramTasks() {
        doMenuSelection(new CreateSingularProfile(), new CreateSequentialProfile()).performProgramTasks();
    }
}
