package org.rw3h4.echonote.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.rw3h4.echonote.data.local.model.Category;
import org.rw3h4.echonote.data.local.model.Note;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Note.class, Category.class}, version = 2, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {

    public abstract NoteDao noteDao();
    private static volatile NoteDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static NoteDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (NoteDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            NoteDatabase.class,
                            "note_database"
                    ).addCallback(sRoomDatabaseCallback)
                            // TODO: Implement a migration strategy for production.
                            .fallbackToDestructiveMigration().build();
                }
            }
        }

        return INSTANCE;
    }

    /**
     * Callback triggered when the database is first created.
     * I've used it pre-populate the database with default categories
     */
    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            databaseWriteExecutor.execute(() -> {
            NoteDao dao = INSTANCE.noteDao();

            dao.insertCategory(new Category("None"));
            dao.insertCategory(new Category("Personal"));
            dao.insertCategory(new Category("Work"));
            dao.insertCategory(new Category("Important"));
            dao.insertCategory(new Category("Favorite"));
            dao.insertCategory(new Category("Project"));

            });
        }
    };
}
