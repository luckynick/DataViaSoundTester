package com.luckynick.behaviours.addDevice;

import static com.luckynick.custom.Utils.*;

import com.luckynick.behaviours.ProgramBehaviour;
import com.luckynick.custom.Device;
import com.luckynick.models.ModelIO;
import com.luckynick.net.ConnectionHandler;
import com.luckynick.net.MultiThreadServer;
import com.luckynick.net.WindowsNetworkService;
import com.luckynick.shared.enums.PacketID;
import com.luckynick.shared.enums.TestRole;
import com.luckynick.shared.net.*;
import com.luckynick.shared.SharedUtils;
import nl.pvdberg.pnet.client.Client;
import nl.pvdberg.pnet.packet.Packet;
import nl.pvdberg.pnet.packet.PacketBuilder;
import nl.pvdberg.pnet.packet.PacketReader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class DeviceAdditionBehaviour extends ProgramBehaviour implements PacketListener, ConnectionListener {

    public static final String LOG_TAG = "TestPreparationBehaviour";

    ModelIO<Device> modelIO;
    Thread udpBroadcastThread;
    int tcpPort = SharedUtils.TCP_COMMUNICATION_PORT;

    public DeviceAdditionBehaviour() {
        modelIO = new ModelIO<>(Device.class);
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

            MultiThreadServer server = new MultiThreadServer(SharedUtils.TCP_COMMUNICATION_PORT);
            ConnectionHandler.subscribePacketEvents(this);
            server.subscribeConnectionEvents(this);
            udpBroadcastThread = UDPServer.trapConnection();
        }
    }

    private void addDevice(String json) {
        Device obj = modelIO.deserialize(json);
        obj.setFilename();
        Log(LOG_TAG, "Device to add:");
        Log(LOG_TAG, json);
        addDevice(obj);
    }

    private void addDevice(Device obj) {
        try {
            List<File> fileList = modelIO.listFiles();
            for(File f : fileList) {
                Device toRemove = modelIO.deserialize(f);
                if(obj.macAddress.equals(toRemove.macAddress)) f.delete();
            }
            Log(LOG_TAG, "Serializing " + obj.vendor + " on path " + obj.wholePath);
            modelIO.serialize(obj);
        }
        catch (IOException e) {
            e.printStackTrace();
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
                addDevice(json);
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
