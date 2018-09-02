package com.luckynick.models.profiles;

import com.luckynick.Utils;
import com.luckynick.models.IOClassHandling;

@IOClassHandling(dataStorage = Utils.DataStorage.SCENARIO)
public class TestsScenario extends Profile {

    public SequentialTestProfile[] arrayOtTests = new SequentialTestProfile[0];
}
