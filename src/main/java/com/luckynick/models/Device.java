package com.luckynick.models;

import com.luckynick.enums.TestRole;
import com.luckynick.models.profiles.UpdateFieldOnStartup;

public class Device {
    public String vendor;
    public String model;
    public String androidVersion;
    public TestRole roleOfParticipant;

    @UpdateFieldOnStartup
    public String localIP;
}
