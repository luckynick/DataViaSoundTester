package com.luckynick.behaviours.runTest;

import com.luckynick.behaviours.ProgramBehaviour;
import com.luckynick.custom.Device;
import com.luckynick.models.ModelIO;
import com.luckynick.models.profiles.SequentialTestProfile;
import com.luckynick.models.profiles.SingleTestProfile;
import com.luckynick.models.results.SingleTestResult;
import com.luckynick.models.results.TestsReport;
import com.luckynick.models.results.TestsReportBuilder;
import com.luckynick.net.ConnectionHandler;
import com.luckynick.net.MultiThreadServer;
import com.luckynick.shared.GSONCustomSerializer;
import com.luckynick.shared.SharedUtils;
import com.luckynick.shared.enums.PacketID;
import com.luckynick.shared.model.ReceiveParameters;
import com.luckynick.shared.model.ReceiveSessionSummary;
import com.luckynick.shared.model.SendParameters;
import com.luckynick.shared.model.SendSessionSummary;
import com.luckynick.shared.net.ConnectionListener;
import com.luckynick.shared.net.PacketListener;
import nl.pvdberg.pnet.client.Client;
import nl.pvdberg.pnet.packet.Packet;
import nl.pvdberg.pnet.packet.PacketReader;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.luckynick.custom.Utils.Log;

public class PerformTests extends ProgramBehaviour implements ConnectionListener, PacketListener {

    public static final String LOG_TAG = "PerformTests";

    SequentialTestProfile profile;
    ModelIO<SequentialTestProfile> profileIO = new ModelIO<>(SequentialTestProfile.class);
    Map<String, Client> connections;
    MultiThreadServer server;

    TestsReportBuilder reportBuilder;

    long testTimeMillis = System.currentTimeMillis();

    private Object lock = new Object();

    private volatile SendSessionSummary sendSessionSummary = null;
    private volatile boolean senderReady = false, receiverReady = false;

    public PerformTests(SequentialTestProfile profile, ConcurrentHashMap<String, Client> connections, MultiThreadServer server) {
        this.profile = profile;
        reportBuilder = new TestsReportBuilder(profile);
        this.connections = connections;
        this.server = server;
        this.server.subscribeConnectionEvents(this);
        ConnectionHandler.subscribePacketEvents(this);
    }

    @Override
    public void performProgramTasks() {

        Log(LOG_TAG, "Performing test tasks. Using profile:");
        Log(LOG_TAG, profileIO.serializeStr(profile));

        Thread testsThread = new Thread(new Runnable() {
            @Override
            public void run() {

                for(SingleTestProfile singleTestProfileProfile : profile.testsToPerform) {
                    for(String message : singleTestProfileProfile.dictionary.messages) {
                        SendParameters sParams = new SendParameters();
                        sParams.loudnessLevel = singleTestProfileProfile.loudnessLevel;
                        sParams.soundProductionUnit = singleTestProfileProfile.soundProductionUnit;
                        sParams.message = message;
                        ReceiveParameters rParams = new ReceiveParameters();
                        rParams.soundConsumptionUnit = singleTestProfileProfile.soundConsumptionUnit;
                        sendMessages(sParams, rParams, profile.peer1, profile.peer2);
                        Log(LOG_TAG, "peer1 -> peer2");
                        Log(LOG_TAG, profile.peer1.vendor + " to " + profile.peer2.vendor);
                        synchronized (lock) {
                            try {
                                lock.wait();
                            }
                            catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        sendMessages(sParams, rParams, profile.peer2, profile.peer1);
                        Log(LOG_TAG, "peer2 -> peer1");
                        Log(LOG_TAG, profile.peer2.vendor + " to " + profile.peer1.vendor);
                        synchronized (lock) {
                            try {
                                lock.wait();
                            }
                            catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                TestsReport finalReport = reportBuilder.build();
                ModelIO<TestsReport> reportIO = new ModelIO<>(TestsReport.class);
                try {
                    reportIO.serialize(finalReport);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        testsThread.start();

        //exit();
    }

    private void sendMessages(SendParameters sendParams, ReceiveParameters recvParams, Device sender, Device receiver) {
        currentSender = connections.get(sender.macAddress);
        currentReceiver = connections.get(receiver.macAddress);

        Packet senderPrepPacket = createRequestPacket(PacketID.PREP_SEND_MESSAGE)
                .withString(new GSONCustomSerializer<>(SendParameters.class)
                        .serializeStr(sendParams))
                .build();
        currentSender.send(senderPrepPacket); //expect OK
        Packet receiverPrepPacket = createRequestPacket(PacketID.PREP_RECEIVE_MESSAGE)
                .withString(new GSONCustomSerializer<>(ReceiveParameters.class)
                        .serializeStr(recvParams))
                .build();
        currentReceiver.send(receiverPrepPacket); //expect OK
    }

    private volatile Client currentReceiver = null, currentSender = null;

    private void sendMessages(String message, Device sender, Device receiver) {
        currentSender = connections.get(sender.macAddress);
        currentReceiver = connections.get(receiver.macAddress);

        Packet senderPrepPacket = createRequestPacket(PacketID.PREP_SEND_MESSAGE).withString(message).build();
        currentSender.send(senderPrepPacket); //expect OK
        sendRequest(currentReceiver, PacketID.PREP_RECEIVE_MESSAGE); //expect OK
    }

    @Override
    public void onConnect(Client c) {

    }

    @Override
    public void onDisconnect(Client c) {
        TestsReport finalReport = reportBuilder.build();
        ModelIO<TestsReport> reportIO = new ModelIO<>(TestsReport.class);
        try {
            reportIO.serialize(finalReport);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPacket(Client c, Packet p) {

    }

    @Override
    public void onResponsePacket(Client c, Packet p) {
        PacketReader packetReader = new PacketReader(p);
        try {
            int responseType = packetReader.readInt();
            Log(LOG_TAG, "onResponsePacket: " + PacketID.ordinalToEnum(responseType));
            if(responseType == PacketID.OK.ordinal()) {
                int okForWhat = packetReader.readInt();
                Log(LOG_TAG, "ok for : " + PacketID.ordinalToEnum(okForWhat));
                switch (PacketID.ordinalToEnum(okForWhat)) {
                    case PREP_SEND_MESSAGE:
                        currentSender = c;
                        senderReady = true;
                        tryRunMessageReceiver();
                        break;
                    case PREP_RECEIVE_MESSAGE:
                        currentReceiver = c;
                        receiverReady = true;
                        tryRunMessageReceiver();
                        break;
                    case RECEIVE_MESSAGE:
                        runMessageSender();
                        break;
                    default:
                        Log(LOG_TAG, "Unknown OK response: " + PacketID.ordinalToEnum(okForWhat));
                }
            }
            else if(responseType == PacketID.JOIN.ordinal()) {
                int joinForWhat = packetReader.readInt();
                switch (PacketID.ordinalToEnum(joinForWhat)) {
                    case SEND_MESSAGE:
                        GSONCustomSerializer<SendSessionSummary> serializer = new GSONCustomSerializer<>(SendSessionSummary.class);
                        sendSessionSummary = serializer.deserialize(packetReader.readString());
                        Log(LOG_TAG, "Sender joined.");
                        try {
                            Thread.sleep(1000); // let the recorder settle down
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        pullMessage();
                        break;
                    default:
                        Log(LOG_TAG, "Unknown JOIN response: " + PacketID.ordinalToEnum(joinForWhat));
                }
            }
            else if(responseType == PacketID.TEXT.ordinal()) {
                ModelIO<SingleTestResult> resultIO = new ModelIO<>(SingleTestResult.class);
                GSONCustomSerializer<ReceiveSessionSummary> serializer = new GSONCustomSerializer<>(ReceiveSessionSummary.class);
                String serSummary = packetReader.readString();
                ReceiveSessionSummary recvSummary = serializer.deserialize(serSummary);
                Log(LOG_TAG, serSummary);
                Log(LOG_TAG, "##########################################");
                Log(LOG_TAG, "Receive summary from " + recvSummary.summarySource.vendor);
                Log(LOG_TAG, "RECEIVED MESSAGE: " + recvSummary.message);
                Log(LOG_TAG, "EXCEPTION: " + recvSummary.exceptionDuringDecoding);

                SingleTestResult result = new SingleTestResult(sendSessionSummary, recvSummary);
                String filenameWdateFolder = SharedUtils.formPathString(
                        SharedUtils.getDateStringForFileName(testTimeMillis), result.filename);
                result.setFilename(filenameWdateFolder);
                Log(LOG_TAG, "New SingleTestResult path: " + result.wholePath);
                resultIO.serialize(result);
                reportBuilder.addTestResult(result);

                synchronized (lock) {
                    lock.notifyAll(); //begin next communication session
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * If both sender and receiver are ready -> tell receiver to start recording.
     */
    void tryRunMessageReceiver() {
        if(receiverReady && senderReady && currentReceiver != null){
            Client receiver = currentReceiver;
            receiverReady = false;
            senderReady = false;

            sendRequest(receiver, PacketID.RECEIVE_MESSAGE);
        }
    }

    void runMessageSender() {
        Client sender = currentSender;
        sendRequest(sender, PacketID.SEND_MESSAGE);
    }

    void pullMessage() {
        Client receiver = currentReceiver;
        sendRequest(receiver, PacketID.TEXT);
    }

    @Override
    public void onUnexpectedPacket(Client c, Packet p) {

    }
}
