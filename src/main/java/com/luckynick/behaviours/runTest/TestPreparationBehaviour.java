package com.luckynick.behaviours.runTest;

import com.luckynick.behaviours.ProgramBehaviour;
import com.luckynick.custom.Device;
import com.luckynick.models.ModelIO;
import com.luckynick.models.profiles.SequentialTestProfile;
import com.luckynick.models.profiles.SingleTestProfile;
import com.luckynick.net.ConnectionHandler;
import com.luckynick.net.MultiThreadServer;
import com.luckynick.net.WindowsNetworkService;
import com.luckynick.shared.SharedUtils;
import com.luckynick.shared.ValidationException;
import com.luckynick.shared.enums.PacketID;
import com.luckynick.shared.net.ConnectionListener;
import com.luckynick.shared.net.PacketListener;
import com.luckynick.shared.net.UDPServer;
import nl.pvdberg.pnet.client.Client;
import nl.pvdberg.pnet.packet.Packet;
import nl.pvdberg.pnet.packet.PacketReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.luckynick.custom.Utils.Log;

public class TestPreparationBehaviour extends ProgramBehaviour implements PacketListener, ConnectionListener {

    public static final String LOG_TAG = "TestPreparationBehaviour";

    //private final String INTERRUPTED_DIR = SharedUtils.formPathString(SharedUtils.DataStorage.SINGULAR_RESULT.getDirPath(),
    //       "v1.0-naive_10-100_291118_225308\\");
    final String INTERRUPTED_DIR = null;

    Thread udpBroadcastThread;
    int tcpPort = SharedUtils.TCP_COMMUNICATION_PORT;

    ModelIO<Device> deviceModelIO = new ModelIO<>(Device.class);

    List<Device> connectedDevices = new ArrayList<>();
    List<Device> allowedDevices = new ArrayList<>();
    private final int REQUIRED_DEVICES_AMT = 2;
    SequentialTestProfile sequentialTestProfile;
    MultiThreadServer server;

    final SharedUtils.Counter testCounter;

    private ConcurrentHashMap<String, Client> connections = new ConcurrentHashMap<>();

    public TestPreparationBehaviour(SequentialTestProfile seqTestPro, int repeatNtimes) {
        testCounter = new SharedUtils.Counter(repeatNtimes);
        this.sequentialTestProfile = seqTestPro;
    }

    @Override
    public void performProgramTasks() {
        validate(sequentialTestProfile);
        allowedDevices.add(sequentialTestProfile.peer1);
        allowedDevices.add(sequentialTestProfile.peer2);

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
                    sequentialTestProfile.peer1 = connectedDevices.get(0);
                    sequentialTestProfile.peer2 = connectedDevices.get(1);
                    ConnectionHandler.packetListeners.remove(this);

                    //PerformTests per = new PerformTests(sequentialTestProfile, connections, server);

                    new PerformTests(sequentialTestProfile, connections, server, INTERRUPTED_DIR).performProgramTasks();
                    PerformTests.testEndSubs.add(() -> {
                        if(!testCounter.reachedMaximum()) {
                            new PerformTests(sequentialTestProfile, connections, server, null).performProgramTasks();
                            testCounter.increment();
                        }
                        else {
                            finalizeTests();
                            exit();
                        }
                    });

                    /*
                    for(int i = 0; i < 4; i++) {
                        per.performProgramTasks();
                    }
                    finalizeTests();
                    exit();
                    */
                }
                return;
            }
        }
    }

    public void finalizeTests() {
        if(this.server != null) server.close();
        this.connections.values().forEach(conn -> {if(conn != null) conn.close();});
        if(this.udpBroadcastThread != null) this.udpBroadcastThread.interrupt();
    }

    private void replaceDeviceInProfile(Device replacement) {
        if(sequentialTestProfile.peer1.macAddress.equals(replacement.macAddress)) sequentialTestProfile.peer1 = replacement;
        if(sequentialTestProfile.peer2.macAddress.equals(replacement.macAddress)) sequentialTestProfile.peer2 = replacement;
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

    public static void validate(SequentialTestProfile toValidate) {
        String spectralAnalysisString = null;
        if(toValidate.spectralAnalysis) {
            for(SingleTestProfile sProf: toValidate.testsToPerform) {
                if(spectralAnalysisString == null) {
                    spectralAnalysisString = sProf.dictionary.messages.get(0);
                    if(spectralAnalysisString == null || spectralAnalysisString.trim().equals("")){
                        throw new ValidationException("Spectral analysis string '"+spectralAnalysisString+
                                "' is empty.");
                    }
                }
                if(sProf.dictionary.messages.size() != 1 || !spectralAnalysisString.equals(sProf.dictionary.messages.get(0))){
                    throw new ValidationException("Dictionaries must be equal for spectral analysis test.");
                }
                for(String freqBaseShift: sProf.frequenciesBindingShifts) {
                    try{
                        Integer.parseInt(freqBaseShift);
                    }
                    catch (NumberFormatException e) {
                        throw new ValidationException("Frequency shift '"+freqBaseShift+"' can't be used in spectral\n " +
                                "analysis test because it can't be parsed to int.");
                    }
                }
            }
        }
    }
}
