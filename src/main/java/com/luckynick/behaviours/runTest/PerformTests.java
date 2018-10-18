package com.luckynick.behaviours.runTest;

import com.luckynick.behaviours.ProgramBehaviour;
import com.luckynick.custom.Device;
import com.luckynick.models.ModelIO;
import com.luckynick.models.profiles.SequentialTestProfile;
import com.luckynick.models.profiles.SingleTestProfile;
import com.luckynick.net.ConnectionHandler;
import com.luckynick.net.MultiThreadServer;
import com.luckynick.shared.enums.PacketID;
import com.luckynick.shared.net.ConnectionListener;
import com.luckynick.shared.net.PacketListener;
import nl.pvdberg.pnet.client.Client;
import nl.pvdberg.pnet.packet.Packet;
import nl.pvdberg.pnet.packet.PacketReader;

import java.io.IOException;
import java.util.Map;

import static com.luckynick.custom.Utils.Log;

public class PerformTests extends ProgramBehaviour implements ConnectionListener, PacketListener {

    public static final String LOG_TAG = "PerformTests";

    SequentialTestProfile profile;
    ModelIO<SequentialTestProfile> profileIO = new ModelIO<>(SequentialTestProfile.class);
    Map<String, Client> connections;
    MultiThreadServer server;

    private Object lock = new Object();

    private volatile boolean senderReady = false, receiverReady = false;

    public PerformTests(SequentialTestProfile profile, Map<String, Client> connections, MultiThreadServer server) {
        this.profile = profile;
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
                        prepSendMessage(message, profile.peer1, profile.peer2);
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
                        prepSendMessage(message, profile.peer2, profile.peer1);
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
            }
        });
        testsThread.start();

        //exit();
    }

    private volatile Client currentReceiver = null, currentSender = null;

    private void prepSendMessage(String message, Device sender, Device receiver) {
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
                        Log(LOG_TAG, "Sender joined.");
                        pullMessage();
                        break;
                    default:
                        Log(LOG_TAG, "Unknown JOIN response: " + PacketID.ordinalToEnum(joinForWhat));
                }
            }
            else if(responseType == PacketID.TEXT.ordinal()) {
                String receivedMessage = packetReader.readString();
                Log(LOG_TAG, "##########################################");
                Log(LOG_TAG, "From " + c.getInetAddress().getHostAddress());
                Log(LOG_TAG, "RECEIVED MESSAGE: " + receivedMessage);
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
