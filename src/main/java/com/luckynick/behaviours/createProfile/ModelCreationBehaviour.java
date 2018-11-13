package com.luckynick.behaviours.createProfile;

import com.luckynick.models.ModelIO;
import com.luckynick.behaviours.ProgramBehaviour;
import com.luckynick.models.ModelEditor;
import com.luckynick.models.SerializableModel;
import com.luckynick.models.profiles.Profile;

import java.io.IOException;

public abstract class ModelCreationBehaviour<T extends SerializableModel> extends ProgramBehaviour {

    private String profile_path;

    T profileToManipulate;
    Class<T> classOfModel;


    public ModelCreationBehaviour(Class<T> classOfModel) {
        try {
            this.classOfModel = classOfModel;
            profileToManipulate = classOfModel.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void performProgramTasks() {
        try {
            ModelIO<T> modelIO = new ModelIO<T>(classOfModel);
            T editedProfile = ModelEditor.requireEditedModel(profileToManipulate, modelIO);
            if(editedProfile != null) modelIO.serialize(editedProfile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
