package org.rw3h4.echonote.viewmodel;

import android.app.Application;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.rw3h4.echonote.data.local.model.Category;
import org.rw3h4.echonote.data.local.model.Note;
import org.rw3h4.echonote.repository.NoteRepository;

import java.util.List;

public class AddEditNoteViewModel extends AndroidViewModel {
    private final NoteRepository repository;

    public final LiveData<List<Category>> allCategories;

    // To be used to signal the Activity to finish after a successful save.
    private final MutableLiveData<Boolean> saveFinished = new MutableLiveData<>();

    public AddEditNoteViewModel(@NonNull Application application) {
        super(application);
        repository = new NoteRepository(application);
        allCategories = repository.getAllCategories();
    }

    public LiveData<Boolean> getSaveFinished() {
        return saveFinished;
    }


    /**
     * This is the main for saving a note. Handles both creating a new note and
     * updating an existing note
     *
     * @param existingNote Original note if we are editing, null if creating a new one
     * @param title  The notes title from the input field
     * @param content Note's content from the input field
     * @param categoryName The category name from the AutoCompleteTextView
     */
    public void saveNote(Note existingNote, String title, String content, String categoryName) {
        if (TextUtils.isEmpty(title)) {
            return;
        }

        final String finalCategoryName = TextUtils.isEmpty(categoryName) ? "None" : categoryName;

        Note noteToSave;
        if (existingNote == null) {
            noteToSave = new Note(title, content, 0);
        } else {
            noteToSave = new Note(
                    existingNote.getId(),
                    title,
                    content,
                    existingNote.getCategoryId(),
                    existingNote.getTimestamp(),
                    System.currentTimeMillis(),
                    existingNote.isPinned(),
                    Note.NOTE_TYPE_TEXT,
                    null,
                    0
            );
        }

        repository.saveNoteWithCategory(noteToSave, finalCategoryName);

        saveFinished.setValue(true);
    }

    public void onSaveComplete() {
        saveFinished.setValue(false);
    }
}
