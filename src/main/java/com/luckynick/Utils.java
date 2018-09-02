package com.luckynick;

import java.io.File;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utils {

    public static final boolean DEBUG_MODE = false;
    public static final String JSON_EXTENSION = ".json";
    public static final String PATH_SEPARATOR = File.separator;
    public static final String LOG_TAG = "[ LOGGER ] ";

    public static void Log(String consoleLog) {
        if(DEBUG_MODE) System.out.println(LOG_TAG + consoleLog);
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

    public enum DataStorage {
        ROOT(Utils.formPathString("data")),

        NONE(Utils.formPathString(DataStorage.ROOT.toString(), "devnull")),
        CONFIG(Utils.formPathString(DataStorage.ROOT.toString(), "config")),
        MODELS(Utils.formPathString(DataStorage.ROOT.toString(), "models")),

        DEVICES(Utils.formPathString(DataStorage.MODELS.toString(), "devices")),

        PROFILES(Utils.formPathString(DataStorage.MODELS.toString(), "profiles")),
        SINGLE(Utils.formPathString(DataStorage.PROFILES.toString(), "single")),
        SEQUENTIAL(Utils.formPathString(DataStorage.PROFILES.toString(), "sequential")),
        SCENARIO(Utils.formPathString(DataStorage.PROFILES.toString(), "scenario")),

        RESULTS(Utils.formPathString(DataStorage.MODELS.toString(), "results")),
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
            return Utils.formPathString(this.toString(), fileName);
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

    public static boolean isReflectedAsNumber(Class<?> type) {
        return Number.class.isAssignableFrom(type) || NUMBER_REFLECTED_PRIMITIVES.contains(type);
    }

    public static String getDateStringForFileName() {
        return new SimpleDateFormat("ddMMyy_HHmmss").format(new Date());
    }

    public static <X> X[] toArray(Class<X> entryClass, List<X> listToConvert) {
        X[] result = (X[]) Array.newInstance(entryClass, listToConvert.size());
        for(int i = 0; i < result.length; i++) {
            result[i] = listToConvert.get(i);
        }
        return result;
    }
}
