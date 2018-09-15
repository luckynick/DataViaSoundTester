package com.luckynick.custom;

import com.luckynick.shared.SharedUtils;

import java.io.File;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utils extends SharedUtils {

    public static void Log(String tag, String consoleLog) {
        SharedUtils.Log(tag, consoleLog);
    }

    public static <X> X[] toArray(Class<X> entryClass, List<X> listToConvert) {
        X[] result = (X[]) Array.newInstance(entryClass, listToConvert.size());
        for(int i = 0; i < result.length; i++) {
            result[i] = listToConvert.get(i);
        }
        return result;
    }
}
