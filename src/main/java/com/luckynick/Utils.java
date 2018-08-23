package com.luckynick;

import java.io.File;

public class Utils {
    public static final String JSON_EXTENSION = ".json";
    public static final String PATH_SEPARATOR = File.separator;

    public static String formPathString(String ... elems) {
        if(elems.length == 0) return PATH_SEPARATOR;
        String result = elems[0] + (!elems[0].endsWith(PATH_SEPARATOR) ? PATH_SEPARATOR : "");
        if(elems.length == 1) return result;
        for(int i = 1; i < elems.length; i++) {
            result += elems[i] + PATH_SEPARATOR;
        }
        return result;
    }
}
