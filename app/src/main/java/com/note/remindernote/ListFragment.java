
package com.note.remindernote;

import static com.note.remindernote.utils.ConstantsBase.PREF_FAB_EXPANSION_BEHAVIOR;
import static com.note.remindernote.utils.ConstantsBase.PREF_NAVIGATION;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.note.remindernote.adapters.DateNoteAdapter;
import com.note.remindernote.adapters.NoteAdapter;
import com.note.remindernote.databinding.FragmentListBinding;
import com.note.remindernote.models.Note;
import com.note.remindernote.models.NoteInfo;
import com.note.remindernote.utils.RecyclerItemClickListener;
import com.note.remindernote.utils.date.DateUtils;
import com.note.remindernote.views.Fab;
import com.pixplicity.easyprefs.library.Prefs;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

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


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initListView() {
        List<Note> notes = realm.where(Note.class).findAll().sort(Utils.NOTEID, Sort.ASCENDING);

//        List<NoteInfo> noteInfoList = notes.parallelStream().map(note -> NoteInfo.from(realm, note)).collect(Collectors.toList());


//        notes.stream().

        HashMap<String, List<Note>> noteHashMap = new HashMap<>();
        List<Note> notesCompleted = new ArrayList<>();
        List<Note> notesCompletedTemp = new ArrayList<>();
        ArrayList<String> checkCompletedTime = new ArrayList();
        ArrayList<String> todayPendingTaskDate = new ArrayList();
        /**
         * Add Current date remaining or expire task
         * */
        long maxlong = 0;
        for (int k = 0; k < notes.size(); k++) {
            Note note = notes.get(k);
            if (note.get_id() > maxlong) {
                maxlong = note.get_id();
            }
        }
        if (!DateUtils.isSameDay(System.currentTimeMillis(), maxlong)) {
            for (Note note : notes) {
                if (note.getCompletedTime() == 0 && !DateUtils.isSameDay(System.currentTimeMillis(), note.get_id())) {
                    if (notesCompletedTemp.stream().filter(note1 -> note1.get_id().equals(note.get_id())).count() < 2) {
                        notesCompletedTemp.add(note);
                    }
                }
            }
            noteHashMap.put(String.valueOf(System.currentTimeMillis()), notesCompletedTemp);
        }
        /**
         * Add Next date task list done or created.
         * */
        for (Note note : notes) {
            notesCompletedTemp = new ArrayList<>();
            notesCompleted.add(note);
            notesCompletedTemp.add(note);
            if (note.getCompletedTime() != 0 && !DateUtils.isSameDay(note.get_id(), note.getCompletedTime())) {
                checkCompletedTime.add(String.valueOf(note.getCompletedTime()));
            }
            /**
             * For task date is expire but still not done task.
             * */
            for (String str : checkCompletedTime) {
                List<Note> collect = notesCompleted.stream().filter(note1 -> note1.getCompletedTime() == Long.parseLong(str)).collect(Collectors.toList());
                if (collect.size() == 1 && DateUtils.isSameDay(note.get_id(), Long.parseLong(str))) {
                    notesCompleted.add(collect.get(0));
                    notesCompletedTemp.add(collect.get(0));
                }
            }
            /**
             * For today date's all pending task with expire max and today
             * */
            if (note.getCompletedTime() == 0 && !DateUtils.isSameDay(System.currentTimeMillis(), note.get_id())) {
                if (notesCompletedTemp.stream().filter(note1 -> note1.get_id().equals(note.get_id())).count() < 2) {
                    todayPendingTaskDate.add(note.get_id().toString());
                }
            }
            /**
             * For today date's all pending task with expire max and today
             * */
            boolean isAddDone = false;
            Set<Map.Entry<String, List<Note>>> entries = noteHashMap.entrySet();
            for (int k = 0; k < entries.size(); k++) {
                String value = (new ArrayList<String>(noteHashMap.keySet())).get(k);
                if (DateUtils.isSameDay(Long.parseLong(value), note.get_id())) {
                    for (Map.Entry<String, List<Note>> entry : noteHashMap.entrySet()) {
                        if (entry.getKey().equals(value)) {
                            isAddDone = true;
                            break;
                        }
                    }
                }

            }
            /**
             * If not exist in hashmap then add todayPendingTaskDate
             * */
            if (!isAddDone) {
                for (String str : todayPendingTaskDate) {
                    if (DateUtils.isSameDay(note.get_id(), System.currentTimeMillis())) {
                        List<Note> collect = notesCompleted.stream().filter(note1 -> note1.get_id() == Long.parseLong(str)).collect(Collectors.toList());
                        notesCompletedTemp.addAll(collect);
                    }

                }
            }
            noteHashMap.put(note.get_id().toString(), notesCompletedTemp);
        }

//        Collections.reverse(notesCompleted);

        HashMap<String, List<Note>> finalNoteHashMap = new LinkedHashMap<>();
        noteHashMap.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByKey()))
                .forEachOrdered(entry ->
                        finalNoteHashMap.put(entry.getKey(), entry.getValue()));
        listAdapter = new NoteAdapter(mainActivity, finalNoteHashMap);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.list.setAdapter(listAdapter);
        HashMap<String, List<Note>> finalNoteHashMap1 = new LinkedHashMap<>();
        binding.idIvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialogNetWorkError = new Dialog(mainActivity, R.style.CustomDialogTheme);
                dialogNetWorkError.setContentView(R.layout.single_button_dialog);
                RecyclerView recDateSelect = dialogNetWorkError.findViewById(R.id.recDateSelect);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
                recDateSelect.setLayoutManager(linearLayoutManager);
                DateNoteAdapter dateNoteAdapter = new DateNoteAdapter(mainActivity, finalNoteHashMap);
                recDateSelect.setAdapter(dateNoteAdapter);
                dialogNetWorkError.show();
                recDateSelect.addOnItemTouchListener(
                        new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                finalNoteHashMap1.clear();
                                String value = (new ArrayList<String>(finalNoteHashMap.keySet())).get(position);
                                List<Note> values = new ArrayList<>();
                                for (Map.Entry<String, List<Note>> entry : finalNoteHashMap.entrySet()) {
                                    if (DateUtils.isSameDay(Long.parseLong(entry.getKey()), Long.parseLong(value))) {
                                        values = entry.getValue();
                                        finalNoteHashMap1.put(entry.getKey(), values);
                                    }
                                }
                                listAdapter = new NoteAdapter(mainActivity, finalNoteHashMap1);
                                binding.list.setAdapter(listAdapter);
                                dialogNetWorkError.dismiss();
                            }
                        }));
            }
        });
        binding.idIvRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listAdapter = new NoteAdapter(mainActivity, finalNoteHashMap);
                binding.list.setAdapter(listAdapter);
            }
        });
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


    private boolean isFabAllowed() {
        return false;
    }


}
