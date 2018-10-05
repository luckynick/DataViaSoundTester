package com.luckynick.behaviours;

import java.util.Scanner;

public abstract class ProgramBehaviour extends MenuSelectable {

    private String overrideMenuName;

    public ProgramBehaviour() {
    }

    public ProgramBehaviour(String overrideMenuName) {
        this.overrideMenuName = overrideMenuName;
    }

    @Override
    public String toString(){
        return overrideMenuName == null ? this.getClass().getSimpleName() : overrideMenuName;
    }

    public void waitConsoleInput(String info) {
        System.out.println("[INFO] " + info);
        waitConsoleInput();
    }

    public void waitConsoleInput() {
        System.out.println("Press \"ENTER\" to continue...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
}
