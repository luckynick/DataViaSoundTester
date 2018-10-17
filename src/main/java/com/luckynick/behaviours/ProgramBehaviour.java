package com.luckynick.behaviours;

import com.luckynick.shared.enums.PacketID;
import nl.pvdberg.pnet.client.Client;
import nl.pvdberg.pnet.packet.Packet;
import nl.pvdberg.pnet.packet.PacketBuilder;

import java.util.Scanner;

public abstract class ProgramBehaviour extends MenuSelectable {

    private String overrideMenuName;

    public ProgramBehaviour() {
    }

    public ProgramBehaviour(String overrideMenuName) {
        this.overrideMenuName = overrideMenuName;
    }

    @Override
    public String toString(){
        return overrideMenuName == null ? this.getClass().getSimpleName() : overrideMenuName;
    }

    public void waitConsoleInput(String info) {
        System.out.println("[INFO] " + info);
        waitConsoleInput();
    }

    public void waitConsoleInput() {
        System.out.println("Press \"ENTER\" to continue...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }

    public void exit() {

        System.exit(0);
    }

    public static void sendRequest(Client c, PacketID requiredResponse) {
        c.send(createRequestPacket(requiredResponse).build());
    }

    public static PacketBuilder createRequestPacket(PacketID requiredResponse) {
        return createRequestPacket().withInt(requiredResponse.ordinal());
    }

    public static PacketBuilder createRequestPacket() {
        PacketBuilder pb = new PacketBuilder(Packet.PacketType.Request);
        pb.withID((short) PacketID.REQUEST.ordinal());
        return pb;
    }
}
