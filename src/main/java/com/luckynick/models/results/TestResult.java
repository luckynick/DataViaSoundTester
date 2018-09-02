package com.luckynick.models.results;

import com.luckynick.Utils;
import com.luckynick.models.IOClassHandling;
import com.luckynick.models.SerializableModel;

import java.text.SimpleDateFormat;
import java.util.Date;

@IOClassHandling(dataStorage = Utils.DataStorage.NONE)
public abstract class TestResult extends SerializableModel {

    public TestResult() {
        setFilename("result_" + Utils.getDateStringForFileName() + Utils.JSON_EXTENSION);
    }
}
