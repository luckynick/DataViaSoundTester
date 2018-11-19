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

    public static final int TEST_FREQ_BINDING_BASE = 0;//0 1500
    public static final double TEST_FREQ_BINDING_SCALE = 1;//1 1.33

    public static <X> X[] toArray(Class<X> entryClass, List<X> listToConvert) {
        X[] result = (X[]) Array.newInstance(entryClass, listToConvert.size());
        for(int i = 0; i < result.length; i++) {
            result[i] = listToConvert.get(i);
        }
        return result;
    }

    public static void main(String args[]) { //15 ierations
        //spec_0_1000();
        spec_0_2000();
    }

    static void spec_0_1000() {
        for(int i = 1; i < 1000; i += 66) {
            System.out.println(i);
        }
    }

    static void spec_0_2000() {
        for(int i = 1; i < 2000; i += 133) {
            System.out.println(i);
        }
    }
}
