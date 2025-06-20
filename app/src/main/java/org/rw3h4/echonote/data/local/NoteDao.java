package org.rw3h4.echonote.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import org.rw3h4.echonote.data.local.model.Note;

import java.util.List;

@Dao
public interface NoteDao {

    @Insert
    long insertNote(Note note);

    @Update
    void updateNote(Note note);

    @Delete
    void deleteNote(Note note);

    @Query("SELECT * FROM notes ORDER BY is_pinned DESC, last_edited DESC, note_timestamp DESC")
    LiveData<List<Note>> getAllNotes();

    @Query("SELECT * FROM notes WHERE note_title LIKE '%' || :query || '%' OR note_content LIKE '%' || :query || '%' ORDER BY is_pinned DESC, last_edited DESC")
    LiveData<List<Note>> searchNotes(String query);

    @Query("SELECT * FROM notes WHERE note_category = :category ORDER BY is_pinned DESC, last_edited DESC")
    LiveData<List<Note>> searchNotesByCategory(String category);

    @Query("SELECT * FROM notes WHERE is_pinned = 1 ORDER BY last_edited DESC")
    LiveData<List<Note>> getPinnedNotes();

    @Query("UPDATE notes SET is_pinned = :pinned WHERE note_id = :noteId")
    void updatePinStatus(int noteId, boolean pinned);

    @Query("UPDATE notes SET last_edited = :lastEdited WHERE note_id = :noteId")
    void updateLastEdited(int noteId, long lastEdited);
}
