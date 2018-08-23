package com.luckynick.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

public class ModelIO <T extends Serializable>  {

    private File file;
    private FileWriter fileWriter;
    private FileReader fileReader;
    private Gson gsonIO = new GsonBuilder().setPrettyPrinting().create();
    private Class<T> classOfModel;

    public ModelIO(String path, Class<T> classOfModel) {
        File serializationFile = new File(path);
        if(!serializationFile.exists()) {
            File folder = serializationFile.getParentFile();
            folder.mkdirs();
            serializationFile.delete();
        }
        file = serializationFile;
        this.classOfModel = classOfModel;
    }


    public void serialize(T object) throws IOException {
        file.createNewFile();
        if(file == null) throw new IllegalStateException("Model file doesn't exist.");
        fileWriter = new FileWriter(file, false);
        gsonIO.toJson(object, fileWriter);
        close(fileWriter);
    }

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
            c.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        c = null;
    }
}
