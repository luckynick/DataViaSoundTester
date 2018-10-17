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

    private boolean senderReady = false, receiverReady = false;

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

        for(SingleTestProfile singleTestProfileProfile : profile.testsToPerform) {
            for(String message : singleTestProfileProfile.dictionary.messages) {
                prepSendMessage(message, profile.peer1, profile.peer2);
                prepSendMessage(message, profile.peer2, profile.peer1);
            }
        }

        //exit();
    }

    private void prepSendMessage(String message, Device sender, Device receiver) {
        Client senderConn = connections.get(sender.macAddress);
        Client receiverConn = connections.get(receiver.macAddress);

        Packet senderPrepPacket = createRequestPacket(PacketID.PREP_SEND_MESSAGE).withString(message).build();
        senderConn.send(senderPrepPacket); //expect OK
        sendRequest(receiverConn, PacketID.PREP_RECEIVE_MESSAGE); //expect OK
    }

    private void executePreparedAction() {
        for(Client conn : connections.values()) {
            sendRequest(conn, PacketID.EXECUTE);
        }
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
            if(responseType == PacketID.OK.ordinal()) {
                int okForWhat = packetReader.readInt();
                switch (PacketID.ordinalToEnum(okForWhat)) {
                    case PREP_SEND_MESSAGE:
                        senderReady = true;
                        break;
                    case PREP_RECEIVE_MESSAGE:
                        receiverReady = true;
                        break;
                    default:
                        Log(LOG_TAG, "Unknown OK response.");
                }
                if(senderReady && receiverReady) executePreparedAction();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUnexpectedPacket(Client c, Packet p) {

    }
}
