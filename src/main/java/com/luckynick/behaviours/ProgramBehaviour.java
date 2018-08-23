package com.luckynick.behaviours;

import com.luckynick.PureFunctionalInterface;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.RandomAccess;
import java.util.Scanner;

public abstract class ProgramBehaviour implements MenuSelectable {

    private Scanner consoleScanner = new Scanner(new BufferedReader(new InputStreamReader(System.in)));

    protected MenuSelectable doMenuSelection(MenuSelectable ... options) {
        if(options.length < 1) {
            throw new IllegalArgumentException("No options provided for menu.");
        }
        System.out.println("Select kind of action to perform: ");
        for(int i = 1; i <= options.length; i++) {
            System.out.println(i + ". " + options[i-1]);
        }
        return options[readIntInRange(1, options.length) - 1];
    }

    private int readIntInRange(int start, int end) {
        int selectedOption;
        do {
            System.out.print("Input int value from " + start + " to " + end +": ");
            selectedOption = consoleScanner.nextInt();
        }
        while(selectedOption < start || selectedOption > end);
        return selectedOption;
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName();
    }
}
