package org.rw3h4.echonote.data.local.model;

public class NoteWithCategory {
    private final Note note;
    private final String categoryName;

    public NoteWithCategory(Note note, String categoryName) {
        this.note = note;
        this.categoryName = categoryName;
    }

    public Note getNote() { return note; }

    public String getCategoryName() { return categoryName; }
}
