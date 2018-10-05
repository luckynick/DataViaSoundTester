package com.luckynick.behaviours.runTest;

import com.luckynick.behaviours.ProgramBehaviour;
import com.luckynick.custom.Device;
import com.luckynick.models.ModelIO;
import com.luckynick.models.ModelSelector;
import com.luckynick.models.profiles.SequentialTestProfile;
import com.luckynick.net.ConnectionHandler;
import com.luckynick.net.MultiThreadServer;
import com.luckynick.net.WindowsNetworkService;
import com.luckynick.shared.SharedUtils;
import com.luckynick.shared.enums.PacketID;
import com.luckynick.shared.enums.TestRole;
import com.luckynick.shared.net.ConnectionListener;
import com.luckynick.shared.net.PacketListener;
import com.luckynick.shared.net.UDPServer;
import nl.pvdberg.pnet.client.Client;
import nl.pvdberg.pnet.packet.Packet;
import nl.pvdberg.pnet.packet.PacketBuilder;
import nl.pvdberg.pnet.packet.PacketReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.luckynick.custom.Utils.Log;

public class TestPreparationBehaviour extends ProgramBehaviour implements PacketListener, ConnectionListener {

    public static final String LOG_TAG = "TestPreparationBehaviour";

    Thread udpBroadcastThread;
    int tcpPort = SharedUtils.TCP_COMMUNICATION_PORT;

    ModelIO<Device> deviceModelIO = new ModelIO<>(Device.class);
    ModelIO<SequentialTestProfile> profileModelIO = new ModelIO<>(SequentialTestProfile.class);

    List<Device> connectedDevices = new ArrayList<>();
    List<Device> allowedDevices = ModelSelector.requireSelection(deviceModelIO, true);
    private final int REQUIRED_DEVICES_AMT = 2;

    @Override
    public void performProgramTasks() {
        if(allowedDevices == null) {
            System.out.println("Tests were terminated.");
            return;
        }



        WindowsNetworkService serviceOut = new WindowsNetworkService();
        try (WindowsNetworkService service = serviceOut) {
            //waitConsoleInput("Confirm that hotspot was started [Enter]");
            service.connectWifi();
            if(!service.isWifiConnected()) {
                System.out.println("Failed to establish WIFI connection. Abort.");
                return;
            }
            System.out.println("WIFI connected: " + service.isWifiConnected());

            MultiThreadServer server = new MultiThreadServer(SharedUtils.TCP_COMMUNICATION_PORT);
            ConnectionHandler.subscribePacketEvents(this);
            server.subscribeConnectionEvents(this);
            trapConnection();
        }
    }

    private void trapConnection() {
        udpBroadcastThread = UDPServer.broadcastThread(
                TestRole.CONTROLLER + " " + tcpPort);
        udpBroadcastThread.start();
    }

    private void addDevice(String json) {
        Device obj = deviceModelIO.deserialize(json);
        addDevice(obj);
    }

    private void addDevice(Device obj) {
        for(Device stored : allowedDevices) {
            if(obj.macAddress.equals(stored.macAddress)) {
                connectedDevices.add(obj);
                if(connectedDevices.size() == REQUIRED_DEVICES_AMT) {
                    //SequentialTestProfile
                    new PerformTests(null).performProgramTasks();
                }
                return;
            }
        }
    }


    @Override
    public void onRequestPacket(Client c, Packet p) {
        Log(LOG_TAG, "onRequestPacket: " + p);
    }

    @Override
    public void onResponsePacket(Client c, Packet p) {
        PacketReader packetReader = new PacketReader(p);
        try {
            int responseType = packetReader.readInt();
            if(responseType == PacketID.DEVICE.ordinal()) {
                String json = packetReader.readString();
                //TODO: handle response
            }
            else {
                Log(LOG_TAG, "Response doesn't contain device data.");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onUnexpectedPacket(Client c, Packet p) {
        Log(LOG_TAG, "onUnexpectedPacket: " + p);
    }

    @Override
    public void onConnect(Client c) {
        Log(LOG_TAG, "onConnect: " + c.getInetAddress().getHostAddress());

        sendPullRequest(c, PacketID.DEVICE); //require device info from remote phone
    }

    @Override
    public void onDisconnect(Client c) {
        Log(LOG_TAG, "onDisconnect: " + c.getInetAddress().getHostAddress());
        throw new IllegalStateException("Device was disconnected.");
    }

    public static void sendPullRequest(Client c, PacketID requiredResponse) {
        c.send(createRequestPacket().withInt(requiredResponse.ordinal()).build());
    }

    public static PacketBuilder createRequestPacket() {
        PacketBuilder pb = new PacketBuilder(Packet.PacketType.Request);
        pb.withID((short)PacketID.REQUEST.ordinal());
        return pb;
    }
}
