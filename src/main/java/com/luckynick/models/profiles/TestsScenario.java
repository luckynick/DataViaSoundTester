package com.luckynick.models.profiles;

import com.luckynick.shared.IOClassHandling;
import com.luckynick.shared.SharedUtils;

import java.util.ArrayList;
import java.util.List;

@IOClassHandling(dataStorage = SharedUtils.DataStorage.SCENARIO)
public class TestsScenario extends Profile {

    public List<SequentialTestProfile> listOtTests = new ArrayList<>();
}
