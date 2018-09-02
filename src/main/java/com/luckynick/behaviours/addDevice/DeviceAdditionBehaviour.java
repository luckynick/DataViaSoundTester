package com.luckynick.behaviours.addDevice;

import com.luckynick.behaviours.ProgramBehaviour;
import com.luckynick.models.Device;
import com.luckynick.models.ModelEditor;
import com.luckynick.models.ModelIO;
import com.luckynick.net.NetworkService;

import java.io.IOException;
import java.util.ArrayList;

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
            service.connectWifi();
            if(!service.isWifiConnected()) {
                System.out.println("Failed to establish WIFI connection. Abort.");
                return;
            }
            System.out.println("WIFI connected: " + service.isWifiConnected());

            for(String ip : service.requireParticipantsIPs()){
                System.out.println("Local IP: " + ip);
            }
            
            waitConsoleInput();
        }
        System.out.println("WIFI connected: " + serviceOut.isWifiConnected());

        /*try {
            //TODO: Scatter devices from network
            ArrayList<Device> devices = new ArrayList<>();


            modelIO.serialize(deviceObjectToManipulate);
        }
        catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    void waitConsoleInput() {
        try {
            System.in.read();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
