package org.rw3h4.echonote.adapter;

import android.net.Uri;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.rw3h4.echonote.R;
import org.rw3h4.echonote.data.local.model.Note;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private final List<Note> notes = new ArrayList<>();
    private final OnNoteClickListener listener;

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
        void onNoteLongClick(Note note);
    }

    public NoteAdapter(OnNoteClickListener listener) {
        this.listener = listener;
    }

    public void setNotes(List<Note> newNotes) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new NoteDiffCallback(this.notes, newNotes)
        );
        notes.clear();
        notes.addAll(newNotes);
        diffResult.dispatchUpdatesTo(this);
    }

    public Note getNoteAt(int position) {
        return notes.get(position);
    }

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
        Note note = notes.get(position);

        holder.titleTextView.setText(note.getTitle());
        holder.categoryTextView.setText(note.getCategory());

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

    @Override
    public int getItemCount() {
        return notes.size();
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
