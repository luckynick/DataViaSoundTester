package com.luckynick.models.profiles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.luckynick.models.ModelIO;
import com.luckynick.models.profiles.Profile;

import java.io.*;
import java.nio.file.Paths;

public class ProfileIO<T extends Profile> extends ModelIO<T> {

    public ProfileIO(String path, Class classOfModel) {
        super(path, classOfModel);
    }

}
