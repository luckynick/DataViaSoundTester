package com.luckynick;

import com.luckynick.shared.SharedUtils;

import java.io.File;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utils {

    public static final boolean DEBUG_MODE = true;
    public static final String LOG_TAG = "[ LOGGER ] ";

    public static void Log(String consoleLog) {
        if(DEBUG_MODE) System.out.println(LOG_TAG + consoleLog);
    }

    public static <X> X[] toArray(Class<X> entryClass, List<X> listToConvert) {
        X[] result = (X[]) Array.newInstance(entryClass, listToConvert.size());
        for(int i = 0; i < result.length; i++) {
            result[i] = listToConvert.get(i);
        }
        return result;
    }
}
