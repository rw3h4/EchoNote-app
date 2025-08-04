package org.rw3h4.echonotex.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.rw3h4.echonotex.data.local.model.Category;
import org.rw3h4.echonotex.data.local.model.Note;
import org.rw3h4.echonotex.repository.NoteRepository;

import java.util.List;

/**
 * Dedicated view model class for the RecordVoiceNoteActivity
 */
public class RecordVoiceNoteViewModel extends AndroidViewModel {
    private final NoteRepository repository;
    public final LiveData<List<Category>> allCategories;
    private final MutableLiveData<Boolean> saveFinished = new MutableLiveData<>();

    public RecordVoiceNoteViewModel(@NonNull Application application) {
        super(application);
        repository = new NoteRepository(application);
        allCategories = repository.getAllCategories();
    }

    public LiveData<Boolean> getSaveFinished() {
        return saveFinished;
    }

    /**
     * Creates a new voice object and tells the repository to save it
     */
    public void saveVoiceNote(String title, String categoryName, String filePath, long duration,
                              @NonNull String userId) {
        if (TextUtils.isEmpty(title)) {
            // Don't save notes without a title
            return;
        }

        final String finalCategoryName = TextUtils.isEmpty(categoryName) ? "None" : categoryName;

        Note noteToSave = new Note(title, 0, filePath, duration, userId);

        repository.saveNoteWithCategory(noteToSave, finalCategoryName);
        saveFinished.setValue(true);
    }

    public void onSaveComplete() {
        saveFinished.setValue(false);
    }
}
