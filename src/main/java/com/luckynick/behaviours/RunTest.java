package com.luckynick.behaviours;

import com.luckynick.behaviours.createProfile.CreateConfig;
import com.luckynick.behaviours.runTest.TestPreparationBehaviour;
import com.luckynick.models.ModelIO;
import com.luckynick.models.ModelSelector;
import com.luckynick.models.profiles.Config;
import com.luckynick.models.profiles.SequentialTestProfile;

import java.util.List;

import static com.luckynick.custom.Utils.Log;

//TODO: run aditional 70-100% loudness tests
public class RunTest extends ProgramBehaviour {

    public static final String LOG_TAG = "RunTest";

    private boolean useConfigProfile;

    public RunTest(boolean useConfig) {
        this.useConfigProfile = useConfig;
    }

    @Override
    public void performProgramTasks() {
        SequentialTestProfile testProfile;
        if(useConfigProfile) {
            ModelIO<Config> profileModelIO = new ModelIO<>(Config.class);
            List<Config> configs = profileModelIO.listObjects();
            Log(LOG_TAG, "Done. " + configs.size());
            if(configs.size() == 0) {
                new CreateConfig().performProgramTasks(); //if config doesn't exist -> create config
                configs = profileModelIO.listObjects();
            }
            Config confInstance = configs.get(0);
            testProfile = confInstance.defaultProfile;
        }
        else {
            ModelIO<SequentialTestProfile> profileModelIO = new ModelIO<>(SequentialTestProfile.class);
            List<SequentialTestProfile> profile = ModelSelector.requireSelection(profileModelIO, false);
            testProfile = profile.get(0);
        }

        int repeat = 1;
        System.out.println("Running tests: repeat " + repeat + " times.");
        new TestPreparationBehaviour(testProfile, repeat).performProgramTasks();

        //runTimes(testProfile, 4);
    }

    /*private void runTimes(SequentialTestProfile testProfile, final int n) {

        final PureFunctionalInterfaceWithReturn<Integer> counter = new PureFunctionalInterfaceWithReturn<Integer>() {
            int c = 0;
            @Override
            public Integer performProgramTasks() {
                return c++;
            }
        };
        System.out.println("Running test");
        new TestPreparationBehaviour(testProfile).performProgramTasks();
        PerformTests.testEndSubs.add(() -> {
            if(counter.performProgramTasks() < n - 1) {
                System.out.println("Running test");
                new TestPreparationBehaviour(testProfile, ).performProgramTasks();
            }
            else {
                exit();
            }
        });
    }*/
}
