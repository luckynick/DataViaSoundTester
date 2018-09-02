package com.luckynick.net;

import com.luckynick.Utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class OSExecutables {

    public static final int WAIT_TIME_AFTER_FAIL = 1000;
    public static final int COMMAND_PERSISTENCE_ATTEMPTS = 10;

    /**
     * Only last command is persisted.
     * @param commands
     */
    public static boolean executeCommandsPersistEach(String[] commands) {
        if(commands.length == 0) throw new IllegalArgumentException("No commands provided for execution.");
        boolean success;
        for(int i = 0; i < commands.length; i++) {
            success = persistCommand(commands[i]);
            if(!success) return false;
        }
        return true;
    }

    public static String executeCommandReturnString(String command) {
        String result = null;
        try {
            Process process = Runtime.getRuntime().exec(command);
            if(process.waitFor() != 0) return result;
            Scanner scn = new Scanner(new InputStreamReader(process.getInputStream()));
            result = "";
            while(scn.hasNext()) {
                result += scn.next() + "\n";
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Utils.Log("[command with string return] " + command);
        return result;
    }

    /**
     * Persist more than one command together.
     * @param command
     * @return
     */
    public static boolean persistCommand(String[] command) {
        int attempts = COMMAND_PERSISTENCE_ATTEMPTS;
        Utils.Log("Persisting complex command ("+attempts+" attempts)");
        boolean success = true;
        for(int i = 0; i < attempts; i++) {
            success = true;
            for(int j = 0; j < command.length; j++) {
                int exitCode = executeCommand(command[j]);
                if(exitCode != 0) success = false;
            }
            if(!success) {
                try {
                    Thread.sleep(WAIT_TIME_AFTER_FAIL);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else break;
        }
        return success;
    }

    private static boolean persistCommand(String command) {
        int attempts = COMMAND_PERSISTENCE_ATTEMPTS;
        Utils.Log("Persisting command ("+attempts+" attempts):" + command);
        int exitCode;
        do {
            exitCode = executeCommand(command);
            attempts--;
            if(exitCode != 0) {
                if(attempts < 1) return false;
                try {
                    Thread.sleep(WAIT_TIME_AFTER_FAIL);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        while (exitCode != 0);
        return true;
    }

    private static int executeCommand(String command) {
        int exitCode = -1;
        try {
            exitCode = Runtime.getRuntime().exec(command).waitFor();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Utils.Log("[code " + exitCode + "] " + command);
        return exitCode;
    }


    /**
     * Only last command is persisted.
     * @param commands
     */
    private static boolean executeCommandsPersistLast(String[] commands) {
        if(commands.length == 0) throw new IllegalArgumentException("No commands provided for execution.");
        for(int i = 0; i < commands.length - 1; i++) {
            executeCommand(commands[i]);
        }
        return persistCommand(commands[commands.length - 1]);
    }
}
