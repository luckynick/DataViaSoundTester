package com.luckynick.behaviours.runTest;

import com.luckynick.behaviours.ProgramBehaviour;
import com.luckynick.custom.Device;
import com.luckynick.custom.Utils;
import com.luckynick.models.ModelIO;
import com.luckynick.models.ModelSelector;
import com.luckynick.models.profiles.SequentialTestProfile;
import com.luckynick.models.profiles.SingleTestProfile;
import com.luckynick.models.results.CumulatedReport;
import com.luckynick.models.results.SingleTestResult;
import com.luckynick.models.results.TestsReport;
import com.luckynick.models.results.TestsReportBuilder;
import com.luckynick.net.ConnectionHandler;
import com.luckynick.net.MultiThreadServer;
import com.luckynick.shared.GSONCustomSerializer;
import com.luckynick.shared.IOClassHandling;
import com.luckynick.shared.PureFunctionalInterface;
import com.luckynick.shared.SharedUtils;
import com.luckynick.shared.enums.PacketID;
import com.luckynick.shared.model.ReceiveParameters;
import com.luckynick.shared.model.ReceiveSessionSummary;
import com.luckynick.shared.model.SendParameters;
import com.luckynick.shared.model.SendSessionSummary;
import com.luckynick.shared.net.ConnectionListener;
import com.luckynick.shared.net.PacketListener;
import com.sun.istack.internal.Nullable;
import nl.pvdberg.pnet.client.Client;
import nl.pvdberg.pnet.packet.Packet;
import nl.pvdberg.pnet.packet.PacketReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static com.luckynick.custom.Utils.Log;

/*
TODO: make sure that sound production starts after start of recording
there might be async tasks which finish with delay
 */
/*
Observation:
There is some kind of warm up (of unknown nature).
First tests demonstrate worse results than last ones.
*/
public class PerformTests extends ProgramBehaviour implements ConnectionListener, PacketListener {

    public static final String LOG_TAG = "PerformTests";

    public static List<PureFunctionalInterface> testEndSubs = new ArrayList<>();

    SequentialTestProfile profile;
    ModelIO<SequentialTestProfile> profileIO = new ModelIO<>(SequentialTestProfile.class);
    Map<String, Client> connections;
    private volatile Client currentReceiver = null, currentSender = null;
    MultiThreadServer server;

    TestsReportBuilder reportBuilder;

    long testTimeMillis = System.currentTimeMillis();

    private Object lock = new Object();

    private volatile SendSessionSummary sendSessionSummary = null;
    private volatile boolean senderReady = false, receiverReady = false;

    public PerformTests(SequentialTestProfile profile, ConcurrentHashMap<String, Client> connections,
                        MultiThreadServer server, @Nullable String continuePreviousTest) {
        this.profile = profile;
        reportBuilder = new TestsReportBuilder(profile);
        this.connections = connections;
        this.server = server;
        this.server.subscribeConnectionEvents(this);
        ConnectionHandler.subscribePacketEvents(this);

        if(continuePreviousTest != null) {
            ModelIO<SingleTestResult> resultIO = new ModelIO<>(SingleTestResult.class);
            //String dirStr = SharedUtils.formPathString(SharedUtils.DataStorage.SINGULAR_RESULT.getDirPath(), "v1.0-naive_10-100_111118_234659\\");
            String dirStr = continuePreviousTest;
            ArrayList<File> files = new ArrayList<>();
            Path dir = Paths.get(dirStr);
            try (Stream<Path> paths = Files.walk(dir)) {
                paths
                        .filter(Files::isRegularFile)
                        .filter((p) -> p.toString().endsWith(SharedUtils.JSON_EXTENSION))
                        //.filter((p) -> p.getParent().equals(dir))
                        .forEach((p) -> {files.add(p.toFile());});
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            for(File f : files) {
                try {
                    reportBuilder.addTestResult(resultIO.deserialize(f));
                    //modelObjects.add(resultIO.deserialize(f));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log(LOG_TAG, "Builder was prepopulated, now contains " + reportBuilder.getNumOfTestResults() + " entries.");
        }
    }

    @Override
    public void performProgramTasks() {

        Log(LOG_TAG, "Performing test tasks. Using profile:");
        Log(LOG_TAG, profileIO.serializeStr(profile));

        Thread testsThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for(SingleTestProfile singleTestProfileProfile : profile.testsToPerform) {
                    List<String> messages = singleTestProfileProfile.dictionary.messages;
                    for(String message : messages) {
                        if(reportBuilder.getNumOfTestResults() > c) {
                            Log(LOG_TAG, "Skipping: " + c);
                            continue;
                        }
                        c+=2;
                        if(singleTestProfileProfile.frequenciesBindingShifts.size() == 0) {
                            performSingleTest(singleTestProfileProfile, message);
                        }
                        else {
                            for(String freqBaseShift: singleTestProfileProfile.frequenciesBindingShifts) {
                                performSingleTest(singleTestProfileProfile, message, Integer.parseInt(freqBaseShift),
                                        singleTestProfileProfile.frequenciesBindingScale);
                            }
                        }
                    }
                }

                TestsReport finalReport = reportBuilder.build();
                finalReport.customPrefix = profile.customPrefix;
                finalReport.setFilename();
                ModelIO<TestsReport> reportIO = new ModelIO<>(TestsReport.class);
                try {
                    reportIO.serialize(finalReport);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                finalizeTests();
                Log(LOG_TAG, "TEST WAS FINISHED SUCCESSFULLY");
                testEndSubs.forEach(func -> func.performProgramTasks());
                //exit();
            }
        });
        testsThread.start();
    }

    private void performSingleTest(SingleTestProfile singleTestProfileProfile, String message) {
        performSingleTest(singleTestProfileProfile, message, Utils.TEST_FREQ_BINDING_BASE,
                Utils.TEST_FREQ_BINDING_SCALE);
    }


    int c = 0; //pass tests eventually, if previous test sequence was interrupted
    private void performSingleTest(SingleTestProfile singleTestProfileProfile, String message, int frequenciesBindingShift,
                                   double frequenciesBindingScale) {
        SendParameters sParams = new SendParameters();
        ReceiveParameters rParams = new ReceiveParameters();
        sParams.loudnessLevel = singleTestProfileProfile.loudnessLevel;
        sParams.soundProductionUnit = singleTestProfileProfile.soundProductionUnit;
                        /*if(profile.spectralAnalysis) {
                            sParams.message = profile.spectralAnalysisString;
                            sParams.spectralAnalysisShift = Integer.parseInt(message);
                            rParams.spectralAnalysisShift = Integer.parseInt(message);
                        }
                        else {
                            sParams.message = message;
                        }*/

        sParams.message = message;
        sParams.frequenciesBindingShift = frequenciesBindingShift;
        sParams.frequenciesBindingScale = frequenciesBindingScale;

        rParams.soundConsumptionUnit = singleTestProfileProfile.soundConsumptionUnit;
        rParams.frequenciesBindingShift = frequenciesBindingShift;
        rParams.frequenciesBindingScale = frequenciesBindingScale;

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


    public void finalizeTests() {
        this.server.unsubscribe();
        ConnectionHandler.packetListeners = new ArrayList<>();

        /*
        this.connections.values().forEach(conn -> conn.close());
        this.currentSender.close();
        this.currentReceiver.close();
        */
    }

    public static void main(String args[]) {
        /*
        ModelIO<CumulatedReport> reportIO = new ModelIO<>(CumulatedReport.class);
        CumulatedReport rep = ModelSelector.requireSelection(reportIO, false).get(0);
        for (TestsReport r : rep.testsReports) {
            System.out.println(r.filename);
        }
        */
        ModelIO<SingleTestResult> resultIO = new ModelIO<>(SingleTestResult.class);
        String dirStr = SharedUtils.formPathString(SharedUtils.DataStorage.SINGULAR_RESULT.getDirPath(), "v1.0-naive_10-100_111118_234659\\");
        ArrayList<File> files = new ArrayList<>();
        Path dir = Paths.get(dirStr);
        try (Stream<Path> paths = Files.walk(dir)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter((p) -> p.toString().endsWith(SharedUtils.JSON_EXTENSION))
                    //.filter((p) -> p.getParent().equals(dir))
                    .forEach((p) -> {files.add(p.toFile());});
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        List<SingleTestResult> modelObjects = new ArrayList<>();
        for(File f : files) {
            try {
                modelObjects.add(resultIO.deserialize(f));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        TestsReportBuilder reportBuilder = new TestsReportBuilder(null);
        for (SingleTestResult res : modelObjects) {
            reportBuilder.addTestResult(res);
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
                        try {
                            //0.5 second pause after start of recording
                            Thread.sleep(500);
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
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

                        /*
                        try {
                            Thread.sleep(1000); // let the recorder settle down //1000 500
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        */
                        try {
                            //0.5 second pause after end of sending
                            Thread.sleep(500);
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
                        profile.customPrefix + SharedUtils.getDateStringForFileName(testTimeMillis), result.filename);
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
