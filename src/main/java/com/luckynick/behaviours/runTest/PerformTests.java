package com.luckynick.behaviours.runTest;

import com.luckynick.behaviours.ProgramBehaviour;
import com.luckynick.custom.Device;
import com.luckynick.models.ModelIO;
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
import nl.pvdberg.pnet.packet.PacketBuilder;
import nl.pvdberg.pnet.packet.PacketReader;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.luckynick.custom.Utils.Log;

public class PerformTests extends ProgramBehaviour {

    public static final String LOG_TAG = "PerformTests";

    SequentialTestProfile profile;
    ModelIO<SequentialTestProfile> profileIO = new ModelIO<>(SequentialTestProfile.class);

    public PerformTests(SequentialTestProfile profile) {
        this.profile = profile;
    }

    @Override
    public void performProgramTasks() {

        Log(LOG_TAG, "Performing test tasks. Using profile:");
        Log(LOG_TAG, profileIO.serializeStr(profile));

        exit();
    }
}
