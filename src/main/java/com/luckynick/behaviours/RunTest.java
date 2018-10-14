package com.luckynick.behaviours;

import com.luckynick.behaviours.runTest.TestPreparationBehaviour;

public class RunTest extends ProgramBehaviour {

    private boolean useConfig;

    public RunTest(boolean useConfig) {
        this.useConfig = useConfig;
    }

    @Override
    public void performProgramTasks() {
        System.out.println("Running test");
        new TestPreparationBehaviour(useConfig).performProgramTasks();
    }
}
