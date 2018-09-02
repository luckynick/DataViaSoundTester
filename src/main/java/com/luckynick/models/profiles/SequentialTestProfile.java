package com.luckynick.models.profiles;

import com.luckynick.Utils;
import com.luckynick.enums.DistanceUnit;
import com.luckynick.models.Device;
import com.luckynick.models.IOClassHandling;
import com.luckynick.models.ManageableField;

@IOClassHandling(dataStorage = Utils.DataStorage.SEQUENTIAL)
public class SequentialTestProfile extends Profile {
    protected Device controller;
    protected Device peer1;
    protected Device peer2;

    @ManageableField(required = true)
    protected int distanceBetweenPeers;
    @ManageableField
    protected DistanceUnit distanceUnitName = DistanceUnit.Centimeter;

    //TODO
    //@ManageableField
    protected SingleTestProfile[] testsToPerform;
}
