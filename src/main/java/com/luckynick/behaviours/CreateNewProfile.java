package com.luckynick.behaviours;

import com.luckynick.behaviours.createProfile.CreateSequentialProfile;
import com.luckynick.behaviours.createProfile.CreateSingularProfile;
import com.luckynick.behaviours.createProfile.CreateTestScenario;
import com.luckynick.behaviours.createProfile.ProfileCreationBehaviour;

public class CreateNewProfile extends ProgramBehaviour {
    @Override
    public void performProgramTasks() {
        ProfileCreationBehaviour[] menuActions = new ProfileCreationBehaviour[]{
                new CreateSingularProfile(),
                new CreateSequentialProfile(),
                new CreateTestScenario()
        };

        doMenuSelection(menuActions).performProgramTasks();
    }
}
