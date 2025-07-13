package org.rw3h4.echonote.data.local.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

/**
 * Represents a note in the application.
 * Establish a relationship with the categories table.
 * If a categgory is deleted, any note using it will have
 * its category ID set to default value 1 (for the "None" category).
 * All fields have been updated from public to private final for immutability
 */
@Entity(
        tableName = "notes",
        foreignKeys = @ForeignKey(
                entity = Category.class,
                parentColumns = "category_id",
                childColumns = "note_category_id",
                onDelete = ForeignKey.SET_DEFAULT
        )
)
public class Note implements Parcelable {

    public static final String NOTE_TYPE_TEXT = "TEXT";
    public static final String NOTE_TYPE_VOICE = "VOICE";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "note_id")
    private final int id;

    @NonNull
    @ColumnInfo(name = "note_title")
    private final String title;

    /**
     * Refactor: Consider the note's content as an HTML string.
     * This adding features like text formatting and inline images
     * using URI.
     * This effectively makes the imageUri field redundant.
     */
    @Nullable
    @ColumnInfo(name = "note_content")
    private final String content;

    // The String field is replaced with an integer field to correspond to the
    // Category table. Default value for "None" category set to 1 in the database
    @ColumnInfo(name = "note_category_id", defaultValue = "1")
    private final int categoryId;


    // public String imageUri; migrated to Html for better text formatting and inline images

    @ColumnInfo(name = "note_timestamp")
    private final long timestamp;

    @ColumnInfo(name = "last_edited")
    private final long lastEdited;

    @ColumnInfo(name = "is_pinned")
    private final boolean isPinned;

    @NonNull
    @ColumnInfo(name = "note_type", defaultValue = NOTE_TYPE_TEXT)
    private final String noteType;

    @Nullable
    @ColumnInfo(name = "file_path")
    private final String filePath;

    @Nullable
    @ColumnInfo(name = "duration")
    private final long duration; // in milliseconds

    // Primary Room constructor
    public Note(int id, @NonNull String title, @NonNull String content, int categoryId,
                long timestamp, long lastEdited, boolean isPinned, @NonNull String noteType,
                @Nullable String filePath, long duration
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
        this.timestamp = timestamp;
        this.lastEdited = lastEdited;
        this.isPinned = isPinned;
        this.noteType  = noteType;
        this.filePath = filePath;
        this.duration = duration;
    }

    // Convenience contructor for creating new TEXT Note
    @Ignore
    public Note(@NonNull String title, @NonNull String content, int categoryId) {
        this.id = 0;
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
        long currentTime = System.currentTimeMillis();
        this.timestamp = currentTime;
        this.lastEdited = currentTime;
        this.isPinned = false;
        this.noteType = NOTE_TYPE_TEXT;
        this.filePath = null;
        this.duration = 0;
    }

    // Convenience constructor for creating a new VOICE Note
    @Ignore
    public Note(@NonNull String title, int categoryId, @NonNull String filePath, long duration) {
        this.id = 0;
        this.title = title;
        this.categoryId = categoryId;
        this.filePath = filePath;
        this.duration = duration;
        long currentTime = System.currentTimeMillis();
        this.timestamp = currentTime;
        this.lastEdited = currentTime;
        this.isPinned = false;
        this.noteType = NOTE_TYPE_VOICE;
        this.content = null;
    }

    protected Note(Parcel in) {
        id = in.readInt();
        title = Objects.requireNonNull(in.readString());
        content = Objects.requireNonNull(in.readString());
        categoryId = in.readInt();
        timestamp = in.readLong();
        lastEdited = in.readLong();
        isPinned = in.readByte() != 0;
        noteType = Objects.requireNonNull(in.readString());
        filePath = in.readString();
        duration = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeInt(categoryId);
        dest.writeLong(timestamp);
        dest.writeLong(lastEdited);
        dest.writeByte((byte) (isPinned ? 1 : 0));
        dest.writeString(noteType);
        dest.writeString(filePath);
        dest.writeLong(duration);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };


    public int getId() { return id; }

    @NonNull
    public String getTitle() { return title; }

    @Nullable
    public String getContent() { return content; }

    public int getCategoryId() { return categoryId; }

    public long getTimestamp() { return timestamp; }

    public long getLastEdited() { return lastEdited; }

    public boolean isPinned() { return isPinned; }

    @NonNull
    public String getNoteType() { return noteType; }

    @Nullable
    public String getFilePath() { return filePath; }

    public long getDuration() { return duration; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;

        // Here I consider two notes equal if they have the same ID
        // and content for the DiffUtil purposes
        return id == note.id &&
                categoryId == note.categoryId &&
                timestamp == note.timestamp &&
                lastEdited == note.lastEdited &&
                isPinned == note.isPinned &&
                duration == note.duration &&
                title.equals(note.title) &&
                Objects.equals(content, note.content) &&
                noteType.equals(note.noteType) &&
                Objects.equals(filePath, note.filePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id, title, content, categoryId, timestamp, lastEdited, isPinned,
                noteType, filePath, duration
        );
    }

    @NonNull
    @Override
    public String toString() {
        return "Note{" + "id=" + id + ", title='" + title + '\'' + ", categoryId="
                + categoryId + ", isPinned=" + isPinned + '}';
    }

}
