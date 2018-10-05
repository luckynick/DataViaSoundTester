package com.luckynick.behaviours;

import com.luckynick.behaviours.runTest.TestPreparationBehaviour;

public class RunTest extends ProgramBehaviour {

    @Override
    public void performProgramTasks() {
        System.out.println("Running test");
        new TestPreparationBehaviour().performProgramTasks();
    }
}
