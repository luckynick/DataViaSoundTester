package com.luckynick.net;

import static com.luckynick.custom.Utils.*;

import com.luckynick.shared.enums.PacketID;
import com.luckynick.shared.net.ConnectionListener;
import com.luckynick.shared.net.PacketListener;
import nl.pvdberg.pnet.client.Client;
import nl.pvdberg.pnet.event.PNetListener;
import nl.pvdberg.pnet.packet.Packet;

import java.io.*;
import java.util.*;

public class ConnectionHandler {

    public static final String LOG_TAG = "ConnectionHandler";
    /**
     * List of all server-side connection handlers.
     */
    protected static List<ConnectionHandler> handlers = new ArrayList<>();

    public static List<PacketListener> packetListeners = new ArrayList<>(); //for internal use on server machine

    private String ip;

    protected Client connection;

    /*protected DataInputStream in;
    protected DataOutputStream out;*/

    /**
     * Initialize this handler. Decide if this handler has admin rights.
     * @param connection
     * @throws IOException
     */
    /*ConnectionHandler(Socket connection) throws IOException
    {
        in = new DataInputStream(new BufferedInputStream(connection.getInputStream()));
        out = new DataOutputStream(new BufferedOutputStream(connection.getOutputStream()));
        this.connection = connection; //keep socket object in Handler
        ip = connection.getInetAddress().getHostAddress();
    }*/

    /**
     * Initialize this handler. Decide if this handler has admin rights.
     * @param connection
     * @throws IOException
     */
    ConnectionHandler(Client connection) throws IOException
    {
        this.connection = connection; //keep socket object in Handler
        ip = connection.getInetAddress().getHostAddress();
        synchronized(handlers) //only one thread can change list of handlers in the same time
        {
            handlers.add(this);
        }
        this.connection.setClientListener(new PNetListener() {
            @Override
            public void onConnect(Client c) {
                Log(LOG_TAG, "onConnect");
            }

            @Override
            public void onDisconnect(Client c) {
                Log(LOG_TAG, "onDisconnect");
                stop();
            }

            @Override
            public void onReceive(Packet p, Client c) throws IOException {
                Log(LOG_TAG, "onReceive");
                ConnectionHandler.this.onReceive(p,c);
            }
        });
    }

    public static void subscribePacketEvents(PacketListener listener) {
        packetListeners.add(listener);
    }

    /**
     * Check if there is handler on this server with field
     * 'username' equal to argument for this method.
     * @param ip to search
     * @return true if user exists
     */
    protected boolean ipExists(String ip)
    {
        for(ConnectionHandler handler : handlers)
        {
            if(handler.getIp() == null) continue;
            if(handler.getIp().equals(ip)) return true;
        }
        return false;
    }

    /**
     * Send response to every client in this chat including
     * this client excluding private chats.
     */
    protected void toAll(Packet packet)
    {
        broadcast(packet);
        respond(packet);
    }

    /**
     * Send response to every client in this chat excluding
     * private chats and client connected to this handler.
     */
    protected void broadcast (Packet packet) {
        synchronized (handlers) {
            for (ConnectionHandler handler : handlers)
            {
                if (handler == this) continue; //don't send response to this handler
                handler.connection.send(packet);
            }
        }
    }

    /**
     * Send response to client.
     */
    protected void respond(Packet packet)
    {
        this.connection.send(packet);
    }

    public String getIp() {
        return ip;
    }

    public void stop() {
        synchronized(handlers)
        {
            handlers.remove(this);
        }
        connection.close();
    }

    public void onReceive(Packet p, Client c) throws IOException {
        System.out.println("Packet (client " + ip + "): " + p);
        switch(PacketID.ordinalToEnum(p.getPacketID())) //react on request
        {
            case UNDEFINED:
                for(PacketListener lis : packetListeners) {
                    lis.onUnexpectedPacket(c, p);
                }
                break;
            case REQUEST:
                for(PacketListener lis : packetListeners) {
                    lis.onRequestPacket(c, p);
                }
                break;
            case RESPONSE:
                for(PacketListener lis : packetListeners) {
                    lis.onResponsePacket(c, p);
                }
                break;
            default:
                for(PacketListener lis : packetListeners) {
                    lis.onUnexpectedPacket(c, p);
                }
                break;
        }
    }
}
