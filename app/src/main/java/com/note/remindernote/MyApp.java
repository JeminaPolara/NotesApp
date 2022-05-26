
package com.note.remindernote;


import android.content.Context;
import android.content.res.Configuration;
import android.os.StrictMode;

import androidx.multidex.MultiDexApplication;

import com.pixplicity.easyprefs.library.Prefs;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmFileException;


public class MyApp extends MultiDexApplication {

    private static Context mContext;
    private static final String DEFAULT_REALM_NAME = "notes.realm";

    public static Context getAppContext() {
        return MyApp.mContext;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        try {
            Realm.init(this);
            RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                    //.encryptionKey(getString(R.string.realm_enc_key).getBytes())
                    .name(DEFAULT_REALM_NAME)
                    .schemaVersion(2)
                    //.migration(new MyRealmMigration())
                    .deleteRealmIfMigrationNeeded()
                    .build();
            Realm.setDefaultConfiguration(realmConfiguration);
        } catch (RealmFileException e) {
        }
        initSharedPreferences();
        enableStrictMode();



    }

    private void initSharedPreferences() {
        new Prefs.Builder()
                .setContext(this)
                .setMode(MODE_PRIVATE)
                .setPrefsName("com.note.remindernote")
                .setUseDefaultSharedPreference(true)
                .build();
    }

    private void enableStrictMode() {
        StrictMode.enableDefaults();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
      }

}
