package com.example.administrator.streamingdemo.model.utils;

/**
 * Created by linhtruong on 5/19/2016.
 */
public class Utils {
    public static boolean isStringHasText(final String string) {
        return string != null && !string.isEmpty() && !string.equals("null");
    }
}
