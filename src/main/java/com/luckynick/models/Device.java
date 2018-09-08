package com.luckynick.models;

import com.luckynick.shared.IOClassHandling;
import com.luckynick.shared.IOFieldHandling;
import com.luckynick.shared.SharedUtils;
import com.luckynick.shared.enums.TestRole;

@IOClassHandling(dataStorage = SharedUtils.DataStorage.DEVICES, sendViaNetwork = true)
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
        setFilename("device_" + vendor + '_' + model + '_' + SharedUtils.getDateStringForFileName()
                + SharedUtils.JSON_EXTENSION);
    }
}
