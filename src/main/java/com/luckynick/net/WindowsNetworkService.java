package com.luckynick.net;

import com.luckynick.shared.net.NetworkService;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import static com.luckynick.custom.Utils.*;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 192.168.***.***
 * Strategy:
 * 1. Find hotspot host
 * 2. Ask for IPs which are connected to hotspot
 * 3. Establish separate connections with each IP
 */
public class WindowsNetworkService extends NetworkService implements Closeable {

    public WindowsNetworkService() {
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
            OSExecutables.executeCommandsPersistEach(commands);
        }
    }

    public void connectWifi() {
        System.out.println("Connecting to SSID " + SSID);
        String[] commands = new String[] { //TODO: refresh list of WIFIs
                "netsh interface ip delete arpcache",
                "netsh wlan connect name=" + SSID,
                //"cmd /c start \"\" bat\\connect_wifi.bat " + SSID
        };
        if(OSExecutables.persistCommand(commands)) {
            while(!isWifiConnected()) {
                Log(LOG_TAG, "Still waiting for WIFI to connect.");
                try {
                    Thread.sleep(WAIT_TIME_AFTER_FAIL);
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
        if(OSExecutables.executeCommandsPersistEach(commands)) {
            while(isWifiConnected()) {
                Log(LOG_TAG, "Still waiting for WIFI to disconnect.");
                try {
                    Thread.sleep(WAIT_TIME_AFTER_FAIL);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void close() { //TODO: uncomment
        //disconnectWifi();
    }


    public boolean isWifiConnected() {
        boolean connected = false;
        String[] commands = new String[] {
                "cmd /C netsh wlan show interfaces | Findstr /c:\"Signal\" && Echo Online || Echo Offline",
        };
        String result = OSExecutables.executeCommandReturnString(commands[0]);
        if(result.contains("Online")) connected = true;
        Log(LOG_TAG, "Wifi connected: "+connected);
        return connected;
    }

    /**
     * Eventually first element is IP address of this device.
     * @return
     */
    private List<String> arpScan() {
        List<String> result = new ArrayList<>();

        // Create operating system process from arpe.bat file command
        String out = OSExecutables.executeCommandReturnString("arp -a");
        // A compiled representation of a regular expression
        Pattern pattern =
                Pattern.compile(".*\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b");
		/* An engine that performs match operations on a character sequence by interpreting a Pattern */
        Matcher match = pattern.matcher(out);
        String prev="",pLoc;
        if(!(match.find()))        // In case no IP address Found in out
            return result;
        else
        {
            while(match.find())
            {
                pLoc = match.group();	// Returns the IP of other hosts
                result.add(pLoc);
            }
        }
        return result;
    }
}
