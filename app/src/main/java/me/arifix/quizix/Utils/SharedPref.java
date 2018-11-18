package me.arifix.quizix.Utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Arif Khan on 01/1/2018.
 */

public class SharedPref {
    private static SharedPref myPreference;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    SharedPref(Context context) {
        sharedPreferences = context.getSharedPreferences(Config.PROJECT_CODENAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static SharedPref getPreferences(Context context) {
        if (myPreference == null)
            myPreference = new SharedPref(context);

        return myPreference;
    }

    // Put String Value
    public void setStringData(String Key, String Value) {
        editor.putString(Key, Value);
        editor.apply();
        editor.commit();
    }

    // Put Integer Value
    public void setIntData(String Key, int Value) {
        editor.putInt(Key, Value);
        editor.apply();
        editor.commit();
    }

    // Put Float Value
    public void setFloatData(String Key, float Value) {
        editor.putFloat(Key, Value);
        editor.apply();
        editor.commit();
    }

    // Put Boolean Value
    public void setBoolData(String Key, boolean Value) {
        editor.putBoolean(Key, Value);
        editor.apply();
        editor.commit();
    }

    // Put Long Value
    public void setLongData(String Key, long Value) {
        editor.putLong(Key, Value);
        editor.apply();
        editor.commit();
    }

    // Get String Value
    public String getStringData(String Key, String Fallback) {
        return sharedPreferences.getString(Key, Fallback);
    }

    // Get Integer Value
    public int getIntData(String Key, int Fallback) {
        return sharedPreferences.getInt(Key, Fallback);
    }

    // Get Float Value
    public float getData(String Key, float Fallback) {
        return sharedPreferences.getFloat(Key, Fallback);
    }

    // Get Boolean Value
    public boolean getBoolData(String Key, boolean Fallback) {
        return sharedPreferences.getBoolean(Key, Fallback);
    }

    // Get Long Value
    public long getLongData(String Key, long Fallback) {
        return sharedPreferences.getLong(Key, Fallback);
    }

    // Clear All Existing Room
    public void setEmpty() {
        editor.clear();
        editor.commit();
    }
}
