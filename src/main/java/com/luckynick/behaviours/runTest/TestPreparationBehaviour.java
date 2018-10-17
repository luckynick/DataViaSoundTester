package com.luckynick.behaviours.runTest;

import com.luckynick.behaviours.ProgramBehaviour;
import com.luckynick.behaviours.createProfile.CreateConfig;
import com.luckynick.custom.Device;
import com.luckynick.models.ModelIO;
import com.luckynick.models.ModelSelector;
import com.luckynick.models.profiles.Config;
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
import nl.pvdberg.pnet.packet.PacketReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.luckynick.custom.Utils.Log;

public class TestPreparationBehaviour extends ProgramBehaviour implements PacketListener, ConnectionListener {

    public static final String LOG_TAG = "TestPreparationBehaviour";

    Thread udpBroadcastThread;
    int tcpPort = SharedUtils.TCP_COMMUNICATION_PORT;

    ModelIO<Device> deviceModelIO = new ModelIO<>(Device.class);
    ModelIO<SequentialTestProfile> profileModelIO = new ModelIO<>(SequentialTestProfile.class);

    List<Device> connectedDevices = new ArrayList<>();
    List<Device> allowedDevices = new ArrayList<>();
    private final int REQUIRED_DEVICES_AMT = 2;
    SequentialTestProfile testProfile;
    MultiThreadServer server;

    private Map<String, Client> connections = new HashMap<>();

    private boolean useConfig;

    public TestPreparationBehaviour(boolean useConfig) {
        this.useConfig = useConfig;
    }

    @Override
    public void performProgramTasks() {
        if(useConfig) {
            ModelIO<Config> profileModelIO = new ModelIO<>(Config.class);
            List<Config> configs = profileModelIO.listObjects();
            Log(LOG_TAG, "Done. " + configs.size());
            if(configs.size() == 0) {
                new CreateConfig().performProgramTasks(); //if config doesn't exist -> create config
                configs = profileModelIO.listObjects();
            }
            Config confInstance = configs.get(0);
            testProfile = confInstance.defaultProfile;
        }
        else {
            List<SequentialTestProfile> profile = ModelSelector.requireSelection(profileModelIO, false);
            testProfile = profile.get(0);
        }
        allowedDevices.add(testProfile.peer1);
        allowedDevices.add(testProfile.peer2);

        for(Device v : allowedDevices) {
            System.out.println("allowed mac " +v.macAddress);
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

            server = new MultiThreadServer(SharedUtils.TCP_COMMUNICATION_PORT);
            ConnectionHandler.subscribePacketEvents(this);
            server.subscribeConnectionEvents(this);
            udpBroadcastThread = UDPServer.trapConnection();
        }
    }

    private void addDevice(String json, Client cli) {
        Device obj = deviceModelIO.deserialize(json);
        addDevice(obj, cli);
    }

    private void addDevice(Device newDevice, Client cli) {
        for(Device stored : allowedDevices) {
            if(newDevice.macAddress.equals(stored.macAddress)) {
                connections.put(newDevice.macAddress, cli);
                connectedDevices.add(newDevice);
                replaceDeviceInProfile(newDevice);
                if(connectedDevices.size() == REQUIRED_DEVICES_AMT) {
                    udpBroadcastThread.interrupt();
                    testProfile.peer1 = connectedDevices.get(0);
                    testProfile.peer2 = connectedDevices.get(1);
                    ConnectionHandler.packetListeners.remove(this);
                    new PerformTests(testProfile, connections, server).performProgramTasks();
                }
                return;
            }
        }
    }

    private void replaceDeviceInProfile(Device replacement) {
        if(testProfile.peer1.macAddress.equals(replacement.macAddress)) testProfile.peer1 = replacement;
        if(testProfile.peer2.macAddress.equals(replacement.macAddress)) testProfile.peer2 = replacement;
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
                addDevice(json, c);
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

        sendRequest(c, PacketID.DEVICE); //require device info from remote phone
    }

    @Override
    public void onDisconnect(Client c) {
        Log(LOG_TAG, "onDisconnect: " + c.getInetAddress().getHostAddress());
        throw new IllegalStateException("Device was disconnected.");
    }
}
