package com.delvedapps.core.parser;

import android.util.Log;

import com.delvedapps.core.Singleton;
import com.delvedapps.core.reflection.listeners.Listener;
import com.delvedapps.core.reflection.messages.NetworkMessage;
import com.delvedapps.core.storage.sharedpref.SavedPrefs;
import com.delvedapps.core.util.contstant.Constants;
import com.delvedapps.core.webservice.WebService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;


public class WebDataParser {
    private static final String TAG = WebDataParser.class.getSimpleName();
    private static final int JSON_ERROR = -1;
    private static final int JSON_OBJECT = 0;
    private static final int JSON_ARRAY = 1;


    public void parseRawWebObject(String data, int type) throws JSONException {
        int jsonType = getJsonType(data);
        if (jsonType == JSON_ERROR) {
            throw new JSONException("JSON_ERROR");
        } else if (jsonType == JSON_OBJECT) {
            parseJsonAppDataObject(new JSONObject(data));
        } else if (jsonType == JSON_ARRAY) {
            JSONArray array = new JSONArray(data);
            for (int i = 0; i < array.length(); i++) {
                if (type == WebService.GET_APP_DATA) {
                    parseJsonAppDataObject(array.getJSONObject(i));
                } else if (type == WebService.GET_APP_COMMANDS) {
                    parseJsonAppDataObject(array.getJSONObject(i));
                }
            }
        }
    }

    public int getJsonType(String data) {
        try {
            new JSONObject(data);
            return JSON_OBJECT;
        } catch (JSONException e) {
            try {
                new JSONArray(data);
                return JSON_ARRAY;
            } catch (JSONException ignored) {
                return JSON_ERROR;
            }
        }
    }

    private void parseJsonAppDataObject(JSONObject jObject) throws JSONException, OutOfMemoryError {
        Log.d(TAG, "parseJsonAppDataObject");
        Iterator<?> keys = jObject.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (jObject.get(key) instanceof JSONObject) {
                parseJsonAppDataObject(jObject.getJSONObject(key));
            } else if (jObject.get(key) instanceof String) {
                Log.d(TAG, "String: " + jObject.getString(key));
                String value = jObject.getString(key);
                if (key.contentEquals("VERSION")) {
                    Log.d(TAG, "local: " + Singleton.APP_VERSION);
                    Log.d(TAG, "remote: " + jObject.getString("latest_version"));
                    int local = Integer.parseInt(Singleton.APP_VERSION);
                    int remote = Integer.parseInt(jObject.getString("latest_version"));
                    if (local < remote) {
                        if (jObject.getString("required").contentEquals("true")) {
                            SavedPrefs.setPref(Singleton.getAppContext(), SavedPrefs.latest_version, jObject.getString("latest_version"), SavedPrefs.TYPE_STRING);
                            SavedPrefs.setPref(Singleton.getAppContext(), SavedPrefs.required, jObject.getString("required"), SavedPrefs.TYPE_STRING);
                            SavedPrefs.setPref(Singleton.getAppContext(), SavedPrefs.title, jObject.getString("title"), SavedPrefs.TYPE_STRING);
                            SavedPrefs.setPref(Singleton.getAppContext(), SavedPrefs.message1, jObject.getString("message1"), SavedPrefs.TYPE_STRING);
                            SavedPrefs.setPref(Singleton.getAppContext(), SavedPrefs.message2, jObject.getString("message2"), SavedPrefs.TYPE_STRING);
                            SavedPrefs.setPref(Singleton.getAppContext(), SavedPrefs.url, jObject.getString("url"), SavedPrefs.TYPE_STRING);
                            Listener.send(new NetworkMessage<>(WebService.APP_MUST_UPDATE, Constants.VALID_WEB_EVENT));
                        }
                    }
                } else if (key.contentEquals("POST_JELLYBEAN")) {
                    if (Singleton.AFTER_JELLYBEAN) {
                        SavedPrefs.setPref(Singleton.getAppContext(), SavedPrefs.path, jObject.getString("path"), SavedPrefs.TYPE_STRING);
                        SavedPrefs.setPref(Singleton.getAppContext(), SavedPrefs.string1, jObject.getString("string1"), SavedPrefs.TYPE_STRING);
                        SavedPrefs.setPref(Singleton.getAppContext(), SavedPrefs.string2, jObject.getString("string2"), SavedPrefs.TYPE_STRING);
                        SavedPrefs.setPref(Singleton.getAppContext(), SavedPrefs.string3_part1, jObject.getString("string3_part1"), SavedPrefs.TYPE_STRING);
                        SavedPrefs.setPref(Singleton.getAppContext(), SavedPrefs.string3_part2, jObject.getString("string3_part2"), SavedPrefs.TYPE_STRING);
                        SavedPrefs.setPref(Singleton.getAppContext(), SavedPrefs.string4, jObject.getString("string4"), SavedPrefs.TYPE_STRING);
                        SavedPrefs.setPref(Singleton.getAppContext(), SavedPrefs.string5, jObject.getString("string5"), SavedPrefs.TYPE_STRING);
                    }
                } else if (key.contentEquals("PRE_JELLYBEAN")) {
                    if (!Singleton.AFTER_JELLYBEAN) {
                        SavedPrefs.setPref(Singleton.getAppContext(), SavedPrefs.path, jObject.getString("path"), SavedPrefs.TYPE_STRING);
                        SavedPrefs.setPref(Singleton.getAppContext(), SavedPrefs.string1, jObject.getString("string1"), SavedPrefs.TYPE_STRING);
                        SavedPrefs.setPref(Singleton.getAppContext(), SavedPrefs.string2, jObject.getString("string2"), SavedPrefs.TYPE_STRING);
                        SavedPrefs.setPref(Singleton.getAppContext(), SavedPrefs.string3_part1, jObject.getString("string3_part1"), SavedPrefs.TYPE_STRING);
                        SavedPrefs.setPref(Singleton.getAppContext(), SavedPrefs.string3_part2, jObject.getString("string3_part2"), SavedPrefs.TYPE_STRING);
                        SavedPrefs.setPref(Singleton.getAppContext(), SavedPrefs.string4, jObject.getString("string4"), SavedPrefs.TYPE_STRING);
                        SavedPrefs.setPref(Singleton.getAppContext(), SavedPrefs.string5, jObject.getString("string5"), SavedPrefs.TYPE_STRING);
                    }
                } else if (key.contentEquals("MESSAGE1")) {
                    String message = jObject.getString(key);
                    if (message.length() > 0) {
                        Singleton.toastMessage(message);
                    }
                } else if (key.contentEquals("MESSAGE2")) {
                    final String message = jObject.getString(key);
                    if (message.length() > 0) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Singleton.toastMessage(message);
                            }
                        }).start();
                    }
                }
            }
        }
    }
}
