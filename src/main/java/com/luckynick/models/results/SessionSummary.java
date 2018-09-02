package com.luckynick.models.results;

import com.luckynick.Utils;
import com.luckynick.models.Device;
import com.luckynick.models.SerializableModel;
import com.luckynick.models.IOClassHandling;

import java.util.Date;

/**
 * Not serialized independently, but inside of test result
 */
@IOClassHandling(sendViaNetwork = true, dataStorage = Utils.DataStorage.NONE)
class SessionSummary extends SerializableModel {

     Device dataSource;
    /**
     * Depending on role of device which sent this summary (sender/receiver),
     * it is either sent or decoded data
     */
    String data;
    Date sessionStartDate;
}
