
package com.note.remindernote.adapters;

import static com.note.remindernote.utils.ConstantsBase.PREF_COLORS_APP_DEFAULT;

import android.graphics.Color;
import android.text.format.DateFormat;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.note.remindernote.MainActivity;
import com.note.remindernote.R;
import com.note.remindernote.Utils;
import com.note.remindernote.listeners.RecyclerViewItemClickSupport;
import com.note.remindernote.models.Note;
import com.note.remindernote.models.holders.NoteViewHolder;
import com.note.remindernote.utils.date.DateUtils;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;


public class DateNoteAdapter extends RecyclerView.Adapter<NoteViewHolder> {

    private final MainActivity mActivity;
    private final HashMap<String, List<Note>> notes;
    private final SparseBooleanArray selectedItems = new SparseBooleanArray();

    final int CONTENT_SUBSTRING_LENGTH = 300;

    private long checkBeforeCompletedTime = 0;
    private Realm realm;

    public DateNoteAdapter(MainActivity activity, HashMap<String, List<Note>> notes) {
        this.mActivity = activity;
        this.notes = notes;
        realm = Realm.getDefaultInstance();
    }


    /**
     * Highlighted if is part of multiselection of notes. Remember to search for child with card ui
     */


    public HashMap<String, List<Note>> getNotes() {
        return notes;
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
        String value = (new ArrayList<String>(notes.keySet())).get(position);
        if (position != 0) {
            String value1 = (new ArrayList<String>(notes.keySet())).get(position - 1);
            if (DateUtils.isSameDay(Long.parseLong(value1), Long.parseLong(value))) {
                holder.idLL_CommonDate.setVisibility(View.GONE);
            } else {
                holder.idLL_CommonDate.setVisibility(View.VISIBLE);
            }
        }
        holder.idTVCommonDate.setText(DateFormat.format(Utils.dateFormat, new Date(Long.parseLong(value))).toString());



    }

    @Override
    public int getItemCount() {
        return this.notes.size();
    }

}