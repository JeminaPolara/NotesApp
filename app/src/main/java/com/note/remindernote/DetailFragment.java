
package com.note.remindernote;

import static androidx.core.view.ViewCompat.animate;
import static com.note.checklistview.Settings.CHECKED_ON_BOTTOM;
import static com.note.remindernote.MainActivity.FRAGMENT_DETAIL_TAG;
import static com.note.remindernote.utils.ConstantsBase.DATE_FORMAT_ASSIGN;
import static com.note.remindernote.utils.ConstantsBase.INTENT_NOTE;
import static com.note.remindernote.utils.ConstantsBase.PREF_KEEP_CHECKED;
import static com.note.remindernote.utils.ConstantsBase.PREF_KEEP_CHECKMARKS;
import static com.note.remindernote.utils.ConstantsBase.PREF_PRETTIFIED_DATES;
import static com.note.remindernote.utils.ConstantsBase.SWIPE_MARGIN;
import static com.note.remindernote.utils.ConstantsBase.SWIPE_OFFSET;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.materialdialogs.MaterialDialog;
import com.neopixl.pixlui.components.edittext.EditText;
import com.note.checklistview.exceptions.ViewNotSupportedException;
import com.note.checklistview.interfaces.CheckListChangedListener;
import com.note.checklistview.models.CheckListViewItem;
import com.note.checklistview.models.ChecklistManager;
import com.note.remindernote.databinding.FragmentDetailBinding;
import com.note.remindernote.helpers.date.DateHelper;
import com.note.remindernote.helpers.date.RecurrenceHelper;
import com.note.remindernote.listeners.OnReminderPickedListener;
import com.note.remindernote.models.Note;
import com.note.remindernote.models.Task;
import com.note.remindernote.utils.Display;
import com.note.remindernote.utils.KeyboardUtils;
import com.note.remindernote.utils.date.DateUtils;
import com.note.remindernote.utils.date.ReminderPickers;
import com.pixplicity.easyprefs.library.Prefs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;


public class DetailFragment extends Fragment implements OnReminderPickedListener,
        OnTouchListener,
        TextWatcher, CheckListChangedListener {


    private FragmentDetailBinding binding;

    boolean goBack = false;
    private View toggleChecklistView;
    private Uri attachmentUri;
    private Note note;
    private Note noteTmp;
    private Note noteOriginal;
    private ChecklistManager mChecklistManager;
    // Values to print result
    private String exitMessage;
    // Flag to check if after editing it will return to ListActivity or not
    // and in the last case a Toast will be shown instead than Crouton
    private boolean afterSavedReturnsToList = true;
    private boolean showKeyboard = false;
    private boolean swiping;
    private int startSwipeX;
    private boolean orientationChanged;
    private DetailFragment mFragment;
    private int contentLineCounter = 1;
    private MainActivity mainActivity;


    private Calendar calendar;
    private int year;
    private int month;
    private int day;
    private int mHour;
    private int mMinute;


    private Realm realm;
    private boolean isRemoveReminder = false;
    private boolean isAddReminder = false;
    private boolean isCompleteTask = false;
    private int flag = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragment = this;
        realm = Realm.getDefaultInstance();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mainActivity = (MainActivity) getActivity();
        binding.idIvBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                saveAndExit();


            }
        });
        binding.idSaveNote.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateUp();
            }
        });
        binding.idBtDoneTask.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (noteTmp.isChecklist()) {
                    if (mChecklistManager != null && mChecklistManager.mCheckListView != null) {
                        for (int i = mChecklistManager.mCheckListView.getChildCount() - 1; i >= 0; i--) {
                            CheckListViewItem mCheckListViewItem = mChecklistManager.mCheckListView.getChildAt(i);
                            if (!mCheckListViewItem.isHintItem()) {
                                mCheckListViewItem.getCheckBox().setChecked(true);
                            }
                        }
                    }
                    goHome();
                } else {
                    flag = 1;
                    goBack = true;
                    isRemoveReminder = true;
                    noteTmp.setDone(true);
                    noteTmp.setCompletedTime(System.currentTimeMillis());
                    saveNote();
                    fireCompeteNotification();
                }

            }
        });

        init();

    }


    @Override
    public void onPause() {
        super.onPause();

        if (!goBack) {
            saveNote();
        }

        if (toggleChecklistView != null) {
            KeyboardUtils.hideKeyboard(toggleChecklistView);
            binding.contentWrapper.clearFocus();
        }
    }

    private void init() {

        if (note == null) {
            note = getArguments().getParcelable(INTENT_NOTE);
            if (note.getCreation() == null) {
                note.setCreation(System.currentTimeMillis());
            }
        }
        if (noteTmp == null) {
            noteTmp = new Note(note);
        }

        initViews();
    }


    private void initViews() {
        binding.detailRoot.setOnTouchListener(this);

        initViewTitle();
        initViewContent();
        initViewReminder();
        initViewFooter();

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);
        if (noteTmp.getAssignDate() != null) {
            binding.idEtAssignDate.setText(DateHelper
                    .getDateTimeShort(MyApp.getAppContext(), noteTmp.getAssignDate()));
            binding.idEtMaxDate.setText(DateHelper
                    .getDateTimeShort(MyApp.getAppContext(), noteTmp.getMaxDate()));
        } else {
            Calendar calendar = Calendar.getInstance();
            binding.idEtAssignDate.setText(DateHelper
                    .getDateTimeShort(MyApp.getAppContext(), calendar.getTime().getTime()));
            noteTmp.setAssignDate(calendar.getTime().getTime());
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            noteTmp.setMaxDate(calendar.getTime().getTime());
            binding.idEtMaxDate.setText(DateHelper
                    .getDateTimeShort(MyApp.getAppContext(), calendar.getTime().getTime()));

        }
        initReminders();


        if (mChecklistManager != null && mChecklistManager.mCheckListView != null) {
            boolean isChanged = false;
            for (int i = mChecklistManager.mCheckListView.getChildCount() - 1; i >= 0; i--) {
                CheckListViewItem mCheckListViewItem = mChecklistManager.mCheckListView.getChildAt(i);
                if (mCheckListViewItem.getCheckBox().isChecked()) {
                    mCheckListViewItem.getCheckBox().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if (!b) {
                                compoundButton.setChecked(true);
                            }
                        }
                    });
                    mCheckListViewItem.getEditText().setEnabled(false);
                } else {
                    isChanged = true;
                }
            }
            if (!isChanged) {
                binding.fragmentDetailContent.reminderLayout.setEnabled(false);
                binding.idEtAssignDate.setEnabled(false);
                binding.idBtnAssignDate.setEnabled(false);
                binding.idEtMaxDate.setEnabled(false);
                binding.idBtnMaxDate.setEnabled(false);
                binding.detailTitle.setEnabled(false);
                binding.idBtDoneTask.setEnabled(false);
            }

        }

        if (noteTmp.getCompletedTime() != 0) {
            binding.fragmentDetailContent.detailContent.setEnabled(false);
            binding.fragmentDetailContent.reminderLayout.setEnabled(false);
            binding.idEtAssignDate.setEnabled(false);
            binding.idBtnAssignDate.setEnabled(false);
            binding.idEtMaxDate.setEnabled(false);
            binding.idBtnMaxDate.setEnabled(false);
            binding.detailTitle.setEnabled(false);
            binding.idBtDoneTask.setEnabled(false);
        } else if (note.getAssignDate() != null) {
            binding.idEtAssignDate.setEnabled(false);
            binding.idBtnAssignDate.setEnabled(false);
        }

    }

    private void initViewFooter() {
        String creation = DateHelper
                .getFormattedDate(noteTmp.getCreation(), Prefs.getBoolean(PREF_PRETTIFIED_DATES, true));
        binding.creation
                .append(creation.length() > 0 ? getString(R.string.creation) + " " + creation : "");
        if (binding.creation.getText().length() == 0) {
            binding.creation.setVisibility(View.GONE);
        }

        String lastModification = DateHelper
                .getFormattedDate(noteTmp.getLastModification(), Prefs.getBoolean(
                        PREF_PRETTIFIED_DATES, true));
        binding.lastModification
                .append(lastModification.length() > 0 ? getString(R.string.last_update) + " " +
                        lastModification : "");
        if (binding.lastModification.getText().length() == 0) {
            binding.lastModification.setVisibility(View.GONE);
        }
    }

    private void initViewReminder() {
        binding.fragmentDetailContent.reminderLayout.setOnClickListener(v -> {
            ReminderPickers reminderPicker = new ReminderPickers(mainActivity, mFragment);

            reminderPicker.pick(DateUtils.getPresetReminder(noteTmp.getAlarm()), noteTmp
                    .getRecurrenceRule(), noteTmp);
        });

        binding.fragmentDetailContent.reminderLayout.setOnLongClickListener(v -> {
            long reminder = note.getFromReminder();
            if (reminder != 0) {
                MaterialDialog dialog = new MaterialDialog.Builder(mainActivity)
                        .content(R.string.remove_reminder)
                        .positiveText(R.string.ok)
                        .onPositive((dialog1, which) -> {
                            isRemoveReminder = true;
                            binding.fragmentDetailContent.reminderIcon
                                    .setImageResource(R.drawable.ic_alarm_black_18dp);
                            binding.fragmentDetailContent.datetime.setText("");
                        }).build();
                dialog.show();
            }
            return true;
        });

        // Reminder
        String reminderString = initReminder(noteTmp);
        if (!TextUtils.isEmpty(reminderString)) {
            binding.fragmentDetailContent.reminderIcon
                    .setImageResource(R.drawable.ic_alarm_add_black_18dp);
            binding.fragmentDetailContent.datetime.setText(reminderString);
        }
    }


    private void initViewTitle() {
        binding.detailTitle.setText(noteTmp.getTitle());
        binding.detailTitle.gatherLinksForText();
        // To avoid dropping here the  dragged checklist items
        binding.detailTitle.setOnDragListener((v, event) -> {
//					((View)event.getLocalState()).setVisibility(View.VISIBLE);
            return true;
        });
        //When editor action is pressed focus is moved to last character in content field
        binding.detailTitle.setOnEditorActionListener((v, actionId, event) -> {
            binding.fragmentDetailContent.detailContent.requestFocus();
            binding.fragmentDetailContent.detailContent
                    .setSelection(binding.fragmentDetailContent.detailContent.getText().length());
            return false;
        });
        requestFocus(binding.detailTitle);
    }

    private void initViewContent() {

        RealmResults<Task> taskList = realm.where(Task.class).equalTo("creation", noteTmp.get_id()).sort("flag", Sort.ASCENDING, "taskid", Sort.DESCENDING).findAll();
//        taskList.sort("flag")
        StringBuilder stringBuilder = new StringBuilder();
        for (Task task1 : taskList) {
            if (noteTmp.isChecklist())
                if (task1.getFlag() == 0) {
                    stringBuilder.append("[ ] ");
                } else {
                    stringBuilder.append("[x] ");
                }
            stringBuilder.append(task1.getTaskDetail());
            if (noteTmp.isChecklist())
                stringBuilder.append("\n");
        }
        binding.fragmentDetailContent.detailContent.setText(stringBuilder.toString()/*noteTmp.getContent()*/);
        binding.fragmentDetailContent.detailContent.gatherLinksForText();
        // Avoids focused line goes under the keyboard
        binding.fragmentDetailContent.detailContent.addTextChangedListener(this);

        // Restore checklist
        toggleChecklistView = binding.fragmentDetailContent.detailContent;

        if (noteTmp.isChecklist()) {
            noteTmp.setChecklist(false);
            setAlpha(toggleChecklistView, 0);
            toggleChecklist2();
        }
//    moveCheckedItemsToBottom();

    }

    public static void setAlpha(View v, float alpha) {
        if (v != null) {
            v.setAlpha(alpha);
        }
    }

    /**
     * Force focus and shows soft keyboard. Only happens if it's a new note, without shared content.
     * {@link showKeyboard} is used to check if the note is created from shared content.
     */
    @SuppressWarnings("JavadocReference")
    private void requestFocus(final EditText view) {
        if (note.get_id() == null && !noteTmp.isChanged(note) && showKeyboard) {
            KeyboardUtils.showKeyboard(view);
        }
    }


    @SuppressLint("NewApi")
    private boolean goHome() {

        // The activity has managed a shared intent from third party app and
        // performs a normal onBackPressed instead of returning back to ListActivity
        if (!afterSavedReturnsToList) {
            if (!TextUtils.isEmpty(exitMessage)) {
                mainActivity.showToast(exitMessage, Toast.LENGTH_SHORT);
            }
            mainActivity.finish();

        } else {
            if (mChecklistManager != null && mChecklistManager.mCheckListView != null) {
                boolean isChanged = false;
                if (mChecklistManager.mCheckListView.getChildCount() != 1) {
                    for (int i = mChecklistManager.mCheckListView.getChildCount() - 1; i >= 0; i--) {
                        CheckListViewItem mCheckListViewItem = mChecklistManager.mCheckListView.getChildAt(i);
                        if (!mCheckListViewItem.getCheckBox().isChecked()) {
                            if (!mCheckListViewItem.isHintItem())
                                isChanged = true;
                        }
                    }
                    if (!isChanged && noteTmp.getCompletedTime() == 0) {
                        realm.beginTransaction();
                        noteTmp.setAlarm(0);
                        noteTmp.setFromReminder(0);
                        noteTmp.setReminderFired(false);
                        noteTmp.setDone(true);
                        noteTmp.setCompletedTime(System.currentTimeMillis());
                        noteTmp.setLastModification(System.currentTimeMillis());
                        realm.insertOrUpdate(noteTmp);
                        realm.commitTransaction();
                        fireCompeteNotification();

                    }
                }

            } else if (isAddReminder) {
                realm.beginTransaction();
                noteTmp.setFromReminder(0);
                noteTmp.setAlarm(0);
                realm.commitTransaction();
            }

            // Otherwise the result is passed to ListActivity
            if (mainActivity != null) {
                mainActivity.getSupportFragmentManager();
                mainActivity.getSupportFragmentManager().popBackStack();

            }

        }

        return true;
    }

    public void fireCompeteNotification() {
        Intent notificationIntent = new Intent(mainActivity, AlarmReceiver.class);
        notificationIntent.setAction("android.intent.action.EVENT_REMINDER");
        notificationIntent.putExtra("noteId", noteTmp.get_id());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mainActivity, noteTmp.get_id().intValue(), notificationIntent, 0);
        AlarmManager alarmManager = (AlarmManager) mainActivity.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis(), pendingIntent);
    }

    private void navigateUp() {
        afterSavedReturnsToList = true;
        saveAndExit();
    }


    private void toggleChecklist2() {
        boolean keepChecked = Prefs.getBoolean(PREF_KEEP_CHECKED, true);
        boolean showChecks = Prefs.getBoolean(PREF_KEEP_CHECKMARKS, true);
        toggleChecklist2(keepChecked, showChecks);
    }

    private void toggleChecklist2(final boolean keepChecked, final boolean showChecks) {
        mChecklistManager = mChecklistManager == null ? new ChecklistManager(mainActivity) : mChecklistManager;
        int checkedItemsBehavior = Integer
                .parseInt(Prefs.getString("settings_checked_items_behavior", String.valueOf
                        (CHECKED_ON_BOTTOM)));
        if (noteTmp.getCompletedTime() == 0) {
            mChecklistManager = null;
            mChecklistManager = new ChecklistManager(mainActivity);
            mChecklistManager
                    .showCheckMarks(showChecks)
                    .newEntryHint(getString(R.string.checklist_item_hint))
                    .keepChecked(keepChecked)
                    .undoBarContainerView(binding.contentWrapper)
                    .moveCheckedOnBottom(checkedItemsBehavior);

            // Links parsing options
            mChecklistManager.addTextChangedListener(mFragment);
            mChecklistManager.setCheckListChangedListener(mFragment);
        } else {
            mChecklistManager = null;
            mChecklistManager = new ChecklistManager(mainActivity);
            mChecklistManager
                    .showHintItem(false)
                    .showCheckMarks(showChecks)
                    .keepChecked(keepChecked)
                    .dragEnabled(false);


        }
        // Switches the views
        View newView = null;
        try {
            newView = mChecklistManager.convert(toggleChecklistView);
        } catch (ViewNotSupportedException e) {
        }

        // Switches the views
        if (newView != null) {
            mChecklistManager.replaceViews(toggleChecklistView, newView);
            toggleChecklistView = newView;
            animate(toggleChecklistView).alpha(1).scaleXBy(0).scaleX(1).scaleYBy(0).scaleY(1);
            noteTmp.setChecklist(!noteTmp.isChecklist());
        }
    }


    public void saveAndExit() {
        if (isChangedNote() || isRemoveReminder || isAddReminder) {
            new MaterialDialog.Builder(mainActivity)
                    .content(R.string.back_note_confirmation)
                    .positiveText(R.string.ok)
                    .negativeText(R.string.cancel)
                    .onPositive((dialog, which) -> {
                        if (isAdded()) {
                            exitMessage = getString(R.string.note_updated);
                            goBack = true;
                            saveNote();
                        }
                    })
                    .onNegative(((dialog, which) -> {
                        goBack = true;
                        goHome();
                    })).build().show();
        } else {
            goBack = true;
            goHome();
        }
    }

    public boolean isChangeTask() {
        RealmResults<Task> taskList = realm.where(Task.class).equalTo("creation", noteTmp.get_id()).sort("flag", Sort.ASCENDING, "taskid", Sort.DESCENDING).findAll();

        StringBuilder stringBuilder = new StringBuilder();
        for (Task task1 : taskList) {
            if (noteTmp.isChecklist())
                if (task1.getFlag() == 0) {
                    stringBuilder.append("[ ] ");
                } else {
                    stringBuilder.append("[x] ");
                }
            stringBuilder.append(task1.getTaskDetail());
            if (noteTmp.isChecklist())
                stringBuilder.append("\n");
        }
        StringBuilder checkStr = new StringBuilder();
        if (noteTmp.isChecklist() && mChecklistManager != null && mChecklistManager.mCheckListView != null) {
            for (int i = 0; i < mChecklistManager.mCheckListView.getChildCount(); i++) {
                CheckListViewItem mCheckListViewItem = mChecklistManager.mCheckListView.getChildAt(i);
                if (!mCheckListViewItem.isHintItem()) {
                    if (mCheckListViewItem.isChecked()) {
                        checkStr.append("[x] ").append(mCheckListViewItem.getEditText().getText());
                        checkStr.append("\n");
                    } else {
                        checkStr.append("[ ] ").append(mCheckListViewItem.getEditText().getText());
                        checkStr.append("\n");
                    }
                }
            }

        } else {
            checkStr.append(binding.fragmentDetailContent.detailContent.getText().toString());
        }
        return !stringBuilder.toString().contentEquals(checkStr);
    }

    public boolean isChangedNote() {

        if (!noteTmp.getTitle().equals(binding.detailTitle.getText().toString())) {
            return true;
        } else if (note.getAssignDate() != null && !noteTmp.getAssignDate().equals(note.getAssignDate())) {
            return true;
        } else if (note.getMaxDate() != null && !noteTmp.getMaxDate().equals(note.getMaxDate())) {
            return true;
        } else if (isChangeTask()) {
            return true;
        } else
            return false;
    }

    void saveNote() {

        // Changed fields
        noteTmp.setTitle(getNoteTitle());
        noteTmp.setContent(getNoteContent(flag));
        if (isRemoveReminder) {
            noteTmp.setAlarm(0);
            noteTmp.setFromReminder(0);
            noteTmp.setReminderFired(false);

            noteTmp.setLastModification(System.currentTimeMillis());
            isRemoveReminder = false;
        }
        // Check if some text or attachments of any type have been inserted or is an empty note
        if (goBack && TextUtils.isEmpty(noteTmp.getTitle()) && TextUtils.isEmpty(noteTmp.getContent())
            /*&& noteTmp.getAttachmentsList().isEmpty()*/) {
            exitMessage = getString(R.string.empty_note_not_saved);
            goHome();
            return;
        }

//        if (saveNotNeeded() && noteTmp.getFromReminder() == 0) {
//            exitMessage = "";
//            if (goBack) {
//                goHome();
//            }
//            return;
//        }
        noteTmp.setLastModification(System.currentTimeMillis());
        realm.beginTransaction();
        realm.insertOrUpdate(noteTmp);
        realm.commitTransaction();
        goHome();
    }

    /**
     * Checks if nothing is changed to avoid committing if possible (check)
     */
    private boolean saveNotNeeded() {
        return !noteTmp.isChanged(note) || (noteTmp.isLocked());
    }


    private String getNoteTitle() {
        if (!TextUtils.isEmpty(binding.detailTitle.getText())) {
            return binding.detailTitle.getText().toString();
        } else {
            return "";
        }
    }

    private String getNoteContent(int mainflag) {
        String contentText = "";
        if (!noteTmp.isChecklist()) {
            // Due to checklist library introduction the returned EditText class is no more a
            View contentView = binding.detailRoot.findViewById(R.id.detail_content);
            if (contentView instanceof EditText) {
                contentText = ((EditText) contentView).getText().toString();
            } else if (contentView instanceof android.widget.EditText) {
                contentText = ((android.widget.EditText) contentView).getText().toString();
            }
            Task task = realm.where(Task.class)
                    .equalTo(Utils.NOTEID, noteTmp.get_id())
                    .findFirst();
            if (task != null && (isChangeTask() || task.getFlag() != mainflag)) {
                saveTask(task, mainflag, contentText);
            } else
                saveTask(task, mainflag, contentText);
        } else {
            if (mChecklistManager != null) {
//                mChecklistManager.keepChecked(true).showCheckMarks(true);

                contentText = mChecklistManager.getText();
                if (isChangeTask()) {
                    List<Task> taskList = realm.where(Task.class)
                            .equalTo(Utils.NOTEID, noteTmp.get_id())
                            .findAll();
                    realm.beginTransaction();
                    for (Task task : taskList) {
                        task.deleteFromRealm();
                    }
                    realm.commitTransaction();
                    for (int i = mChecklistManager.mCheckListView.getChildCount() - 1; i >= 0; i--) {
                        CheckListViewItem mCheckListViewItem = mChecklistManager.mCheckListView.getChildAt(i);
                        if (!mCheckListViewItem.isHintItem()) {
                            int flag = 0;
                            if (mCheckListViewItem.isChecked()) {
                                flag = 1;
                            }
                            saveTask(null, flag, mCheckListViewItem.getText());

                        }
                    }
                }
            }
        }

        return contentText;
    }


    public void saveTask(Task task, int flag, String mCheckListViewItem) {
        int nextId;
        if (task == null) {
            Number currentIdNum = realm.where(Task.class).max("taskid");
            if (currentIdNum == null) {
                nextId = 1;
            } else {
                nextId = currentIdNum.intValue() + 1;
            }
        } else {
            nextId = task.getTaskid().intValue();
        }
        realm.beginTransaction();
        if (task == null) {
            task = new Task();
            task.setTaskid(Long.valueOf(nextId));
            task.setCreation(noteTmp.get_id());
            task.setFlag(flag);
            task.setTaskDetail(mCheckListViewItem);
        } else {
            task.setFlag(flag);
            task.setTaskDetail(mCheckListViewItem);
        }


        realm.insertOrUpdate(task);
        realm.commitTransaction();
    }


    /**
     * Used to set actual reminder state when initializing a note to be edited
     */
    private void initReminders() {
        binding.idBtnAssignDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialogDatePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                                new TimePickerDialog.OnTimeSetListener() {

                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay,
                                                          int minute) {
                                        String dateStr = new StringBuilder().append(dayOfMonth).append("/")
                                                .append(month + 1).append("/").append(year).append(" ").append(hourOfDay).append(":").append(minute).toString();
                                        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_ASSIGN);
                                        Date date = null;
                                        try {
                                            date = format.parse(dateStr);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        noteTmp.setAssignDate(date.getTime());
                                        binding.idEtAssignDate.setText(DateHelper
                                                .getDateTimeShort(MyApp.getAppContext(), date.getTime()));

                                    }
                                }, mHour, mMinute, false);
                        timePickerDialog.show();
                    }
                }, year, month, day);
                dialogDatePicker.getDatePicker().setMinDate(new Date().getTime());
                dialogDatePicker.show();
            }
        });
        binding.idBtnMaxDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialogDatePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                                new TimePickerDialog.OnTimeSetListener() {

                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay,
                                                          int minute) {
                                        String dateStr = new StringBuilder().append(dayOfMonth).append("/")
                                                .append(month + 1).append("/").append(year).append(" ").append(hourOfDay).append(":").append(minute).toString();
                                        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_ASSIGN);
                                        Date date = null;
                                        try {
                                            date = format.parse(dateStr);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        noteTmp.setMaxDate(date.getTime());
                                        binding.idEtMaxDate.setText(DateHelper
                                                .getDateTimeShort(MyApp.getAppContext(), date.getTime()));

                                    }
                                }, mHour, mMinute, false);
                        timePickerDialog.show();
                    }
                }, year, month, day + 1);
                dialogDatePicker.getDatePicker().setMinDate(new Date().getTime());
                dialogDatePicker.show();
            }
        });


    }


    private String initReminder(Note note) {
        if (noteTmp.getAlarm() == null) {
            return "";
        }
        long reminder = note.getFromReminder();
        String rrule = note.getRecurrenceRule();
        if (reminder != 0 && note.isReminderFired()) {
            return RecurrenceHelper.getNoteRecurrentReminderText(reminder, rrule);
        } else {
            return getString(R.string.add_reminder)/*RecurrenceHelper.getNoteReminderText(reminder)*/;
        }
    }


    @SuppressLint("NewApi")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                int w;

                Point displaySize = Display.getUsableSize(mainActivity);
                w = displaySize.x;

                if (x < SWIPE_MARGIN || x > w - SWIPE_MARGIN) {
                    swiping = true;
                    startSwipeX = x;
                }

                break;

            case MotionEvent.ACTION_UP:
                if (swiping) {
                    swiping = false;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (swiping) {
                    if (Math.abs(x - startSwipeX) > SWIPE_OFFSET) {
                        swiping = false;
                        FragmentTransaction transaction = mainActivity.getSupportFragmentManager()
                                .beginTransaction();
                        DetailFragment mDetailFragment = new DetailFragment();
                        Bundle b = new Bundle();
                        b.putParcelable(INTENT_NOTE, new Note());
                        mDetailFragment.setArguments(b);
                        transaction.replace(R.id.fragment_container, mDetailFragment, FRAGMENT_DETAIL_TAG)
                                .addToBackStack(
                                        FRAGMENT_DETAIL_TAG).commit();
                    }
                }
                break;

            default:
                break;
        }

        return true;
    }


    @Override
    public void onReminderPicked(long timeInMillis, long reminder) {
        realm.beginTransaction();
        noteTmp.setFromReminder(timeInMillis);
        noteTmp.setAlarm(reminder);
        realm.commitTransaction();
        isAddReminder = true;
        if (mFragment.isAdded()) {
            binding.fragmentDetailContent.reminderIcon.setImageResource(R.drawable.ic_alarm_black_18dp);
        }
    }

    @Override
    public void onRecurrenceReminderPicked(String recurrenceRule) {
        noteTmp.setRecurrenceRule(recurrenceRule);
        if (!TextUtils.isEmpty(recurrenceRule)) {
            long reminder = noteTmp.getFromReminder();
            if (reminder != 0 && noteTmp.isReminderFired()) {
                binding.fragmentDetailContent.datetime.setText(RecurrenceHelper
                        .getNoteRecurrentReminderText(noteTmp.getFromReminder(), recurrenceRule));
             } else {
                binding.fragmentDetailContent.datetime.setText(getString(R.string.add_reminder));
            }
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        scrollContent();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Nothing to do
    }

    @Override
    public void afterTextChanged(Editable s) {
        // Nothing to do
    }

    @Override
    public void onCheckListChanged() {
        scrollContent();
    }

    private void scrollContent() {
        if (noteTmp.isChecklist()) {
            if (mChecklistManager.getCount() > contentLineCounter) {
                binding.contentWrapper.scrollBy(0, 60);
            }
            contentLineCounter = mChecklistManager.getCount();
        } else {
            if (binding.fragmentDetailContent.detailContent.getLineCount() > contentLineCounter) {
                binding.contentWrapper.scrollBy(0, 60);
            }
            contentLineCounter = binding.fragmentDetailContent.detailContent.getLineCount();
        }
    }


}
