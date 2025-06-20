package org.rw3h4.echonote.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.rw3h4.echonote.data.local.model.Note;
import org.rw3h4.echonote.repository.NoteRepository;

import java.util.List;

public class NoteViewModel extends AndroidViewModel {
    private final NoteRepository repository;
    private final LiveData<List<Note>> allNotes;
    private final LiveData<List<Note>> pinnedNotes;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        repository = new NoteRepository(application);
        allNotes = repository.getAllNotes();
        pinnedNotes = repository.getPinnedNotes();
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public LiveData<List<Note>> getPinnedNotes() {
        return pinnedNotes;
    }

    public LiveData<List<Note>> searchNotesByTitle(String title) {
        return repository.searchNotesByTitle(title);
    }

    public LiveData<List<Note>> searchNotesByCategory(String category) {
        return repository.searchNotesByCategory(category);
    }

    public void insert(Note note) {
        repository.insert(note);
    }

    public void update(Note note) {
        repository.update(note);
    }

    public void delete(Note note) {
        repository.delete(note);
    }

    public void updatePinStatus(int noteId, boolean isPinned) {
        repository.updatePinStatus(noteId, isPinned);
    }

    public void updateLastEdited(int noteId, long lastEdited) {
        repository.updateLastEdited(noteId, lastEdited);
    }
}
