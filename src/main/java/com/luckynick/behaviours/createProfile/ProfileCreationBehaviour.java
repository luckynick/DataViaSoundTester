package com.luckynick.behaviours.createProfile;

import com.luckynick.models.ModelIO;
import com.luckynick.behaviours.ProgramBehaviour;
import com.luckynick.models.ModelEditor;
import com.luckynick.models.profiles.Profile;

import java.io.IOException;

public abstract class ProfileCreationBehaviour<T extends Profile> extends ProgramBehaviour {

    private String profile_path;

    T profileToManipulate;
    ModelIO<T> modelIO;

    public ProfileCreationBehaviour(Class<T> classOfModel) {
        try {
            profileToManipulate = classOfModel.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        modelIO = new ModelIO<T>(classOfModel);
    }

    @Override
    public void performProgramTasks() {
        try {

            T editedProfile = ModelEditor.<T>requireEditedModel(profileToManipulate, modelIO);
            if(editedProfile != null) modelIO.serialize(editedProfile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
