package org.rw3h4.echonote.ui.note;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import org.rw3h4.echonote.R;
import org.rw3h4.echonote.data.local.model.Note;
import org.rw3h4.echonote.util.note.GlideImageGetter;

import java.util.Date;
import java.util.Objects;

public class ReadNoteActivity extends AppCompatActivity {

    public static final String EXTRA_NOTE = "note_to_read";
    public static final String EXTRA_CATEGORY_NAME = "category_name_to_read";

    private Note currentNote;
    private String currentCategoryName;

    private final ActivityResultLauncher<Intent> editNoteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // If the user saved an edit, the result code will be RESULT_OK
                if (result.getResultCode() == RESULT_OK) {
                    finish();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_note);

        MaterialToolbar toolbar =  findViewById(R.id.read_note_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();
        if (intent  != null && intent.hasExtra(EXTRA_NOTE)) {
            currentNote = intent.getParcelableExtra(EXTRA_NOTE);
            currentCategoryName = intent.getStringExtra(EXTRA_CATEGORY_NAME);
            populateUI();
        } else {
            // Handle the error case, the note not passed
            finish();
        }
    }

    private void populateUI() {
        TextView title = findViewById(R.id.read_note_title);
        TextView category = findViewById(R.id.read_note_category);
        TextView timestamp = findViewById(R.id.read_note_timestamp);
        TextView content = findViewById(R.id.read_note_content);

        title.setText(currentNote.getTitle());
        category.setText(currentCategoryName);

        long timeToUse = currentNote.getLastEdited() > 0 ? currentNote.getLastEdited()
                : currentNote.getTimestamp();
        String formattedTime = "Last edited: " + DateFormat.format("dd MMM, hh:mm a",
                new Date(timeToUse)).toString();
        timestamp.setText(formattedTime);

        // Use GlideImageGetter for rendering the full note content with images
        GlideImageGetter  imageGetter = new GlideImageGetter(this, content);
        CharSequence spannedText = Html.fromHtml(currentNote.getContent(),
                Html.FROM_HTML_MODE_COMPACT, imageGetter, null);
        content.setText(spannedText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.read_note_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_edit_note) {
            Intent intent = new Intent(ReadNoteActivity.this, AddEditNoteActivity.class);
            intent.putExtra("note_to_edit", currentNote);
            editNoteLauncher.launch(intent);
            return true;
        }
        // Handle back buttin pressed
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}