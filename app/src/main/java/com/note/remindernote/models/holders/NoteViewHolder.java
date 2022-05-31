

package com.note.remindernote.models.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.neopixl.pixlui.components.textview.TextView;
import com.note.remindernote.databinding.NoteLayoutBinding;


public class NoteViewHolder extends ViewHolder {

    public View root;

    public TextView title;
    public TextView content;
    public TextView date;
    public TextView idTVCommonDate;
    public LinearLayout idLL_CommonDate;
    public RecyclerView recNoteList;

    public NoteViewHolder(View view) {
        super(view);

        NoteLayoutBinding binding = NoteLayoutBinding.bind(view);
        root = binding.root;
        idTVCommonDate = binding.idTVCommonDate;
        idLL_CommonDate = binding.idLLCommonDate;
        recNoteList = binding.recNoteList;

    }

}
