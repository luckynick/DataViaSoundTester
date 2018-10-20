package com.luckynick.models.profiles;

import com.luckynick.models.ManageableField;
import com.luckynick.models.SerializableModel;
import com.luckynick.shared.IOClassHandling;
import com.luckynick.shared.SharedUtils;

import java.util.ArrayList;
import java.util.List;

@IOClassHandling(dataStorage = SharedUtils.DataStorage.CONFIG)
public class Config extends SerializableModel {

    @ManageableField(required = true)
    public SequentialTestProfile defaultProfile;

    @Override
    public void setFilename() {
        setFilename("config" + SharedUtils.JSON_EXTENSION);
    }
}
