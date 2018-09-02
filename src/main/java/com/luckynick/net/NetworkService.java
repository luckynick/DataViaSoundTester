package com.luckynick.net;

import com.luckynick.Utils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class NetworkService {

    public static final int COMMUNICATION_PORT = 8080;
    public static final int WAIT_AFTER_LAST_COMMAND = 500;
    public static final int COMMAND_PERSISTANCE_ATTEMPTS = 20;
    public static final boolean THIS_IS_WIFI_HOTSPOT = false;
    public static final String SSID = "Heh_mobile", PASSWORD = "123456798";
    public static final String configFolder = Utils.DataStorage.CONFIG.toString();
    public static final String wifiProfilePath = Utils.formPathString(configFolder, SSID + ".xml");

    private List<Socket> connectionPool = new ArrayList<>();

    private boolean wifiConnected = false;

    public NetworkService() {
        prepWifi();
    }

    private void prepWifi() {
        System.out.println("Preparing WIFI.");
        if(THIS_IS_WIFI_HOTSPOT) throw new NotImplementedException();
        else {
            File wifiProfile = new File(wifiProfilePath);
            String[] commands = new String[] {
                    "netsh wlan disconnect",
                    "Netsh WLAN delete profile name=\""+SSID+"\"",
                    "Netsh WLAN add profile filename=\""+wifiProfile.getAbsolutePath()+"\"",
            };
            executeCommandsPersistEach(commands);
        }
    }

    public void connectWifi() {
        System.out.println("Connecting to SSID " + SSID);
        String[] commands = new String[] { //TODO: refresh list of WIFIs
                "netsh wlan connect name=" + SSID,
        };
        if(executeCommandsPersistEach(commands)) {
            while(!isWifiConnected()) {
                Utils.Log("Still waiting for WIFI to connect.");
                try {
                    Thread.sleep(WAIT_AFTER_LAST_COMMAND);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void disconnectWifi() {
        System.out.println("Disconnecting");
        String[] commands = new String[] { //TODO: refresh list of WIFIs
                "netsh wlan disconnect",
        };
        if(executeCommandsPersistEach(commands)) {
            while(isWifiConnected()) {
                Utils.Log("Still waiting for WIFI to disconnect.");
                try {
                    Thread.sleep(WAIT_AFTER_LAST_COMMAND);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Only last command is persisted.
     * @param commands
     */
    private boolean executeCommandsPersistLast(String[] commands) {
        if(commands.length == 0) throw new IllegalArgumentException("No commands provided for execution.");
        for(int i = 0; i < commands.length - 1; i++) {
            executeCommand(commands[i]);
        }
        return persistCommand(commands[commands.length - 1], COMMAND_PERSISTANCE_ATTEMPTS);
    }

    /**
     * Only last command is persisted.
     * @param commands
     */
    private boolean executeCommandsPersistEach(String[] commands) {
        if(commands.length == 0) throw new IllegalArgumentException("No commands provided for execution.");
        boolean success;
        for(int i = 0; i < commands.length; i++) {
            success = persistCommand(commands[i], COMMAND_PERSISTANCE_ATTEMPTS);
            if(!success) return false;
        }
        return true;
    }

    private int executeCommand(String command) {
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

    private String executeCommandReturnString(String command) {
        String result = null;
        try {
            Process process = Runtime.getRuntime().exec(command);
            if(process.waitFor() != 0) return result;
            Scanner scn = new Scanner(new InputStreamReader(process.getInputStream()));
            result = "";
            while(scn.hasNext()) {
                result += scn.next();
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Utils.Log("[command with string] " + command);
        return result;
    }

    private boolean persistCommand(String command, int attempts) {
        Utils.Log("Persisting command ("+attempts+" attempts):");
        int exitCode;
        do {
            exitCode = executeCommand(command);
            attempts--;
            if(exitCode != 0) {
                if(attempts < 1) return false;
                try {
                    Thread.sleep(WAIT_AFTER_LAST_COMMAND);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        while (exitCode != 0);
        return true;
    }

    public List<String> requireParticipantsIPs(int numOfParticipantsToWait) {
        //TODO
        return new ArrayList<>();
    }

    private Socket connect(String ip) {

        //connectionPool.add();
        return null;
    }

    public void finalize() {
        disconnectWifi();
    }


    public boolean isWifiConnected() {
        boolean connected = false;
        String[] commands = new String[] {
                "cmd /C netsh wlan show interfaces | Findstr /c:\"Signal\" && Echo Online || Echo Offline",
        };
        String result = executeCommandReturnString(commands[0]);
        if(result.contains("Online")) connected = true;
        return connected;
    }
}
