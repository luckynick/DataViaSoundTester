package com.luckynick;

import com.luckynick.behaviours.CreateNewProfile;
import com.luckynick.behaviours.PickDefaultProfile;
import com.luckynick.behaviours.ProgramBehaviour;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        resolveProgramArgs(args).performProgramTasks();
    }

    public static ProgramBehaviour resolveProgramArgs(String[] args) {
        //Arrays.stream(args).forEach((x) -> {System.out.println("arg: "+x);});
        return new CreateNewProfile();
    }
}
