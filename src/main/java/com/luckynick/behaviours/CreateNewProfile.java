package com.luckynick.behaviours;

import com.luckynick.behaviours.createProfile.*;

public class CreateNewProfile extends ProgramBehaviour {
    @Override
    public void performProgramTasks() {
        ModelCreationBehaviour[] menuActions = new ModelCreationBehaviour[]{
                new CreateSingularProfile(),
                new CreateSequentialProfile(),
                new CreateTestScenario(),
                new CreateDictionary(),
                new CreateConfig(),
        };

        doMenuSelection(menuActions).performProgramTasks();
    }
}
