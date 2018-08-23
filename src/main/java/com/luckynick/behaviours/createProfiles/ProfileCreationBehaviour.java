package com.luckynick.behaviours.createProfiles;

import com.luckynick.models.profiles.ProfileIO;
import com.luckynick.behaviours.ProgramBehaviour;
import com.luckynick.models.ModelManager;
import com.luckynick.models.profiles.Profile;

import java.io.IOException;

public abstract class ProfileCreationBehaviour<T extends Profile> extends ProgramBehaviour {

    private String profile_path;

    T profileToManipulate;

    private ProfileIO<T> profileIO;

    public ProfileCreationBehaviour(Class<T> classOfModel) {
        try {
            profileToManipulate = classOfModel.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        profileIO = new ProfileIO<T>(profileToManipulate.wholePath, classOfModel);
    }

    @Override
    public void performProgramTasks() {
        try {
            T editedProfile = ModelManager.<T>requireEditedModel(profileToManipulate);
            if(editedProfile != null) profileIO.serialize(editedProfile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
