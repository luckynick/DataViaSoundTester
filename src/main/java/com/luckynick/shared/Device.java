package com.luckynick.shared;

import com.luckynick.shared.enums.TestRole;

@IOClassHandling(dataStorage = SharedUtils.DataStorage.DEVICES, sendViaNetwork = true)
public class Device {
    @IOFieldHandling(serialize = false, updateOnLoad = true)
    public String macAddress;
    @IOFieldHandling(serialize = false, updateOnLoad = true)
    public String localIP;

    public String vendor;
    public String model;
    public String androidVersion;
    public TestRole roleOfParticipant;
}
