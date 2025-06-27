package org.rw3h4.echonote.adapter;

import android.text.Html;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import org.rw3h4.echonote.R;
import org.rw3h4.echonote.data.local.model.Note;
import org.rw3h4.echonote.data.local.model.NoteWithCategory;

import java.util.Date;

// Moved to ListAdapter (from RecyclerView.Adapter) to simplify code when using DiffUtil
public class NoteAdapter extends ListAdapter<NoteWithCategory, NoteAdapter.NoteViewHolder> {

    private final OnNoteClickListener listener;

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
        void onNoteLongClick(Note note);
    }

    public NoteAdapter(OnNoteClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    //Changed the DiffUtil Callback to a static final field
    private static final DiffUtil.ItemCallback<NoteWithCategory> DIFF_CALLBACK = new DiffUtil.ItemCallback<NoteWithCategory>() {
        @Override
        public boolean areItemsTheSame(@NonNull NoteWithCategory oldItem, @NonNull NoteWithCategory newItem) {
            return oldItem.getNote().getId() == newItem.getNote().getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull NoteWithCategory oldItem, @NonNull NoteWithCategory newItem) {
            return oldItem.getNote().equals(newItem.getNote()) &&
                    oldItem.getCategoryName().equals(newItem.getCategoryName());
        }
    };

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.note_item, parent, false
        );

        return new NoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteWithCategory currentItem = getItem(position);
        Note note = currentItem.getNote();

        holder.titleTextView.setText(note.getTitle());
        holder.categoryTextView.setText(currentItem.getCategoryName());

        // Migrated from text based note content to Html for inline images and text formatting
        holder.contentTextView.setText(Html.fromHtml(note.getContent(), Html.FROM_HTML_MODE_COMPACT));

        long timeToUse = note.getLastEdited() > 0 ? note.getLastEdited() : note.getTimestamp();
        String formattedTime = DateFormat.format("dd MMM yyyy, hh:mm a", new Date(timeToUse)).toString();
        holder.timestampTextView.setText(formattedTime);

        holder.pinIcon.setVisibility(note.isPinned() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> listener.onNoteClick(note));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onNoteLongClick(note);
            return true;
        });
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, contentTextView, categoryTextView, timestampTextView;
        ImageView pinIcon;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.note_title);
            categoryTextView = itemView.findViewById(R.id.note_category);
            contentTextView = itemView.findViewById(R.id.note_content);
            timestampTextView = itemView.findViewById(R.id.note_timestamp);
            pinIcon = itemView.findViewById(R.id.pin_icon_imageview);
        }

    }
}
