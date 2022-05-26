
package com.note.remindernote;

import static com.note.remindernote.utils.ConstantsBase.PREF_FAB_EXPANSION_BEHAVIOR;
import static com.note.remindernote.utils.ConstantsBase.PREF_NAVIGATION;
import static com.note.remindernote.utils.ConstantsBase.PREF_PRETTIFIED_DATES;

import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.note.remindernote.adapters.NoteAdapter;
import com.note.remindernote.databinding.FragmentListBinding;
import com.note.remindernote.helpers.date.DateHelper;
import com.note.remindernote.listeners.RecyclerViewItemClickSupport;
import com.note.remindernote.models.Note;
import com.note.remindernote.models.UndoBarController;
import com.note.remindernote.utils.date.DateUtils;
import com.note.remindernote.views.Fab;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import io.realm.Realm;
import io.realm.Sort;


public class ListFragment extends Fragment /*implements
        UndoBarController.UndoListener*/ {

    public static final String LIST_VIEW_POSITION = "listViewPosition";
    public static final String LIST_VIEW_POSITION_OFFSET = "listViewPositionOffset";

    private FragmentListBinding binding;

    private final List<Note> selectedNotes = new ArrayList<>();
    private int listViewPosition;
    private int listViewPositionOffset = 16;

    private boolean undoArchive = false;
    private boolean undoCategorize = false;
    private final SortedMap<Integer, Note> undoNotesMap = new TreeMap<>();
    private final Map<Note, Boolean> undoArchivedMap = new HashMap<>();


    private NoteAdapter listAdapter;
    //    private UndoBarController ubc;
    private Fab fab;
    private MainActivity mainActivity;

    Realm realm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(LIST_VIEW_POSITION)) {
                listViewPosition = savedInstanceState.getInt(LIST_VIEW_POSITION);
                listViewPositionOffset = savedInstanceState.getInt(LIST_VIEW_POSITION_OFFSET);
            }
        }
        binding = FragmentListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.list.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.list.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                binding.list.getContext(),
                linearLayoutManager.getOrientation());
        dividerItemDecoration
                .setDrawable(getResources().getDrawable(R.drawable.fragment_list_item_divider));
        binding.list.addItemDecoration(dividerItemDecoration);

        binding.list.setEmptyView(binding.emptyList);
        realm = Realm.getDefaultInstance();

        closeFab();
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        if (mainActivity != null && savedInstanceState != null) {
            mainActivity.navigationTmp = savedInstanceState.getString("navigationTmp");
        }
        init();
    }


    private void init() {
        initListView();
//        ubc = new UndoBarController(binding.undobar.getRoot(), this);

        initFab();
        initTitle();
        binding.idIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.finish();
            }
        });
        if (isFabAllowed()) {
            fab.setAllowed(true);
            fab.showFab();
        } else {
            fab.setAllowed(false);
            fab.hideFab();
        }
        binding.snackbarPlaceholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeFab();
            }
        });
        binding.listFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeFab();
            }
        });

    }


    private void initFab() {
        fab = new Fab(binding.fab.getRoot(), binding.list,
                Prefs.getBoolean(PREF_FAB_EXPANSION_BEHAVIOR, false));
        fab.setOnFabItemClickedListener(id -> {
            View v = mainActivity.findViewById(id);
            switch (id) {
                case R.id.fab_checklist:
                    Note note = new Note();
                    note.setChecklist(true);
                    editNote(note, v);
                    break;
                default:
                    editNote(new Note(), v);
            }
        });
    }


    boolean closeFab() {
        if (fab != null && fab.isExpanded()) {
            fab.performToggle();
            return true;
        }
        return false;
    }


    /**
     * Activity title initialization based on navigation
     */
    private void initTitle() {
        String[] navigationList = getResources().getStringArray(R.array.navigation_list);
        String[] navigationListCodes = getResources().getStringArray(R.array.navigation_list_codes);
        String navigation = mainActivity.navigationTmp != null
                ? mainActivity.navigationTmp
                : Prefs.getString(PREF_NAVIGATION, navigationListCodes[0]);
        int index = Arrays.asList(navigationListCodes).indexOf(navigation);
        String title = null;
        // If is a traditional navigation item
        if (index >= 0 && index < navigationListCodes.length) {
            title = navigationList[index];
        }
        title = title == null ? getString(R.string.title_activity_list) : title;
        mainActivity.setActionBarTitle(title);
    }


    @Override
    public void onPause() {
        super.onPause();
        Crouton.cancelAllCroutons();
        closeFab();

    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        refreshListScrollPosition();
        outState.putInt("listViewPosition", listViewPosition);
        outState.putInt(LIST_VIEW_POSITION_OFFSET, listViewPositionOffset);
    }


    private void refreshListScrollPosition() {
        if (binding != null) {
            listViewPosition = ((LinearLayoutManager) binding.list.getLayoutManager())
                    .findFirstVisibleItemPosition();
            View v = binding.list.getChildAt(0);
            listViewPositionOffset =
                    (v == null) ? (int) getResources().getDimension(R.dimen.vertical_margin) : v.getTop();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        init();
    }


    private void initListView() {
        List<Note> notes = realm.where(Note.class).findAll().sort(Utils.NOTEID, Sort.DESCENDING);


        List<Note> notesCompleted = new ArrayList<>();
        ArrayList<String> checkCompletedTime = new ArrayList();


        for (int i = notes.size() - 1; i >= 0; i--) {
            Note note = notes.get(i);
            notesCompleted.add(note);
            Log.e("aaaaaaa", "--->" + note.get_id());
            if (note.getCompletedTime() != 0 && !DateUtils.isSameDay(note.get_id(), note.getCompletedTime())) {
                checkCompletedTime.add(String.valueOf(note.getCompletedTime()));
                Log.e("aaaaaaa", "1111--->" + note.getCompletedTime());
//                Note note2 = realm.where(Note.class).equalTo(Utils.COMPLETED_TIME, note.getCompletedTime()).findFirst();
//                notesCompleted.add(note2);

            }
            for (String str : checkCompletedTime) {
                if (DateUtils.isSameDay(note.get_id(), Long.parseLong(str))) {
                    Note note2 = realm.where(Note.class).equalTo(Utils.COMPLETED_TIME, Long.parseLong(str)).findFirst();
                    notesCompleted.add(note2);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    notesCompleted.stream().map(number -> number.getTitle()+"__").forEach(System.out::println);
                    notesCompleted.stream().sorted(new Comparator<Note>() {
                        @Override
                        public int compare(Note note, Note t1) {
                            return note.get_id().compareTo(t1.get_id());
                        }
                    }).forEach(System.out::println);
                }
            }
        }

        Collections.reverse(notesCompleted);

        listAdapter = new NoteAdapter(mainActivity, notesCompleted);

        binding.list.setAdapter(listAdapter);
        RecyclerViewItemClickSupport.addTo(binding.list)
                .setOnItemClickListener((recyclerView, position, view) -> {
                    editNote(listAdapter.getItem(position), view);
                }).setOnItemLongClickListener((recyclerView, position, view) -> {
            deleteNote(listAdapter.getItem(position), view, position);

            return true;
        });

    }

    private void deleteNote(Note note, View view, int position) {
        new MaterialDialog.Builder(mainActivity)
                .content(R.string.delete_note_confirmation)
                .positiveText(R.string.ok)
                .onPositive((dialog, which) -> {
                    mainActivity.deleteNote(note);
                    onResume();
                }).build().show();
    }


    void editNote(final Note note, final View view) {

        editNote2(note);

    }


    void editNote2(Note note) {

        // Current list scrolling position is saved to be restored later
        refreshListScrollPosition();

        // Fragments replacing
        mainActivity.switchToDetail(note);



    }


    private List<Note> getSelectedNotes() {
        return selectedNotes;
    }
/*


    @Override
    public void onUndo(Parcelable undoToken) {
        for (Integer notePosition : undoNotesMap.keySet()) {
            Note currentNote = undoNotesMap.get(notePosition);
            if ((undoCategorize)
                    || undoArchive) {
                if (undoCategorize) {
                } else if (undoArchive) {
                    currentNote.setArchived(undoArchivedMap.get(currentNote));
                }
                listAdapter.replace(currentNote, listAdapter.getPosition(currentNote));
            } else {
                listAdapter.add(notePosition, currentNote);
            }
        }

        listAdapter.notifyDataSetChanged();

        selectedNotes.clear();
        undoNotesMap.clear();

        undoArchive = false;
        undoCategorize = false;
        undoNotesMap.clear();
        undoArchivedMap.clear();

        Crouton.cancelAllCroutons();

        ubc.hideUndoBar(false);
        fab.showFab();
    }
*/


    private boolean isFabAllowed() {
        return false;
    }


}
