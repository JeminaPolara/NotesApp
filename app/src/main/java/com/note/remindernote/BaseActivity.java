
package com.note.remindernote;

import static com.note.remindernote.utils.ConstantsBase.PREF_NAVIGATION;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.pixplicity.easyprefs.library.Prefs;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    protected static final int TRANSITION_VERTICAL = 0;
    protected static final int TRANSITION_HORIZONTAL = 1;

    protected String navigation;
    protected String navigationTmp; // used for widget navigation


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
         super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onResume() {
        super.onResume();
        String navNotes = getResources().getStringArray(R.array.navigation_list_codes)[0];
        navigation = Prefs.getString(PREF_NAVIGATION, navNotes);
    }


    protected void showToast(CharSequence text, int duration) {
        if (Prefs.getBoolean("settings_enable_info", true)) {
            Toast.makeText(getApplicationContext(), text, duration).show();
        }
    }

    protected void setActionBarTitle(String title) {
        // Creating a spannable to support custom fonts on ActionBar
        int actionBarTitle = Resources.getSystem().getIdentifier("action_bar_title", "ID", "android");
        android.widget.TextView actionBarTitleView = getWindow().findViewById(actionBarTitle);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        if (actionBarTitleView != null) {
            actionBarTitleView.setTypeface(font);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }


    public String getNavigationTmp() {
        return navigationTmp;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_MENU || super.onKeyDown(keyCode, event);
    }
}
