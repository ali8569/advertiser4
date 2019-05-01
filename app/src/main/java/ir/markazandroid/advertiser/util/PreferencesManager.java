package ir.markazandroid.advertiser.util;

import android.content.SharedPreferences;

/**
 * Coded by Ali on 09/01/2018.
 */

public class PreferencesManager {


    private SharedPreferences sharedPreferences;
    private static final String GOLD_LIST = "ir.markazandroid.advertiser.util.GOLD_LIST";

    public PreferencesManager(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void saveGoldList(String toSave) {
        sharedPreferences.edit().putString(GOLD_LIST, toSave).apply();
    }

    public String getGoldList() {
        return sharedPreferences.getString(GOLD_LIST, null);
    }
}
