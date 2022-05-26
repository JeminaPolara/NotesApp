

package com.note.remindernote;

import static com.note.remindernote.utils.ConstantsBase.ACTION_START_APP;
import static com.note.remindernote.utils.ConstantsBase.INTENT_NOTE;
import static com.note.remindernote.utils.ConstantsBase.PREF_TOUR_COMPLETE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.note.pixlui.links.UrlCompleter;
import com.note.remindernote.databinding.ActivityMainBinding;
import com.note.remindernote.models.Note;
import com.note.remindernote.models.Task;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import io.realm.Realm;


public class MainActivity extends BaseActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    public final static String FRAGMENT_LIST_TAG = "fragment_list";
    public final static String FRAGMENT_DETAIL_TAG = "fragment_detail";


    boolean prefsChanged = false;
    private FragmentManager mFragmentManager;

    ActivityMainBinding binding;
    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.OmniNotesTheme_ApiSpec);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Prefs.getPreferences().registerOnSharedPreferenceChangeListener(this);

        Prefs.edit().putBoolean(PREF_TOUR_COMPLETE, true).apply();
        realm = Realm.getDefaultInstance();

    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }


    private void init() {

        getFragmentManagerInstance();

        if (getFragmentManagerInstance().findFragmentByTag(FRAGMENT_LIST_TAG) == null) {
            FragmentTransaction fragmentTransaction = getFragmentManagerInstance().beginTransaction();
            fragmentTransaction.add(R.id.fragment_container, new ListFragment(), FRAGMENT_LIST_TAG)
                    .commit();
        }

        handleIntents();
    }

    private FragmentManager getFragmentManagerInstance() {
        if (mFragmentManager == null) {
            mFragmentManager = getSupportFragmentManager();
        }
        return mFragmentManager;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getAction() == null) {
            intent.setAction(ACTION_START_APP);
        }
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntents();
    }

    /**
     * Checks if allocated fragment is of the required type and then returns it or returns null
     */
    private Fragment checkFragmentInstance(int id, Object instanceClass) {
        Fragment result = null;
        Fragment fragment = getFragmentManagerInstance().findFragmentById(id);
        if (fragment != null && instanceClass.equals(fragment.getClass())) {
            result = fragment;
        }
        return result;
    }

    @Override
    public void onBackPressed() {

        Fragment f = checkFragmentInstance(R.id.fragment_container, DetailFragment.class);
        if (f != null) {
            ((DetailFragment) f).goBack = true;
            ((DetailFragment) f).saveAndExit();
            return;
        }

        // ListFragment
        f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
        if (f != null) {
            // Before exiting from app the navigation drawer is opened

            if (!((ListFragment) f).closeFab()) {
                super.onBackPressed();
            }

            return;
        }
        super.onBackPressed();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("navigationTmp", navigationTmp);
    }


    @Override
    protected void onPause() {
        super.onPause();
        Crouton.cancelAllCroutons();
    }

    private void handleIntents() {
        Intent i = getIntent();

        if (i.getAction() == null) {
            return;
        }

        // Tag search
        if (Intent.ACTION_VIEW.equals(i.getAction()) && i.getDataString()
                .startsWith(UrlCompleter.HASHTAG_SCHEME)) {
            switchToList();
        }


    }


    public void switchToList() {
        FragmentTransaction transaction = getFragmentManagerInstance().beginTransaction();
        ListFragment mListFragment = new ListFragment();
        transaction.replace(R.id.fragment_container, mListFragment, FRAGMENT_LIST_TAG).addToBackStack
                (FRAGMENT_DETAIL_TAG).commitAllowingStateLoss();

        getFragmentManagerInstance().getFragments();
    }


    public void switchToDetail(Note note) {
        FragmentTransaction transaction = getFragmentManagerInstance().beginTransaction();
        DetailFragment mDetailFragment = new DetailFragment();
        Bundle b = new Bundle();
        b.putParcelable(INTENT_NOTE, (Parcelable) note);
        mDetailFragment.setArguments(b);
        if (getFragmentManagerInstance().findFragmentByTag(FRAGMENT_DETAIL_TAG) == null) {
            transaction.replace(R.id.fragment_container, mDetailFragment, FRAGMENT_DETAIL_TAG)
                    .addToBackStack(FRAGMENT_LIST_TAG)
                    .commitAllowingStateLoss();
        } else {
            getFragmentManagerInstance().popBackStackImmediate();
            transaction.replace(R.id.fragment_container, mDetailFragment, FRAGMENT_DETAIL_TAG)
                    .addToBackStack(FRAGMENT_DETAIL_TAG)
                    .commitAllowingStateLoss();
        }
    }


    /**
     * Single note permanent deletion
     *
     * @param note Note to be deleted
     */
    public void deleteNote(Note note) {
        realm.beginTransaction();

        List<Task> taskList = realm.where(Task.class).equalTo(Utils.NOTEID, note.get_id()).findAll();
        for (Task task : taskList) {
            task.deleteFromRealm();
        }
        note.deleteFromRealm();
        realm.commitTransaction();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        prefsChanged = true;
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}
