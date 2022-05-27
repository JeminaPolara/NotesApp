package com.note.remindernote.adapters;

import static com.note.remindernote.utils.ConstantsBase.PREF_COLORS_APP_DEFAULT;
import static com.note.remindernote.utils.ConstantsBase.PREF_PRETTIFIED_DATES;

import android.app.Activity;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.note.remindernote.MyApp;
import com.note.remindernote.R;
import com.note.remindernote.Utils;
import com.note.remindernote.helpers.date.DateHelper;
import com.note.remindernote.models.Note;
import com.note.remindernote.models.Task;
import com.note.remindernote.models.holders.NoteViewHolder;
import com.note.remindernote.utils.date.DateUtils;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class SubNotesAdapter extends RecyclerView.Adapter<SubNoteViewHolder> {

    private final Activity mActivity;
    List<Note> notes = new ArrayList<>();
    private Realm realm;
    String value = "";
    private long checkBeforeCompletedTime = 0;
    final int CONTENT_SUBSTRING_LENGTH = 300;

    public SubNotesAdapter(Activity mActivity, List<Note> notes, String value) {
        this.mActivity = mActivity;
        this.notes = notes;
        this.value = value;
        System.out.println("valueee======> " + this.value);
        realm = Realm.getDefaultInstance();
    }

    @NonNull
    @Override
    public SubNoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sub_note_layout, parent, false);
        return new SubNoteViewHolder(view);
    }

    private static String limit(String value, int length, boolean singleLine, boolean elipsize) {
        StringBuilder buf = new StringBuilder(value);
        int indexNewLine = buf.indexOf(System.getProperty("line.separator"));
        int endIndex =
                singleLine && indexNewLine < length ? indexNewLine : length < buf.length() ? length : -1;
        if (endIndex != -1) {
            buf.setLength(endIndex);
            if (elipsize) {
                buf.append("...");
            }
        }
        return buf.toString();
    }

    @Override
    public void onBindViewHolder(@NonNull SubNoteViewHolder holder, int position) {
        Note note = notes.get(position);
        System.out.println("Notes Valuessss------------> " + note.getTitle());

        RealmResults<Task> taskList = realm.where(Task.class).equalTo("creation", note.get_id()).sort("flag", Sort.ASCENDING, "taskid", Sort.DESCENDING).findAll();

        StringBuilder contentText = new StringBuilder();
        for (Task task : taskList) {
            if (note.isChecklist())
                contentText.append("\u2022");
            contentText.append(" " + task.getTaskDetail());
            contentText.append("\n");
        }
        holder.title.setText(note.getTitle());
        holder.content.setVisibility(View.VISIBLE);
        holder.content.setText(limit(contentText.toString(), CONTENT_SUBSTRING_LENGTH, false, true));

        holder.alarmIcon.setVisibility(!note.getAlarm().equals("0") ? View.VISIBLE : View.GONE);
        initDates(note, holder, position);
        manageSelectionColor(position, note, holder);
    }

    private void manageSelectionColor(int position, Note note, SubNoteViewHolder holder) {
        if (System.currentTimeMillis() >= note.getMaxDate() && note.getCompletedTime() == 0) {
            holder.cardLayout
                    .setBackgroundColor(mActivity.getResources().getColor(R.color.list_bg_selected));
            holder.noteExpireDate.setVisibility(View.VISIBLE);
            holder.noteExpireDate.setText("Expired but Remaining Task " + DateHelper.getFormattedDate(note.getMaxDate() != null ? note.getMaxDate() : note.get_id(), Prefs.getBoolean(PREF_PRETTIFIED_DATES, true)));
        } else if (note.getCompletedTime() != 0) {
            holder.cardLayout
                    .setBackgroundColor(mActivity.getResources().getColor(R.color.gray_completed));
//            holder.noteExpireDate.setVisibility(View.VISIBLE);
//            holder.noteExpireDate.setText("Expired " + DateHelper.getFormattedDate(note.getMaxDate() != null ? note.getMaxDate() : note.get_id(), Prefs.getBoolean(PREF_PRETTIFIED_DATES, true)));


        } else if (DateUtils.isSameDay(Long.parseLong(value), note.getMaxDate())) {
            holder.cardLayout
                    .setBackgroundColor(mActivity.getResources().getColor(R.color.colorAccentPressed));

        } else
            restoreDrawable(note, holder.cardLayout, holder);
    }

    private void initDates(Note note, SubNoteViewHolder holder, int position) {
        String dateText = MyApp.getAppContext().getString(R.string.last_update) + " " + DateHelper.getFormattedDate(note
                .getLastModification() != null ? note.getLastModification() : note.get_id(), Prefs.getBoolean(PREF_PRETTIFIED_DATES, true));
        holder.date.setText(dateText);
        String dateString = DateFormat.format(Utils.dateFormat, new Date(note.get_id())).toString();
//        holder.idTVCommonDate.setText(dateString);
        holder.completedIcon.setVisibility(View.VISIBLE);

        if (position != 0) {
            if (note.getCompletedTime() == 0) {
                holder.completedIcon.setVisibility(View.GONE);
            }


        } else {
            if (note.getCompletedTime() != 0) {
                checkBeforeCompletedTime = note.getCompletedTime();
                String dateString1 = DateFormat.format(Utils.dateFormat, new Date(note.getCompletedTime())).toString();
//                holder.idTVCommonDate.setText(dateString1);
            } else {
                holder.completedIcon.setVisibility(View.GONE);
            }
        }

        String completedDate = note.getCompletedTime() != 0 ? /*"Completed : " +*/ DateFormat.format(Utils.dateFormat, new Date(note.getCompletedTime())).toString() : "";

//        if (completedDate.equals(holder.idTVCommonDate.getText().toString())) {
        if (DateUtils.isSameDay(Long.parseLong(value), note.get_id())) {
//            if (!DateUtils.isSameDay(note.get_id(), note.getCompletedTime()))
            if (note.getCompletedTime() != 0)
                completedDate = "Created : " + DateFormat.format(Utils.dateFormat, new Date(note.get_id())).toString();
            else {
                completedDate = "";
            }
        } else if (!completedDate.equals("")) {
            completedDate = "Completed : " + completedDate;
        }
        holder.noteCompletedDate.setText(completedDate);
        int count = (int) (((note.getCompletedTime() - note.get_id()) / (1000 * 60 * 60 * 24)));

        String days = String.valueOf(note.getCompletedTime() != 0 ? count == 1 ? count + " day" : count + " days" : "");
        holder.noteCountDays.setText(days);
//        holder.idTVCommonDate.setText(note.getCompletedTime() != 0 ? DateFormat.format(Utils.dateFormat, new Date(note.getCompletedTime())) : holder.idTVCommonDate.getText());
//        holder.idTVCommonDate.setText(completedDate);
//        holder.noteExpireDate.setText();
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public Note getItem(int index) {
        return notes.get(index);
    }

    public void restoreDrawable(Note note, View v) {
        restoreDrawable(note, v, null);
    }


    public void restoreDrawable(Note note, View v, SubNoteViewHolder holder) {
        final int paddingBottom = v.getPaddingBottom();
        final int paddingLeft = v.getPaddingLeft();
        final int paddingRight = v.getPaddingRight();
        final int paddingTop = v.getPaddingTop();
        v.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        colorNote(note, v, holder);
    }

    private void colorNote(Note note, View v, SubNoteViewHolder holder) {

        String colorsPref = Prefs.getString("settings_colors_app", PREF_COLORS_APP_DEFAULT);

        // Checking preference
        if (!colorsPref.equals("disabled")) {

            // Resetting transparent color to the view
            v.setBackgroundColor(Color.parseColor("#00000000"));

        }
    }
}
