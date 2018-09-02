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
        NetworkService service = new NetworkService();
        service.connectWifi();
        if(!service.isWifiConnected()) {
            System.out.println("Failed to establish WIFI connection. Abort.");
            return;
        }
        System.out.println("WIFI connected: " + service.isWifiConnected());
        service.finalize();
        System.out.println("WIFI connected: " + service.isWifiConnected());

        /*try {
            //TODO: Scatter devices from network
            ArrayList<Device> devices = new ArrayList<>();


            modelIO.serialize(deviceObjectToManipulate);
        }
        catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
