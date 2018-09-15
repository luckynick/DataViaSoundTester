package com.luckynick.models.results;

import com.luckynick.shared.IOClassHandling;
import com.luckynick.models.SerializableModel;
import com.luckynick.shared.SharedUtils;

@IOClassHandling(dataStorage = SharedUtils.DataStorage.NONE)
public abstract class TestResult extends SerializableModel {

    public TestResult() {
        setFilename("result_" + SharedUtils.getDateStringForFileName() + SharedUtils.JSON_EXTENSION);
    }
}
