package org.rw3h4.echonotex.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import org.rw3h4.echonotex.data.local.model.Category;
import org.rw3h4.echonotex.data.local.model.Note;

import java.util.List;

@Dao
public interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertNote(Note note);

    @Update
    void updateNote(Note note);

    @Delete
    void deleteNote(Note note);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertCategory(Category category);

    @Query("SELECT * FROM categories WHERE category_name = :name LIMIT 1")
    Category findCategoryByName(String name);

    @Query("SELECT * FROM categories ORDER BY category_name ASC")
    LiveData<List<Category>> getAllCategories();

    @Query("SELECT * FROM notes WHERE user_id = :userId ORDER BY is_pinned DESC, last_edited DESC")
    LiveData<List<Note>> getAllNotes(String userId);

    @Query("SELECT * FROM notes WHERE user_id = :userId AND (note_title LIKE :query OR note_content " +
            "LIKE :query) ORDER BY last_edited DESC")
    LiveData<List<Note>> searchNotes(String userId, String query);

    @Query("SELECT * FROM notes WHERE user_id = :userId AND note_category_id = :categoryId " +
            "ORDER BY is_pinned DESC, last_edited DESC")
    LiveData<List<Note>> getNotesByCategoryId(String userId, int categoryId);

    @Query("SELECT * FROM notes WHERE user_id = :userId AND is_pinned = 1 ORDER BY last_edited DESC")
    LiveData<List<Note>> getPinnedNotes(String userId);

    @Query("UPDATE notes SET is_pinned = :pinned WHERE note_id = :noteId")
    void updatePinStatus(int noteId, boolean pinned);

    @Query("UPDATE notes SET last_edited = :lastEdited WHERE note_id = :noteId")
    void updateLastEdited(int noteId, long lastEdited);
}
