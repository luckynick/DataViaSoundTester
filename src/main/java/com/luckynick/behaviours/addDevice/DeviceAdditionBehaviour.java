package com.luckynick.behaviours.addDevice;

import static com.luckynick.custom.Utils.*;

import com.luckynick.behaviours.ProgramBehaviour;
import com.luckynick.custom.Device;
import com.luckynick.models.ModelIO;
import com.luckynick.net.WindowsNetworkService;
import com.luckynick.shared.enums.PacketID;
import com.luckynick.shared.enums.TestRole;
import com.luckynick.shared.net.*;
import com.luckynick.shared.SharedUtils;
import nl.pvdberg.pnet.client.Client;
import nl.pvdberg.pnet.event.PacketHandler;
import nl.pvdberg.pnet.packet.Packet;
import nl.pvdberg.pnet.packet.PacketReader;

import java.io.IOException;
import java.util.Scanner;

public class DeviceAdditionBehaviour extends ProgramBehaviour implements ClientManagerListener {

    public static final String LOG_TAG = "DeviceAdditionBehaviour";

    ModelIO<Device> modelIO;
    Thread udpBroadcastThread;

    ClientManager connectionManager = new ClientManager();

    public DeviceAdditionBehaviour() {
        modelIO = new ModelIO<>(Device.class);
        /*connectionManager.subscribePacketListener(PacketID.DEVICE, (Packet p, Client c) -> {
            Log(LOG_TAG, "Received Device");
            PacketReader packetReader = new PacketReader(p);
            try {
                String json = packetReader.readString();
                addDevice(json);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });*/
    }

    @Override
    public void performProgramTasks() {

        WindowsNetworkService serviceOut = new WindowsNetworkService();
        try (WindowsNetworkService service = serviceOut) {
            //waitConsoleInput("Confirm that hotspot was started [Enter]");
            service.connectWifi();
            if(!service.isWifiConnected()) {
                System.out.println("Failed to establish WIFI connection. Abort.");
                return;
            }
            System.out.println("WIFI connected: " + service.isWifiConnected());

            udpBroadcastThread = UDPServer.broadcastThread(
                    TestRole.CONTROLLER + " " + SharedUtils.TCP_COMMUNICATION_PORT);
            udpBroadcastThread.start();


        }
    }

    @Override
    public void onConnect(Client c, int connectionsNum) {
        Log(LOG_TAG, "Connected. Connections number: " + connectionsNum);

        NetworkExecutionSequence seq = new NetworkExecutionSequence(new ClientWrapper(c));
        String s = "";
        seq.addCommand(PacketID.DEVICE, new PacketHandler() {
            @Override
            public void handlePacket(Packet p, Client c) throws IOException {
                Log(LOG_TAG, "Received Device");
                PacketReader packetReader = new PacketReader(p);
                try {
                    String json = packetReader.readString();
                    addDevice(json);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).build().execute();
    }

    @Override
    public void onDisconnect(Client c, int connectionsNum) {
        Log(LOG_TAG, "Disconnected. Connections number: " + connectionsNum);
    }

    private void addDevice(String json) {
        Device obj = modelIO.deserialize(json);
        Log(LOG_TAG, "Device to add:");
        Log(LOG_TAG, json);
        Device d = modelIO.deserialize(json);
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
