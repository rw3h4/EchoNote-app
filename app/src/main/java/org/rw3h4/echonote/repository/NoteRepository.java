package org.rw3h4.echonote.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import org.rw3h4.echonote.data.local.NoteDao;
import org.rw3h4.echonote.data.local.NoteDatabase;
import org.rw3h4.echonote.data.local.model.Note;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteRepository {

    private final NoteDao noteDao;
    private final LiveData<List<Note>> allNotes;
    private final ExecutorService executorService;

    public NoteRepository(Application application) {
        NoteDatabase db = NoteDatabase.getInstance(application);
        noteDao = db.noteDao();
        allNotes = noteDao.getAllNotes();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public LiveData<List<Note>> searchNotesByTitle(String title) {
        return noteDao.searchNotesByTitle("%" + title + "%");
    }

    public LiveData<List<Note>> searchNotesByCategory(String category) {
        return noteDao.searchNotesByCategory("%" + category + "%");
    }

    public void insert(Note note) {
        executorService.execute(() -> noteDao.insertNote(note));
    }

    public void update(Note note) {
        executorService.execute(() -> noteDao.updateNote(note));
    }

    public void delete(Note note) {
        executorService.execute(() -> noteDao.deleteNote(note));
    }
}
