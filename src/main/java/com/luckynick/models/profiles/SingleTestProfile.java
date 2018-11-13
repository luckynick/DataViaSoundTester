package com.luckynick.models.profiles;

import com.luckynick.shared.SharedUtils;
import com.luckynick.shared.enums.SoundConsumptionUnit;
import com.luckynick.shared.enums.SoundProductionUnit;
import com.luckynick.custom.Device;
import com.luckynick.shared.IOClassHandling;
import com.luckynick.models.ManageableField;

import java.util.ArrayList;
import java.util.List;

@IOClassHandling(dataStorage = SharedUtils.DataStorage.SINGLE)
public class SingleTestProfile extends Profile {

    @ManageableField
    public SoundProductionUnit soundProductionUnit = SoundProductionUnit.LOUD_SPEAKERS;
    @ManageableField
    public SoundConsumptionUnit soundConsumptionUnit = SoundConsumptionUnit.MICROPHONE;
    @ManageableField
    public int loudnessLevel = 100;
    @ManageableField
    public List<String> frequenciesBindingShifts = new ArrayList<>();//for spectral analysis;

    @ManageableField(required = true)
    public Dictionary dictionary;
}
