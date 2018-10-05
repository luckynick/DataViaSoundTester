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
    protected Device controller;
    @ManageableField(required = true)
    protected Device peer1;
    @ManageableField(required = true)
    protected Device peer2;

    @ManageableField(required = true)
    protected int distanceBetweenPeers;
    @ManageableField
    protected DistanceUnit distanceUnitName = DistanceUnit.Centimeter;

    //TODO
    @ManageableField(required = true)
    public List<SingleTestProfile> testsToPerform = new ArrayList<>();
}
