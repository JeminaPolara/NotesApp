package com.note.remindernote.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.neopixl.pixlui.components.textview.TextView;
import com.note.remindernote.databinding.NoteLayoutBinding;
import com.note.remindernote.databinding.SubNoteLayoutBinding;

public class SubNoteViewHolder extends RecyclerView.ViewHolder {
    public View root;
    public View cardLayout;
    public View categoryMarker;

    public TextView title;
    public TextView content;
    public TextView date;
    public ImageView alarmIcon;
    public ImageView completedIcon;
    public TextView noteCountDays;
    public TextView noteCompletedDate;
    public TextView noteExpireDate;

    public SubNoteViewHolder(View view) {
        super(view);

        SubNoteLayoutBinding binding = SubNoteLayoutBinding.bind(view);
        root = binding.root;
        cardLayout = binding.cardLayout;
        categoryMarker = binding.categoryMarker;
        title = binding.noteTitle;
        content = binding.noteContent;
        date = binding.noteDate;
        alarmIcon = binding.alarmIcon;
        completedIcon = binding.completedIcon;
        noteCountDays = binding.noteCountDays;
        noteCompletedDate = binding.noteCompletedDate;
        noteExpireDate = binding.noteExpireDate;

    }
}
