package com.luckynick.net;

import com.luckynick.Utils;
import com.luckynick.shared.SharedUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 192.168.***.***
 * Strategy:
 * 1. Find hotspot host
 * 2. Ask for IPs which are connected to hotspot
 * 3. Establish separate connections with each IP
 */
public class NetworkService implements Closeable {

    public static final int COMMUNICATION_PORT = SharedUtils.COMMUNICATION_PORT;
    public static final String SSID = SharedUtils.SSID, PASSWORD = SharedUtils.PASSWORD;
    public static final boolean THIS_IS_WIFI_HOTSPOT = false;
    public static final String WIFI_SUBNET = SharedUtils.WIFI_SUBNET;
    public static final String configFolder = SharedUtils.DataStorage.CONFIG.toString();
    public static final String wifiProfilePath = SharedUtils.formPathString(configFolder, SSID + ".xml");

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
            OSExecutables.executeCommandsPersistEach(commands);
        }
    }

    public void connectWifi() {
        System.out.println("Connecting to SSID " + SSID);
        String[] commands = new String[] { //TODO: refresh list of WIFIs
                "netsh wlan connect name=" + SSID,
                //"cmd /c start \"\" bat\\connect_wifi.bat " + SSID
        };
        if(OSExecutables.persistCommand(commands)) {
            while(!isWifiConnected()) {
                Utils.Log("Still waiting for WIFI to connect.");
                try {
                    Thread.sleep(OSExecutables.WAIT_TIME_AFTER_FAIL);
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
                Utils.Log("Still waiting for WIFI to disconnect.");
                try {
                    Thread.sleep(OSExecutables.WAIT_TIME_AFTER_FAIL);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<String> requireParticipantsIPs() {
        //networkInterfaceScan();
        //iterateIPs();

        List<String> subnetIPs = new ArrayList<>();
        for(String s : arpScan()) {
            if(s.startsWith(WIFI_SUBNET)) subnetIPs.add(s);
        }

        return subnetIPs;
    }

    public void networkInterfaceScan() {
        Enumeration nis = null;
        try {
            nis = NetworkInterface.getNetworkInterfaces();
            while(nis.hasMoreElements())
            {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration ias = ni.getInetAddresses();
                while (ias.hasMoreElements())
                {
                    InetAddress ia = (InetAddress) ias.nextElement();
                    System.out.println(ia.getHostAddress());
                }

            }
        }
        catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void iterateIPs() {
        int timeout=100;
        List<Integer> thirdBytes = findThirdByte();
        for (int i=0;i<thirdBytes.size();i++){
            for (int j=1;j<255;j++){
                String host = WIFI_SUBNET + '.' + thirdBytes.get(i) + '.' + j;
                try {
                    System.out.println("Check " + host);
                    if (InetAddress.getByName(host).isReachable(timeout)){
                        System.out.println(host + " is reachable");
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private List<Integer> findThirdByte() {
        List<Integer> values = new ArrayList<>();
        int timeout=100;
        for (int i=1;i<255;i++){
            String host = WIFI_SUBNET + '.' + i + '.' + 1;
            try {
                System.out.println("Check "+host);
                if (InetAddress.getByName(host).isReachable(timeout)){
                    System.out.println(host + " is reachable");
                    values.add(i);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return values;
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

    public Socket connect(String ip) {
        try {
            Socket s = new Socket(ip, COMMUNICATION_PORT);
            new DataInputStream(s.getInputStream());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        //connectionPool.add();

        return null;
    }

    public void close() {
        disconnectWifi();
    }


    public boolean isWifiConnected() {
        boolean connected = false;
        String[] commands = new String[] {
                "cmd /C netsh wlan show interfaces | Findstr /c:\"Signal\" && Echo Online || Echo Offline",
        };
        String result = OSExecutables.executeCommandReturnString(commands[0]);
        if(result.contains("Online")) connected = true;
        Utils.Log("Wifi connected: "+connected);
        return connected;
    }
}
