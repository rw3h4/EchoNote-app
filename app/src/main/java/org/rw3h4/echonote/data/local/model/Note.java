package org.rw3h4.echonote.data.local.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String title;

    @NonNull
    public String content;

    @NonNull
    public String category;


    public String imageUri;

    public long timestamp;

    public Note() {}

    protected Note(Parcel in) {
        id = in.readInt();;
        title = in.readString();
        content = in.readString();
        category = in.readString();
        imageUri = in.readString();
        timestamp = in.readLong();
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
        parcel.writeString(imageUri);
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

    public String getImageUri() {
        return imageUri;
    }

    public long getTimestamp() {
        return timestamp;
    }

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

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
