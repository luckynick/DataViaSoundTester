package com.luckynick.models.profiles;

import com.luckynick.shared.SharedUtils;
import com.luckynick.shared.enums.SoundConsumptionUnit;
import com.luckynick.shared.enums.SoundProductionUnit;
import com.luckynick.custom.Device;
import com.luckynick.shared.IOClassHandling;
import com.luckynick.models.ManageableField;

@IOClassHandling(dataStorage = SharedUtils.DataStorage.SINGLE)
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
