package com.luckynick.models.profiles;

import com.luckynick.shared.SharedUtils;
import com.luckynick.shared.enums.DistanceUnit;
import com.luckynick.custom.Device;
import com.luckynick.shared.IOClassHandling;
import com.luckynick.models.ManageableField;

import java.util.ArrayList;
import java.util.List;

@IOClassHandling(dataStorage = SharedUtils.DataStorage.SEQUENTIAL)
public class SequentialTestProfile extends Profile {
    public Device controller;
    @ManageableField(required = true)
    public Device peer1;
    @ManageableField(required = true)
    public Device peer2;

    @ManageableField(required = true)
    public int distanceBetweenPeers;
    @ManageableField
    public DistanceUnit distanceUnitName = DistanceUnit.Centimeter;

    @ManageableField(required = true)
    public List<SingleTestProfile> testsToPerform = new ArrayList<>();
}
