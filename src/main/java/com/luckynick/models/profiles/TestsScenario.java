package com.luckynick.models.profiles;

import com.luckynick.Utils;
import com.luckynick.shared.IOClassHandling;
import com.luckynick.shared.SharedUtils;

@IOClassHandling(dataStorage = SharedUtils.DataStorage.SCENARIO)
public class TestsScenario extends Profile {

    public SequentialTestProfile[] arrayOtTests = new SequentialTestProfile[0];
}
