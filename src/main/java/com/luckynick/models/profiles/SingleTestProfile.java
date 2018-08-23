package com.luckynick.models.profiles;

import com.luckynick.Utils;
import com.luckynick.enums.SoundConsumptionUnit;
import com.luckynick.enums.SoundProductionUnit;
import com.luckynick.models.Device;
import com.luckynick.models.SerializableModel;

import java.io.File;

public class SingleTestProfile extends Profile {

    protected Device sender;
    protected Device receiver;
    @Configurable
    protected SoundProductionUnit soundProductionUnit;
    @Configurable
    protected SoundConsumptionUnit soundConsumptionUnit;
    @Configurable
    protected int loudnessLevel;

    public SingleTestProfile() {
        appendSubfolderToFileRoot("single");
    }
}
