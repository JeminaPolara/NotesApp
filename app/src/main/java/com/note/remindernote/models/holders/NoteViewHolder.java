

package com.note.remindernote.models.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.neopixl.pixlui.components.textview.TextView;
import com.note.remindernote.databinding.NoteLayoutBinding;


public class NoteViewHolder extends ViewHolder {

    public View root;
    public View cardLayout;
    public View categoryMarker;

    public TextView title;
    public TextView content;
    public TextView date;
    public TextView idTVCommonDate;
    public LinearLayout idLL_CommonDate;
     public ImageView alarmIcon;
     public ImageView completedIcon;
    public TextView noteCountDays;
    public TextView noteCompletedDate;
    public TextView noteExpireDate;

    public NoteViewHolder(View view) {
        super(view);

        NoteLayoutBinding binding = NoteLayoutBinding.bind(view);
        root = binding.root;
        cardLayout = binding.cardLayout;
        categoryMarker = binding.categoryMarker;
        title = binding.noteTitle;
        content = binding.noteContent;
        idTVCommonDate = binding.idTVCommonDate;
        idLL_CommonDate = binding.idLLCommonDate;
        date = binding.noteDate;
         alarmIcon = binding.alarmIcon;
         completedIcon = binding.completedIcon;
        noteCountDays = binding.noteCountDays;
        noteCompletedDate = binding.noteCompletedDate;
        noteExpireDate = binding.noteExpireDate;

    }

}
