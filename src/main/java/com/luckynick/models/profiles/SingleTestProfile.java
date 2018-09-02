package com.luckynick.models.profiles;

import com.luckynick.Utils;
import com.luckynick.enums.SoundConsumptionUnit;
import com.luckynick.enums.SoundProductionUnit;
import com.luckynick.models.Device;
import com.luckynick.models.IOClassHandling;
import com.luckynick.models.ManageableField;

@IOClassHandling(dataStorage = Utils.DataStorage.SINGLE)
public class SingleTestProfile extends Profile {

    protected Device sender;
    protected Device receiver;

    @ManageableField
    protected SoundProductionUnit soundProductionUnit = SoundProductionUnit.LOUD_SPEAKERS;
    @ManageableField
    protected SoundConsumptionUnit soundConsumptionUnit = SoundConsumptionUnit.MICROPHONE;
    @ManageableField
    protected int loudnessLevel = 100;
}
