package org.rw3h4.echonote.data.local.model;

import android.os.Parcel;
import android.os.Parcelable;

public class NoteWithCategory implements Parcelable {
    private final Note note;
    private final String categoryName;

    public NoteWithCategory(Note note, String categoryName) {
        this.note = note;
        this.categoryName = categoryName;
    }

    protected NoteWithCategory(Parcel in) {
        note = in.readParcelable(Note.class.getClassLoader());
        categoryName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(note, flags);
        dest.writeString(categoryName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<NoteWithCategory> CREATOR = new Creator<NoteWithCategory>() {
        @Override
        public NoteWithCategory createFromParcel(Parcel in) {
            return new NoteWithCategory(in);
        }

        @Override
        public NoteWithCategory[] newArray(int size) {
            return new NoteWithCategory[size];
        }
    };

    public Note getNote() { return note; }

    public String getCategoryName() { return categoryName; }
}
