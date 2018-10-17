package com.luckynick.net;

import com.luckynick.shared.net.ConnectionListener;
import nl.pvdberg.pnet.client.Client;
import nl.pvdberg.pnet.event.DistributerListener;
import nl.pvdberg.pnet.event.PacketDistributer;
import nl.pvdberg.pnet.packet.Packet;
import nl.pvdberg.pnet.server.Server;
import nl.pvdberg.pnet.server.util.PlainServer;
import nl.pvdberg.pnet.threading.ThreadManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.luckynick.custom.Utils.Log;

public class MultiThreadServer
{
    public static final String LOG_TAG = "ClientManager";

    Server server = null;

    PacketDistributer packetDistributer = new PacketDistributer();

    public List<ConnectionListener> connectionListeners = new ArrayList<>(); //for internal use on server machine
    /**
     * Multithread server. Start listening for new connections on given port.
     * @param port
     * @throws IOException
     */
    public MultiThreadServer(int port)
    {
        Log(LOG_TAG, "Starting MultiThreadServer.");

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                server.stop();
                ThreadManager.shutdown();
            }
        }));
        //if server was closed unexpectedly - stop all connection handlers

        try {
            server = new PlainServer();
            server.setListener(new DistributerListener(packetDistributer){
                @Override
                public void onConnect(final Client c)
                {
                    MultiThreadServer.this.onConnect(c);
                }

                @Override
                public void onDisconnect(final Client c)
                {
                    MultiThreadServer.this.onDisconnect(c);
                }
            });
            packetDistributer.setDefaultHandler((Packet p, Client c) -> {
                Log(LOG_TAG, "Received packet (" + p + "), DOING NOTHING!");
            });
            server.start(port);
        }
        catch (IOException e) {
            e.printStackTrace();
        }


        /*ServerSocket ss = new ServerSocket(port);
        while(true)
        {
            Socket conn = ss.accept();
            System.out.println("New connection from " + conn.getInetAddress());
            ConnectionHandler c = new ConnectionHandler(conn); //create server-side handler of connection
            c.start (); //start thread
        }*/
    }

    public void onConnect(Client c) {
        System.out.println("New connection from " + c.getInetAddress() + ". Starting ConnectionHandler.");
        try {
            new ConnectionHandler(c); //create server-side handler of connection
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        for(ConnectionListener cl : connectionListeners) {
            cl.onConnect(c);
        }
    }

    public void onDisconnect(Client c) {
        Log(LOG_TAG, c.getInetAddress().getHostAddress() + ":" + c.getSocket().getPort() + " disconnected.");
        for(ConnectionListener cl : connectionListeners) {
            cl.onDisconnect(c);
        }
    }

    public void subscribeConnectionEvents(ConnectionListener listener) {
        connectionListeners.add(listener);
    }
}
