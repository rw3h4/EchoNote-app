package org.rw3h4.echonote.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import org.rw3h4.echonote.data.local.NoteDao;
import org.rw3h4.echonote.data.local.NoteDatabase;
import org.rw3h4.echonote.data.local.model.Category;
import org.rw3h4.echonote.data.local.model.Note;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class NoteRepository {

    private final NoteDao noteDao;
    private final ExecutorService databaseWriteExecutor;

    private final LiveData<List<Note>> allNotes;
    private final LiveData<List<Category>> allCategories;

    public NoteRepository(Application application) {
        NoteDatabase db = NoteDatabase.getDatabase(application);
        noteDao = db.noteDao();
        databaseWriteExecutor = NoteDatabase.databaseWriteExecutor;
        allNotes = noteDao.getAllNotes();
        allCategories = noteDao.getAllCategories();
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public LiveData<List<Note>> searchNotes(String query) {
        return noteDao.searchNotes(query);
    }

    public LiveData<List<Note>> getNotesByCategoryId(int categoryId) {
        return noteDao.getNotesByCategoryId(categoryId);
    }

    public LiveData<List<Note>> getPinnedNotes() {
        return noteDao.getPinnedNotes();
    }

    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }

    public void saveNoteWithCategory(final Note noteToSave, final String categoryName) {
        databaseWriteExecutor.execute(() -> {
            Category category = noteDao.findCategoryByName(categoryName);
            int categoryId;

            if (category == null) {
                categoryId = (int) noteDao.insertCategory(new Category(categoryName));
            } else {
                categoryId = category.getId();
            }

            Note finalNote = new Note(
                    noteToSave.getId(),
                    noteToSave.getTitle(),
                    noteToSave.getContent(),
                    categoryId,
                    noteToSave.getTimestamp(),
                    System.currentTimeMillis(),
                    noteToSave.isPinned(),
                    noteToSave.getNoteType(),
                    noteToSave.getFilePath(),
                    noteToSave.getDuration()
            );

            noteDao.insertNote(finalNote);
        });
    }

    public void delete(Note note) {
        databaseWriteExecutor.execute(() -> noteDao.deleteNote(note));
    }

    public void updatePinStatus(int noteId, boolean isPinned) {
        databaseWriteExecutor.execute(() -> noteDao.updatePinStatus(noteId, isPinned));
    }

    // Might no longer be needed, saveNoteWithCategory() is used instead.
    public void updateLastEdited(int noteId, long lastEdited) {
        databaseWriteExecutor.execute(() -> noteDao.updateLastEdited(noteId, lastEdited));
    }
}
