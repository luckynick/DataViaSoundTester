package com.luckynick.models;

import com.luckynick.Utils;
import com.luckynick.enums.TestRole;

@IOClassHandling(dataStorage = Utils.DataStorage.DEVICES, sendViaNetwork = true)
public class Device extends SerializableModel {
    @IOFieldHandling(serialize = false, updateOnLoad = true)
    public String macAddress;
    @IOFieldHandling(serialize = false, updateOnLoad = true)
    public String localIP;

    public String vendor;
    public String model;
    public String androidVersion;
    public TestRole roleOfParticipant;


    public Device() {
        setFilename("device_" + vendor + '_' + model + '_' + Utils.getDateStringForFileName() + Utils.JSON_EXTENSION);
    }
}
