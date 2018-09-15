package com.luckynick.behaviours.addDevice;

import static com.luckynick.custom.Utils.*;

import com.luckynick.behaviours.ProgramBehaviour;
import com.luckynick.custom.Device;
import com.luckynick.models.ModelIO;
import com.luckynick.net.WindowsNetworkService;
import com.luckynick.shared.net.ConnectionManager;
import com.luckynick.shared.SharedUtils;
import com.luckynick.shared.net.TCPConnection;

import java.net.ConnectException;
import java.util.Scanner;

public class DeviceAdditionBehaviour extends ProgramBehaviour {

    public static final String LOG_TAG = "DeviceAdditionBehaviour";

    ModelIO<Device> modelIO;

    public DeviceAdditionBehaviour() {
        modelIO = new ModelIO<>(Device.class);
    }

    @Override
    public void performProgramTasks() {
        WindowsNetworkService serviceOut = new WindowsNetworkService();
        try (ConnectionManager connectionManager = new ConnectionManager();
             WindowsNetworkService service = serviceOut) {
            //waitConsoleInput("Confirm that hotspot was started [Enter]");
            service.connectWifi();
            if(!service.isWifiConnected()) {
                System.out.println("Failed to establish WIFI connection. Abort.");
                return;
            }
            System.out.println("WIFI connected: " + service.isWifiConnected());


            for(int i = 0; i < 2; i++) {
                int port = SharedUtils.TCP_COMMUNICATION_PORT + i;
                try {
                    TCPConnection s = service.waitForConnection(port);
                    connectionManager.addConnection(s);
                }
                catch (ConnectException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            for(TCPConnection conn : connectionManager.getConnectionIterator()) {
                Device resp = conn.receive(Device.class);
                Log(LOG_TAG, "Received response from device: " + modelIO.serializeStr(resp));
                System.out.println("Connected: " + (conn == null ? "none" : conn.getSocket().isConnected() + " " +
                        conn.getSocket().getInetAddress().getHostAddress()));
            }


        }
    }

    void waitConsoleInput(String info) {
        System.out.println("[INFO] " + info);
        waitConsoleInput();
    }

    void waitConsoleInput() {
        //new Scanner(System.in).next();
        System.out.println("Press \"ENTER\" to continue...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
}
