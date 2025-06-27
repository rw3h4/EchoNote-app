package org.rw3h4.echonote.data.local.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
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
    @NonNull
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

    // Primary Room constructor
    public Note(int id, @NonNull String title, @NonNull String content, int categoryId,
                long timestamp, long lastEdited, boolean isPinned
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
        this.timestamp = timestamp;
        this.lastEdited = lastEdited;
        this.isPinned = isPinned;
    }

    /**
     * Convenience contructor for creating new Note before
     * insertion into the database
     * id set to 0, Room will autogenerate
     * isPinned set to false, new Notes are not pinned by default
     * timestamp set to current time which is system time
     */
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
    }

    protected Note(Parcel in) {
        id = in.readInt();
        title = Objects.requireNonNull(in.readString());
        content = Objects.requireNonNull(in.readString());
        categoryId = in.readInt();
        timestamp = in.readLong();
        lastEdited = in.readLong();
        isPinned = in.readByte() != 0;
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

    @NonNull
    public String getContent() { return content; }

    public int getCategoryId() { return categoryId; }

    public long getTimestamp() { return timestamp; }

    public long getLastEdited() { return lastEdited; }

    public boolean isPinned() { return isPinned; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;

        // Here I consider two notes equal if they have the same ID
        // and content for the DiffUtil purposes
        return id == note.id &&
                timestamp == note.timestamp &&
                lastEdited == note.lastEdited &&
                isPinned == note.isPinned &&
                title.equals(note.title) &&
                content.equals(note.content) &&
                categoryId == note.categoryId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, content, categoryId, timestamp, lastEdited, isPinned);
    }

    @NonNull
    @Override
    public String toString() {
        return "Note{" + "id=" + id + ", title='" + title + '\'' + ", categoryId="
                + categoryId + ", isPinned=" + isPinned + '}';
    }

}
