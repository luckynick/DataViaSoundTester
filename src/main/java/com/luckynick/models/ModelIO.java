package com.luckynick.models;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.luckynick.shared.GSONCustomSerializer;
import com.luckynick.shared.IOFieldHandling;

import java.io.*;

public class ModelIO <T extends SerializableModel> extends GSONCustomSerializer<T> {

    private File file;

    public ModelIO(Class<T> classOfModel) {
        super(classOfModel);
        SerializableModel getPathFromIt = null;
        try {
            getPathFromIt = classOfModel.newInstance();
        }
        catch (InstantiationException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        File serializationFile = new File(getPathFromIt.wholePath);
        if(!serializationFile.exists()) {
            File folder = serializationFile.getParentFile();
            if(folder != null) folder.mkdirs();
            serializationFile.delete();
        }
        file = serializationFile;
    }

    public ModelIO(Class<T> classOfModel, File existingFile) {
        super(classOfModel);
        file = existingFile;
        if(!existingFile.exists()) {
            File folder = existingFile.getParentFile();
            if(folder != null) folder.mkdirs();
            existingFile.delete();
        }
    }


    public void serialize(T object) throws IOException {
        file.createNewFile();
        if(file == null) throw new IllegalStateException("Model file doesn't exist.");
        FileWriter fileWriter = new FileWriter(file, false);
        serialize(fileWriter, object);
        close(fileWriter);
    }

    //TODO: recursively read
    public T deserialize() throws IOException {
        if(file == null || !file.exists()) throw new IllegalStateException("Model file doesn't exist.");
        FileReader fileReader = new FileReader(file);
        T result = deserialize(fileReader);
        close(fileReader);
        return result;
    }

    private void close(Closeable c) {
        try {
            if(c != null) c.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        c = null;
    }

    public boolean exists() {
        return this.file.exists();
    }
}
