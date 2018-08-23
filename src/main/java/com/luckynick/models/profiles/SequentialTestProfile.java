package com.luckynick.models.profiles;

import com.luckynick.Utils;
import com.luckynick.enums.DistanceUnit;
import com.luckynick.models.Device;
import com.luckynick.models.SerializableModel;

import java.io.File;

public class SequentialTestProfile extends Profile {
    protected Device controller;
    protected Device peer1;
    protected Device peer2;

    @Configurable
    protected int distanceBetweenPeers;
    @Configurable
    protected DistanceUnit distanceUnitName = DistanceUnit.CENTIMETER;

    //TODO
    //@Configurable
    protected SingleTestProfile[] testsToPerform;

    public SequentialTestProfile() {
        appendSubfolderToFileRoot("sequential");
    }
}
