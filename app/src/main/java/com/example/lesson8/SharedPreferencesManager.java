package com.example.lesson8;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;


public class SharedPreferencesManager {

    //Constants
    private static final String PACKAGE_NAME = "com.example.lesson8";
    static final String KEY_REQUESTING_LOCATION_UPDATES = PACKAGE_NAME + ".requesting_location_updates";
    private static final String PREF_KEY = PACKAGE_NAME + ".appSetting";
    private static final String LOCATIONS_KEY = PACKAGE_NAME + ".locationKey";

    private final SharedPreferences sharedPreferences;
    private static SharedPreferencesManager instance;
    private final Gson gson;
    private final SharedPreferences.Editor editor;


    @SuppressLint("CommitPrefEdits")
    private SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gson = new Gson();
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesManager(context.getApplicationContext());
        }
    }

    public static SharedPreferencesManager getInstance() {
        return instance;
    }

    public void putLocationModels(List<LocationModel> locationModels) {
        String locationModelsJson = gson.toJson(locationModels);
        //Log.i("dev", "putLocationModels() " + locationModelsJson);
        editor.putString(LOCATIONS_KEY, locationModelsJson);
        editor.commit();
    }

    public List<LocationModel> getLocationModels() {
        String locationModelsJson = sharedPreferences.getString(LOCATIONS_KEY, "");
        if (locationModelsJson.isEmpty()) {
            //Log.i("dev", " getLocationModels() newList");
            return new LinkedList<>();
        } else {
            // Log.i("dev", " getLocationModels() LinkedListList");
            Type type = new TypeToken<List<LocationModel>>() {
            }.getType();
            return gson.fromJson(locationModelsJson, type);
        }
    }

    public void clear() {
        editor.clear();
    }

}
