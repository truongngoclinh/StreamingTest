package com.example.administrator.streamingdemo.model.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by linhtruong on 5/20/2016.
 */
public class PrefsManager {

    public static void setPrefValue(Context context, String key, boolean value) {
        SharedPreferences sp = context.getSharedPreferences(Constants.PREFS_KEY, Context.MODE_PRIVATE);
        sp.edit().putBoolean(key, value).commit();
    }

    public static boolean getPrefValue(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(Constants.PREFS_KEY, Context.MODE_PRIVATE);
        return sp.getBoolean(key, false);
    }
}
