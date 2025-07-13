package org.rw3h4.echonote.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import org.rw3h4.echonote.data.local.model.Category;
import org.rw3h4.echonote.data.local.model.Note;
import org.rw3h4.echonote.data.local.model.NoteWithCategory;
import org.rw3h4.echonote.repository.NoteRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NotesViewModel extends AndroidViewModel {
    private final NoteRepository repository;
    public final LiveData<List<Category>> allCategories;

    // Using Mutable Live Data. -1 represents "All Notes"
    private final MutableLiveData<Integer> filterCategoryId = new MutableLiveData<>(-1);

    // Now we have two sources which have to be observed by the UI:
    //  - the list of notes (changes based in filter)
    //  - the list of all categories
    // We have to use MediatorLiveData to observe both sources.
    private final MediatorLiveData<List<NoteWithCategory>> notesWithCategories = new MediatorLiveData<>();


    public NotesViewModel(@NonNull Application application) {
        super(application);
        repository = new NoteRepository(application);
        allCategories = repository.getAllCategories();

        LiveData<List<Note>> notesSource = Transformations.switchMap(filterCategoryId, categoryId -> {
            if (categoryId == null || categoryId == -1) {
                return repository.getAllNotes();
            } else {
                return repository.getNotesByCategoryId(categoryId);
            }
        });

        // Mediate between the two sources using helper method combineData
        notesWithCategories.addSource(notesSource, notes -> combineData(notes, allCategories.getValue()));
        notesWithCategories.addSource(allCategories, categories -> combineData(notesSource.getValue(), categories));
    }

    private void combineData(List<Note> notes, List<Category> categories) {
        if (notes == null || categories == null) {
            return;
        }

        Map<Integer, String> categoryMap = new HashMap<>();
        for (Category category : categories) {
            categoryMap.put(category.getId(), category.getName());
        }

        List<NoteWithCategory> result = new ArrayList<>();
        for (Note note : notes) {
            String categoryName = categoryMap.getOrDefault(note.getCategoryId(), "None");
            result.add(new NoteWithCategory(note, Objects.requireNonNull(categoryName)));
        }
        notesWithCategories.setValue(result);
    }

    public LiveData<List<NoteWithCategory>> getNotesWithCategories() {
        return notesWithCategories;
    }

    public void setCategoryFilter(int categoryId) {
        filterCategoryId.setValue(categoryId);
    }

    public LiveData<List<Note>> searchNotes(String query) {
        return repository.searchNotes(query);
    }

    public void delete(Note note) {
        repository.delete(note);
    }

    public void updatePinStatus(int noteId, boolean isPinned) {
        repository.updatePinStatus(noteId, isPinned);
    }
}
