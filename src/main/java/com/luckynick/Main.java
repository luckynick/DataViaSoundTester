package com.luckynick;

import com.luckynick.behaviours.*;
import com.luckynick.shared.PureFunctionalInterfaceWithReturn;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class Main {

    private static HashMap<String, PureFunctionalInterfaceWithReturn<MenuSelectable>> programParamsActions
            = new HashMap<>();

    static {
        programParamsActions.put(null, IncorrectStartParameter::new);
        programParamsActions.put("", DefaultStartBehaviour::new);
        programParamsActions.put("--createProfile", CreateNewProfile::new);
        programParamsActions.put("--runTest", RunTest::new);
        programParamsActions.put("--addDevice", AddDevice::new);
        programParamsActions.put("--overviewTest", () -> {return new IncorrectStartParameter("OverviewTest");});
    }

    public static void main(String[] args) {
        resolveProgramArgs(args).performProgramTasks();
    }

    public static MenuSelectable resolveProgramArgs(String[] args) {
        System.out.print("Arguments list: ");
        Arrays.stream(args).forEach(x -> {System.out.print(x);});
        System.out.println();

        MenuSelectable incorrectSelection = programParamsActions.get(null).performProgramTasks();
        MenuSelectable defaultSelection = programParamsActions.get("").performProgramTasks();
        MenuSelectable selected = args.length == 0 ? defaultSelection
                : programParamsActions.get(args[0]).performProgramTasks();
        return selected == null ? incorrectSelection : selected;
    }

    private static class IncorrectStartParameter extends ProgramBehaviour {

        public IncorrectStartParameter() {
            super();
        }

        public IncorrectStartParameter(String s) {
            super(s);
        }

        @Override
        public void performProgramTasks() {
            System.out.println("Incorrect start parameter or functionality is not yet implemented. " +
                    "If you want to use default mode - don't pass any parameter.");
        }
    }

    private static class DefaultStartBehaviour extends ProgramBehaviour {
        @Override
        public void performProgramTasks() {
            Set<String> keysSet = programParamsActions.<String>keySet();
            keysSet.remove("");
            keysSet.remove(null);
            int actionsNum = keysSet.size();
            MenuSelectable[] menuActions = new MenuSelectable[actionsNum];
            String[] keys = keysSet.toArray(new String[0]);
            for(int i = 0; i < actionsNum; i++) {
                menuActions[i] = programParamsActions.get(keys[i]).performProgramTasks();
            }

            doMenuSelection(menuActions).performProgramTasks();
        }
    }
}
