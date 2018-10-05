package com.luckynick.models.profiles;

import com.luckynick.models.SerializableModel;
import com.luckynick.shared.IOClassHandling;
import com.luckynick.shared.SharedUtils;

import java.util.ArrayList;
import java.util.List;

@IOClassHandling(dataStorage = SharedUtils.DataStorage.DICTIONARY)
public class Dictionary extends SerializableModel {

    List<String> messages = new ArrayList<>();

    public Dictionary() {
        setFilename("dictionary_" + SharedUtils.getDateStringForFileName() + SharedUtils.JSON_EXTENSION);
    }
}
