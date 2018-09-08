package com.luckynick.shared;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class SharedUtils {

    public static final String SSID = "Heh_mobile", PASSWORD = "123456798";
    public static final int COMMUNICATION_PORT = 8080;
    public static final String WIFI_SUBNET = "192.168";

    public static final String JSON_EXTENSION = ".json";
    public static final String PATH_SEPARATOR = File.separator;



    public enum DataStorage {
        ROOT(formPathString("data")),

        NONE(formPathString(DataStorage.ROOT.toString(), "devnull")),
        CONFIG(formPathString(DataStorage.ROOT.toString(), "config")),
        MODELS(formPathString(DataStorage.ROOT.toString(), "models")),

        DEVICES(formPathString(DataStorage.MODELS.toString(), "devices")),

        PROFILES(formPathString(DataStorage.MODELS.toString(), "profiles")),
        SINGLE(formPathString(DataStorage.PROFILES.toString(), "single")),
        SEQUENTIAL(formPathString(DataStorage.PROFILES.toString(), "sequential")),
        SCENARIO(formPathString(DataStorage.PROFILES.toString(), "scenario")),

        RESULTS(formPathString(DataStorage.MODELS.toString(), "results")),
        ;

        private String path;

        DataStorage(String path) {
            this.path = path;
        }

        @Override
        public String toString() {
            return path;
        }

        public String getFullPath(String fileName) {
            return formPathString(this.toString(), fileName);
        }
    }

    public static String formPathString(String ... elems) {
        if(elems.length == 0) return PATH_SEPARATOR;
        String result = elems[0] + (!elems[0].endsWith(PATH_SEPARATOR) ? PATH_SEPARATOR : "");
        if(elems.length == 1) return result;
        for(int i = 1; i < elems.length; i++) {
            result += elems[i] + PATH_SEPARATOR;
        }
        return result;
    }

    public static String getDateStringForFileName() {
        return new SimpleDateFormat("ddMMyy_HHmmss").format(new Date());
    }




    private final static Set<Class<?>> NUMBER_REFLECTED_PRIMITIVES;
    static {
        Set<Class<?>> s = new HashSet<>();
        s.add(byte.class);
        s.add(short.class);
        s.add(int.class);
        s.add(long.class);
        s.add(float.class);
        s.add(double.class);
        NUMBER_REFLECTED_PRIMITIVES = s;
    }

    public static boolean isReflectedAsNumber(Class<?> type) {
        return Number.class.isAssignableFrom(type) || NUMBER_REFLECTED_PRIMITIVES.contains(type);
    }
}