package com.luckynick.models.profiles;

import com.luckynick.Utils;
import com.luckynick.models.SerializableModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Profile extends SerializableModel {

    public Profile() {
        filenamePrefix = "profile";
        filename = filenamePrefix + '_' + new SimpleDateFormat("ddMMyy_HHmmss").format(new Date()) + Utils.JSON_EXTENSION;
        appendSubfolderToFileRoot("profiles");
    }
}
