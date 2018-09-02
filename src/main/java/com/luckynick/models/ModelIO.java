package com.luckynick.models;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.luckynick.Utils;
import org.json.simple.parser.JSONParser;

import java.io.*;

public class ModelIO <T extends SerializableModel>  {

    private File file;
    private FileWriter fileWriter;
    private FileReader fileReader;
    protected Class<T> classOfModel;

    ExclusionStrategy ioExclusionStrategy = new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            IOFieldHandling a = f.getAnnotation(IOFieldHandling.class);
            return a != null ? !a.serialize() : false;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    };
    private Gson gsonIO = new GsonBuilder().setPrettyPrinting().serializeNulls()
            .setExclusionStrategies(ioExclusionStrategy).create();


    public ModelIO(Class<T> classOfModel) {
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
        this.classOfModel = classOfModel;
    }


    //TODO: recursively write
    public void serialize(T object) throws IOException {
        file.createNewFile();
        if(file == null) throw new IllegalStateException("Model file doesn't exist.");
        fileWriter = new FileWriter(file, false);
        gsonIO.toJson(object, fileWriter);
        close(fileWriter);
    }

    //TODO: recursively read
    public T deserialize() throws IOException {
        if(file == null) throw new IllegalStateException("Model file doesn't exist.");
        fileReader = new FileReader(file);
        T result = gsonIO.fromJson(fileReader, classOfModel);
        close(fileWriter);
        return result;
    }

    public boolean exists() {
        return this.file.exists();
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
}
