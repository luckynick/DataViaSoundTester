package com.luckynick.shared.net;

import com.luckynick.shared.PureFunctionalInterface;
import com.luckynick.shared.enums.PacketID;
import nl.pvdberg.pnet.client.Client;
import nl.pvdberg.pnet.event.PacketHandler;
import nl.pvdberg.pnet.packet.Packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NetworkExecutionSequence {

    ClientManager cliManager = new ClientManager();

    ClientWrapper client;

    private LinkedList<Command> commands = new LinkedList<>();

    public NetworkExecutionSequence(ClientWrapper target) {
        forClient(target);
    }

    private NetworkExecutionSequence forClient(ClientWrapper target) {
        client = target;
        return this;
    }

    public NetworkExecutionSequence addCommand(PacketID id, PacketHandler handler) {
        commands.add(new Command(id, handler));
        return this;
    }

    public Executable build() {
        return new Executable(this.commands);
    }

    class Command extends Thread implements PacketHandler {

        PacketID id;
        PacketHandler handler;

        public Command(PacketID id, PacketHandler handler) {
            this.handler = handler;
            this.id = id;
        }

        @Override
        public void run() {
            cliManager.subscribePacketListener(id, this);
            try {
                wait();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void handlePacket(Packet p, Client c) throws IOException {
            handler.handlePacket(p, c);
            notifyAll();
        }
    }

    public class Executable {
        LinkedList<Command> commands;

        Executable(LinkedList<Command> commands) {
            this.commands = commands;
        }

        public void execute() {
            for(Command c: commands) {
                c.start();
                try {
                    c.join();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
