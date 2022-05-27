
package com.note.remindernote.adapters;

import static com.note.checklistview.interfaces.Constants.CHECKED_ENTITY;
import static com.note.checklistview.interfaces.Constants.CHECKED_SYM;
import static com.note.checklistview.interfaces.Constants.UNCHECKED_ENTITY;
import static com.note.checklistview.interfaces.Constants.UNCHECKED_SYM;
import static com.note.remindernote.utils.ConstantsBase.PREF_COLORS_APP_DEFAULT;
import static com.note.remindernote.utils.ConstantsBase.PREF_PRETTIFIED_DATES;

import android.app.Activity;
import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.SparseBooleanArray;
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

import java.util.Date;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;


public class NoteAdapter extends RecyclerView.Adapter<NoteViewHolder> {

    private final Activity mActivity;
    private final List<Note> notes;
    private final SparseBooleanArray selectedItems = new SparseBooleanArray();

    final int CONTENT_SUBSTRING_LENGTH = 300;

    private long checkBeforeCompletedTime = 0;
    private Realm realm;

    public NoteAdapter(Activity activity, List<Note> notes) {
        this.mActivity = activity;
        this.notes = notes;
        realm = Realm.getDefaultInstance();
    }


    /**
     * Highlighted if is part of multiselection of notes. Remember to search for child with card ui
     */
    private void manageSelectionColor(int position, Note note, NoteViewHolder holder) {
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


        } else
            restoreDrawable(note, holder.cardLayout, holder);
    }


    public List<Note> getNotes() {
        return notes;
    }


    private void initDates(Note note, NoteViewHolder holder, int position) {
        String dateText = MyApp.getAppContext().getString(R.string.last_update) + " " + DateHelper.getFormattedDate(note
                .getLastModification() != null ? note.getLastModification() : note.get_id(), Prefs.getBoolean(PREF_PRETTIFIED_DATES, true));
        holder.date.setText(dateText);
        String dateString = DateFormat.format(Utils.dateFormat, new Date(note.get_id())).toString();
        holder.idTVCommonDate.setText(dateString);
        holder.completedIcon.setVisibility(View.VISIBLE);

        if (position != 0) {
            if (note.getCompletedTime() == 0) {
                holder.completedIcon.setVisibility(View.GONE);
            }
            if (checkBeforeCompletedTime != 0 && DateUtils.isSameDay(checkBeforeCompletedTime, note.get_id())) {
                holder.idLL_CommonDate.setVisibility(View.GONE);
            } else if (notes.get(position - 1).getCompletedTime() == 0 && note.getCompletedTime() != 0) {
//                holder.idTVCommonDate.setText(DateFormat.format(Utils.dateFormat, new Date(note.getAssignDate())).toString());
                holder.idLL_CommonDate.setVisibility(View.GONE);
            } else if (notes.get(position - 1).getCompletedTime() != 0 && note.getCompletedTime() == 0) {
//                holder.idTVCommonDate.setText(DateFormat.format(Utils.dateFormat, new Date(note.getAssignDate())).toString());
                holder.idLL_CommonDate.setVisibility(View.GONE);
            } else if (note.getCompletedTime() != 0 && DateUtils.isSameDay(notes.get(position - 1).getCompletedTime(), note.get_id())) {
                holder.idTVCommonDate.setText(DateFormat.format(Utils.dateFormat, new Date(note.getCompletedTime())).toString());
                holder.idLL_CommonDate.setVisibility(View.GONE);
            } else if (note.getCompletedTime() != 0 && DateUtils.isSameDay(notes.get(position - 1).getCompletedTime(), note.getCompletedTime())) {
                holder.idTVCommonDate.setText(DateFormat.format(Utils.dateFormat, new Date(note.getCompletedTime())).toString());
                holder.idLL_CommonDate.setVisibility(View.GONE);
            } else if (note.getCompletedTime() != 0 && DateUtils.isSameDay(notes.get(position - 1).get_id(), note.get_id())) {
                holder.idTVCommonDate.setText(DateFormat.format(Utils.dateFormat, new Date(note.get_id())).toString());
                holder.idLL_CommonDate.setVisibility(View.GONE);
            } else if (note.getCompletedTime() != 0 && !DateUtils.isSameDay(notes.get(position - 1).get_id(), note.get_id())) {
                holder.idTVCommonDate.setText(DateFormat.format(Utils.dateFormat, new Date(note.get_id())).toString());
            } else if (note.getCompletedTime() != 0 && DateUtils.isSameDay(note.get_id(), note.getCompletedTime())) {
                holder.idTVCommonDate.setText(DateFormat.format(Utils.dateFormat, new Date(note.get_id())).toString());
                holder.idLL_CommonDate.setVisibility(View.GONE);
            } else if (DateUtils.isSameDay(notes.get(position - 1).get_id(), note.get_id())) {
                holder.idTVCommonDate.setText(DateFormat.format(Utils.dateFormat, new Date(note.get_id())).toString());
                holder.idLL_CommonDate.setVisibility(View.GONE);
                holder.completedIcon.setVisibility(View.GONE);
            } /*else if (note.getCompletedTime() != 0 && !DateUtils.isSameDay(notes.get(position - 1).get_id(), note.getCompletedTime())) {
                holder.idTVCommonDate.setText(DateFormat.format(Utils.dateFormat, new Date(note.getCompletedTime())).toString());
                holder.idLL_CommonDate.setVisibility(View.GONE);
                checkBeforeCompletedTime = note.getCompletedTime();
                holder.completedIcon.setVisibility(View.VISIBLE);
            }*/

/*
            if (notes.get(position - 1).getCompletedTime() == 0) {
                if (!DateUtils.isSameDay(notes.get(position - 1).get_id(), note.getCompletedTime())) {
                    holder.idTVCommonDate.setText(DateFormat.format(Utils.dateFormat, new Date(note.get_id())).toString());
                    holder.idLL_CommonDate.setVisibility(View.VISIBLE);
                }
            }
*/
        } else {
            if (note.getCompletedTime() != 0) {
                checkBeforeCompletedTime = note.getCompletedTime();
                String dateString1 = DateFormat.format(Utils.dateFormat, new Date(note.getCompletedTime())).toString();
                holder.idTVCommonDate.setText(dateString1);
            } else {
                holder.completedIcon.setVisibility(View.GONE);
            }
        }

        String completedDate = note.getCompletedTime() != 0 ? /*"Completed : " +*/ DateFormat.format(Utils.dateFormat, new Date(note.getCompletedTime())).toString() : "";

        if (completedDate.equals(holder.idTVCommonDate.getText().toString())) {
            if (!DateUtils.isSameDay(note.get_id(), note.getCompletedTime()))
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


    public SparseBooleanArray getSelectedItems() {
        return selectedItems;
    }


    public void addSelectedItem(Integer selectedItem) {
        selectedItems.put(selectedItem, true);
    }


    public void removeSelectedItem(Integer selectedItem) {
        selectedItems.delete(selectedItem);
    }


    public void clearSelectedItems() {
        selectedItems.clear();
    }


    public void restoreDrawable(Note note, View v) {
        restoreDrawable(note, v, null);
    }


    public void restoreDrawable(Note note, View v, NoteViewHolder holder) {
        final int paddingBottom = v.getPaddingBottom();
        final int paddingLeft = v.getPaddingLeft();
        final int paddingRight = v.getPaddingRight();
        final int paddingTop = v.getPaddingTop();
        v.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        colorNote(note, v, holder);
    }


    /**
     * Color of category marker if note is categorized a function is active in preferences
     */
    private void colorNote(Note note, View v, NoteViewHolder holder) {

        String colorsPref = Prefs.getString("settings_colors_app", PREF_COLORS_APP_DEFAULT);

        // Checking preference
        if (!colorsPref.equals("disabled")) {

            // Resetting transparent color to the view
            v.setBackgroundColor(Color.parseColor("#00000000"));

        }
    }

    public void replace(@NonNull Note note, int index) {
        if (notes.contains(note)) {
            remove(note);
        } else {
            index = notes.size();
        }
        add(index, note);
    }

    public void add(int index, @NonNull Object o) {
        notes.add(index, (Note) o);
        notifyItemInserted(index);
    }

    public void remove(List<Note> notes) {
        for (Note note : notes) {
            remove(note);
        }
    }

    public void remove(@NonNull Note note) {
        int pos = getPosition(note);
        if (pos >= 0) {
            notes.remove(note);
            notifyItemRemoved(pos);
        }
    }

    public int getPosition(@NonNull Note note) {
        return notes.indexOf(note);
    }

    public Note getItem(int index) {
        return notes.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_layout, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);

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

    @Override
    public int getItemCount() {
        return this.notes.size();
    }

}