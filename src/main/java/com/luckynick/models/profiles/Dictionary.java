package com.luckynick.models.profiles;

import com.luckynick.models.ManageableField;
import com.luckynick.models.SerializableModel;
import com.luckynick.shared.IOClassHandling;
import com.luckynick.shared.SharedUtils;

import java.util.ArrayList;
import java.util.List;

@IOClassHandling(dataStorage = SharedUtils.DataStorage.DICTIONARY)
public class Dictionary extends SerializableModel {

    @ManageableField(required = true)
    public List<String> messages = new ArrayList<>();

    @Override
    public void setFilename() {
        setFilename("dictionary_" + SharedUtils.getDateStringForFileName() + SharedUtils.JSON_EXTENSION);
    }
}
