package com.luckynick.behaviours.addDevice;

import com.luckynick.behaviours.ProgramBehaviour;
import com.luckynick.models.Device;
import com.luckynick.models.ModelIO;
import com.luckynick.net.NetworkService;
import com.luckynick.net.OSExecutables;

import java.io.IOException;
import java.util.Scanner;

public class DeviceAdditionBehaviour extends ProgramBehaviour {

    Device deviceObjectToManipulate;
    ModelIO<Device> modelIO;

    public DeviceAdditionBehaviour() {
        deviceObjectToManipulate = new Device();
        modelIO = new ModelIO<>(Device.class);
    }

    @Override
    public void performProgramTasks() {
        NetworkService serviceOut = new NetworkService();
        try (NetworkService service = serviceOut) {
            waitConsoleInput("Confirm that hotspot was started [Enter]");
            service.connectWifi();
            if(!service.isWifiConnected()) {
                System.out.println("Failed to establish WIFI connection. Abort.");
                return;
            }
            System.out.println("WIFI connected: " + service.isWifiConnected());

            for(String ip : service.requireParticipantsIPs()){
                System.out.println("Local IP: " + ip);
            }

            System.out.println("WIFI connected: " + serviceOut.isWifiConnected());

            waitConsoleInput("Smash dat button to proceed");
            System.out.println("Oke alright");

            service.connect("192.168.43.91");  //try to connect
        }

        /*try {
            //TODO: Scatter devices from network
            ArrayList<Device> devices = new ArrayList<>();


            modelIO.serialize(deviceObjectToManipulate);
        }
        catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    void waitConsoleInput(String info) {
        System.out.println("[INFO] " + info);
        waitConsoleInput();
    }

    void waitConsoleInput() {
        //new Scanner(System.in).next();
        System.out.println("Press \"ENTER\" to continue...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
}
