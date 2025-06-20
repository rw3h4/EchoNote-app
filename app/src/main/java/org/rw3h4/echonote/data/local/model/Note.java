package org.rw3h4.echonote.data.local.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "notes")
public class Note implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "note_id")
    public int id;

    @NonNull
    @ColumnInfo(name = "note_title")
    public String title;

    /**
     * Refactor: Consider the note's content as an HTML string.
     * This adding features like text formatting and inline images
     * using URI.
     * This effectively makes the imageUri field redundant.
     */
    @NonNull
    @ColumnInfo(name = "note_content")
    public String content;

    @NonNull
    @ColumnInfo(name = "note_category")
    public String category;


    // public String imageUri;

    @ColumnInfo(name = "note_timestamp")
    public long timestamp;

    @ColumnInfo(name = "last_edited")
    public long lastEdited;

    @ColumnInfo(name = "is_pinned")
    public boolean isPinned;

    public Note() {}

    public Note(@NonNull String title, @NonNull String content, @NonNull String category,
                long timestamp, long lastEdited, boolean isPinned) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.timestamp = timestamp;
        this.lastEdited = lastEdited;
        this.isPinned = isPinned;
    }

    protected Note(Parcel in) {
        id = in.readInt();
        title = Objects.requireNonNull(in.readString());
        content = Objects.requireNonNull(in.readString());
        category = Objects.requireNonNull(in.readString());
        timestamp = in.readLong();
        lastEdited = in.readLong();
        isPinned = in.readByte() != 0;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(title);
        parcel.writeString(content);
        parcel.writeString(category);
        parcel.writeLong(timestamp);
        parcel.writeLong(lastEdited);
        parcel.writeByte((byte) (isPinned ? 1 : 0));
    }

    public int getId() {
        return id;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getContent() {
        return content;
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getLastEdited() { return lastEdited; }

    public boolean isPinned() { return isPinned; }



    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public void setContent(@NonNull String content) {
        this.content = content;
    }

    public void setCategory(@NonNull String category) {
        this.category = category;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setLastEdited(long lastEdited) {
        this.lastEdited = lastEdited;
    }

    public void setPinned(boolean pinned) {
        this.isPinned = pinned;
    }
}
