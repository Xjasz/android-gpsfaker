package com.delvedapps.core.storage.sharedpref;

import android.content.Context;
import android.content.SharedPreferences;

public class SavedPrefs {
    public static final String PREFS_FILE = "PREFS_FILE";
    public static final String CURRENTMAPTYPE = "CURRENTMAPTYPE";
    public static final String LOCATION_LAT = "LOCATION_LAT";
    public static final String LOCATION_LNG = "LOCATION_LNG";
    public static final String CONTROLLER = "CONTROLLER";

    public static final String latest_version = "latest_version";
    public static final String required = "required";
    public static final String title = "title";
    public static final String message1 = "message1";
    public static final String message2 = "message2";
    public static final String url = "url";

    public static final String path = "path";
    public static final String string1 = "string1";
    public static final String string2 = "string2";
    public static final String string3_part1 = "string3_part1";
    public static final String string3_part2 = "string3_part2";
    public static final String string4 = "string4";
    public static final String string5 = "string5";


    public static final int TYPE_STRING = 1;


    public static void setPref(Context context, String prefName, Object prefValue, int type) {
        SharedPreferences.Editor edit = context.getSharedPreferences(PREFS_FILE, 0).edit();
        if (prefValue == null) {
            if (type == TYPE_STRING)
                edit.putString(prefName, null);
        } else {
            if (type == TYPE_STRING)
                edit.putString(prefName, String.valueOf(prefValue));
        }
        edit.commit();
    }

    public static Object getPref(Context context, String prefName, String prefDefault, int type) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, 0);
        if (type == TYPE_STRING)
            return prefs.getString(prefName, prefDefault);
        else return null;
    }
}
