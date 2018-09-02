package com.luckynick.models.profiles;

import com.luckynick.Utils;
import com.luckynick.models.SerializableModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class Profile extends SerializableModel {

    public Profile() {
        setFilename("profile_" + Utils.getDateStringForFileName() + Utils.JSON_EXTENSION);
    }
}
