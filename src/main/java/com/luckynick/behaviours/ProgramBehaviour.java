package com.luckynick.behaviours;

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
}
