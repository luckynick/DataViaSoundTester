package com.luckynick.models.results;

import com.luckynick.custom.Device;
import com.luckynick.models.SerializableModel;
import com.luckynick.shared.IOClassHandling;
import com.luckynick.shared.SharedUtils;

import java.util.Date;

/**
 * Not serialized independently, but inside of test result
 */
@IOClassHandling(sendViaNetwork = true, dataStorage = SharedUtils.DataStorage.NONE)
class SessionSummary extends SerializableModel {

     Device dataSource;
    /**
     * Depending on role of device which sent this summary (sender/receiver),
     * it is either sent or decoded data
     */
    String data;
    Date sessionStartDate;
}
