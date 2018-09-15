package com.luckynick.net;

import static com.luckynick.custom.Utils.*;
import com.luckynick.shared.SharedUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class OSExecutables {

    public static final String LOG_TAG = "OSExecutables";

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

    private static Process startCmdProcess(String command) {
        try {
            return Runtime.getRuntime().exec(command);
            /*ProcessBuilder builder = new ProcessBuilder();
            String[] args = command.split(" ");
            String[] toPass = new String[args.length + 1];
            toPass[0] = "cmd.exe";
            for(int i = 1; i < toPass.length; i++) {
                toPass[i] = args[i-1];
            }
            builder.command(toPass);
            return builder.start();*/
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String executeCommandReturnString(String command) {
        String result = null;
        try {
            Process process = startCmdProcess(command);
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
        Log("command with string return", command);
        return result;
    }

    /**
     * Persist more than one command together.
     * @param command
     * @return
     */
    public static boolean persistCommand(String[] command) {
        int attempts = COMMAND_PERSISTENCE_ATTEMPTS;
        Log(LOG_TAG, "Persisting complex command ("+attempts+" attempts)");
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
        Log(LOG_TAG, "Persisting command ("+attempts+" attempts):" + command);
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

    public static int executeCommand(String command) {
        int exitCode = -1;
        try {
            exitCode = startCmdProcess(command).waitFor();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log("code " + exitCode, command);
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
