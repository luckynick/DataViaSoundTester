package com.luckynick.models.profiles;

import com.luckynick.shared.SharedUtils;
import com.luckynick.shared.enums.DistanceUnit;
import com.luckynick.custom.Device;
import com.luckynick.shared.IOClassHandling;
import com.luckynick.models.ManageableField;

@IOClassHandling(dataStorage = SharedUtils.DataStorage.SEQUENTIAL)
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
