package com.luckynick.models.profiles;

import com.luckynick.models.SerializableModel;
import com.luckynick.shared.SharedUtils;

public abstract class Profile extends SerializableModel {

    @Override
    public void setFilename() {
        setFilename("profile_" + SharedUtils.getDateStringForFileName() + SharedUtils.JSON_EXTENSION);
    }
}
