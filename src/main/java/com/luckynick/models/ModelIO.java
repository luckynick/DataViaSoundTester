package com.luckynick.models;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.luckynick.shared.GSONCustomSerializer;
import com.luckynick.shared.IOClassHandling;
import com.luckynick.shared.IOFieldHandling;
import com.luckynick.shared.SharedUtils;
import com.sun.istack.internal.Nullable;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ModelIO <T extends SerializableModel> extends GSONCustomSerializer<T> {

    private File file;

    public ModelIO(Class<T> classOfModel) {
        super(classOfModel);
        SerializableModel getPathFromIt = null;
        try {
            Constructor<T> constructor;
            constructor = classOfModel.getDeclaredConstructor();
            constructor.setAccessible(true);
            getPathFromIt = constructor.newInstance();

            //getPathFromIt = classOfModel.newInstance();
        }
        catch (InstantiationException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
            /*
            try {
                getPathFromIt = classOfModel.newInstance();
            }
            catch (InstantiationException e1) {
                e1.printStackTrace();
            }
            catch (IllegalAccessException e1) {
                e1.printStackTrace();
            }
            */
        }
        catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        if(getPathFromIt == null) return;
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
        File file = new File(object.wholePath);
        if(!file.exists()) {
            File folder = file.getParentFile();
            if(folder != null) folder.mkdirs();
        }
        file.createNewFile();
        if(file == null) throw new IllegalStateException("Model file doesn't exist.");
        FileWriter fileWriter = new FileWriter(file, false);
        serialize(fileWriter, object);
        close(fileWriter);
    }

    public T deserialize() throws IOException {
        if(file == null || !file.exists()) throw new IllegalStateException("Model file doesn't exist.");
        FileReader fileReader = new FileReader(file);
        T result = deserialize(fileReader);
        close(fileReader);
        return result;
    }

    public T deserialize(File f) throws IOException {
        if(f == null || !f.exists()) throw new IllegalStateException("Model file doesn't exist.");
        FileReader fileReader = new FileReader(f);
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



    public List<File> listFiles() {
        String dirStr = this.classOfModel.getDeclaredAnnotation(IOClassHandling.class).dataStorage().getDirPath();
        ArrayList<File> list = new ArrayList<>();
        if(dirStr == null) return list;
        Path dir = Paths.get(dirStr);
        try (Stream<Path> paths = Files.walk(dir)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter((p) -> p.toString().endsWith(SharedUtils.JSON_EXTENSION))
                    //.filter((p) -> p.getParent().equals(dir))
                    .forEach((p) -> {list.add(p.toFile());});
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Get list of objects which were previously stored in corresponding folder.
     * @return
     */
    public List<T> listObjects() {
        return filesToObjects(listFiles());
    }

    public List<T> filesToObjects(List<File> files) {
        List<T> modelObjects = new ArrayList<>();
        for(File f : files) {
            try {
                modelObjects.add(deserialize(f));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return modelObjects;
    }
}
