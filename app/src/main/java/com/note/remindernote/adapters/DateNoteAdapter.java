
package com.note.remindernote.adapters;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.note.remindernote.MainActivity;
import com.note.remindernote.R;
import com.note.remindernote.Utils;
import com.note.remindernote.models.Note;
import com.note.remindernote.models.holders.NoteViewHolder;
import com.note.remindernote.utils.date.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;


public class DateNoteAdapter extends RecyclerView.Adapter<NoteViewHolder> {

    private final MainActivity mActivity;
    private final HashMap<String, List<Note>> notes;

    private Realm realm;

    public DateNoteAdapter(MainActivity activity, HashMap<String, List<Note>> notes) {
        this.mActivity = activity;
        this.notes = notes;
        realm = Realm.getDefaultInstance();
    }


    public HashMap<String, List<Note>> getNotes() {
        return notes;
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