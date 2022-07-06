package com.bkprojects.soundsleep;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;


import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Entities DAO used to persist all values as KV pairs in SharedPreference
 */
public class EntitiesDAO {

    private static final String PREF_FILE_NAME = "shared_pref";
    private final String START_TIME_KEY= "start_time";
    private final String END_TIME_KEY= "end_time";
    private final String NOTIFICATION_KEY = "notification";
    private final String MODE_KEY = "mode";
    private SharedPreferences sharedPreferences;

    public EntitiesDAO(Context context) throws GeneralSecurityException, IOException {
        MasterKey mkey = new MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();
        sharedPreferences =
                EncryptedSharedPreferences.create(
                        context,
                        PREF_FILE_NAME,
                        mkey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );
    }

    public void setSharedPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    /**
     * Used to save all KV pair as preferences from entities into shared preference
     * @param entities The Entities object
     */
    public void savePreferences(Entities entities) {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(START_TIME_KEY, entities.getStartTime());
        editor.putString(END_TIME_KEY, entities.getEndTime());
        editor.putString(MODE_KEY, entities.getMode());
        editor.putBoolean(NOTIFICATION_KEY, entities.isNotifications());
        editor.apply();
    }

    /**
     * Used for getting all the KV pairs persisted as preference
     * @param context The context of the activity
     * @return Entities object
     */
    public Entities getPreferences(Context context) {
        Entities entities = new Entities();
        entities.setStartTime(sharedPreferences.getString(START_TIME_KEY, context.getString(R.string.start_time_default)));
        entities.setEndTime(sharedPreferences.getString(END_TIME_KEY, context.getString(R.string.end_time_default)));
        entities.setMode(sharedPreferences.getString(MODE_KEY, context.getString(R.string.vibrate_mode)));
        entities.setNotifications(sharedPreferences.getBoolean(NOTIFICATION_KEY, false));
        return entities;
    }

    /**
     * Remove all the shared Preferences
     */
    public void removeAllPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(START_TIME_KEY);
        editor.remove(END_TIME_KEY);
        editor.remove(MODE_KEY);
        editor.remove(NOTIFICATION_KEY);
    }
}
