package org.rw3h4.echonote.ui.note;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import org.rw3h4.echonote.R;
import org.rw3h4.echonote.data.local.model.Note;
import org.rw3h4.echonote.viewmodel.NoteViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddEditNoteActivity extends AppCompatActivity {

    /**
    private static final int REQUEST_CODE_GALLERY = 101;
    private static final int REQUEST_CODE_CAMERA = 102;
     */

    private TextInputEditText titleInput, contentInput;
    private AutoCompleteTextView categoryInput;
    private ImageView imagePreview;
    private Uri imageUri = null;
    private Note existingNote;
    private NoteViewModel noteViewModel;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    imagePreview.setImageURI(imageUri);
                    imagePreview.setVisibility(View.VISIBLE);
                }
            }
    );

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    imagePreview.setImageURI(imageUri);
                    imagePreview.setVisibility(View.VISIBLE);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_note);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        titleInput = findViewById(R.id.note_title_input);
        contentInput = findViewById(R.id.note_content_input);
        categoryInput = findViewById(R.id.note_category_input);

        ImageView galleryBtn = findViewById(R.id.add_attachment);
        ImageView cameraBtn = findViewById(R.id.add_take_photo);
        ImageView saveBtn = findViewById(R.id.save_note_button);
        TextView heading = findViewById(R.id.add_note_textView);

        setupCategorySuggestions();

        if (getIntent() != null &&getIntent().hasExtra("note")) {
            existingNote = getIntent().getParcelableExtra("note");
            if (existingNote != null) {
                heading.setText(R.string.edit_note);
                populateNoteFields(existingNote);
            } else {
                Toast.makeText(this, "Failed to load note", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            heading.setText(R.string.add_note);
        }

        galleryBtn.setOnClickListener(v -> openGallery());
        cameraBtn.setOnClickListener(v -> openCamera());

        saveBtn.setOnClickListener(v -> saveNote());
    }

    private void setupCategorySuggestions() {
        List<String> suggestions = getUserCustomCategories();
        suggestions.remove("All");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, suggestions);
        categoryInput.setAdapter(adapter);
    }

    private void populateNoteFields(Note note) {
        titleInput.setText(note.getTitle());
        contentInput.setText(note.getContent());
        categoryInput.setText(note.getCategory());

    }

    private void saveNote() {
        String title = Objects.requireNonNull(titleInput.getText()).toString().trim();
        String content = Objects.requireNonNull(contentInput.getText()).toString().trim();
        String rawCategory = categoryInput.getText() != null ? categoryInput.getText().toString().trim() : "";
        String category = rawCategory.isEmpty() ? "None" : rawCategory;

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "Note is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            String imgTag = "<br><img src=\"" + imageUri.toString() + "\"/>br>";
            content += imgTag;
        }

        long now = System.currentTimeMillis();

        if (existingNote != null) {
            existingNote.setTitle(title);
            existingNote.setContent(content);
            existingNote.setCategory(category);
            noteViewModel.update(existingNote);
            noteViewModel.updateLastEdited(existingNote.getId(), now);
            Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
        } else {
            Note newNote = new Note(title, content, category, now, now, false);
            noteViewModel.insert(newNote);
            Toast.makeText(this, "Note added", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.READ_MEDIA_IMAGES}, 200);
                return;
            }
        }

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.CAMERA}, 201);
            return;
        }

        File imageFile = new File(getCacheDir(), "camera_image" + System.currentTimeMillis() + ".jpg");
        imageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cameraLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((requestCode == 200 || requestCode == 201) && grantResults.length > 0 && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 200) openGallery();
            else openCamera();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private List<String> getUserCustomCategories() {
        List<String> customCategories = new ArrayList<>();
        customCategories.add("All");
        customCategories.add("Important");
        customCategories.add("Bookmarked");
        customCategories.add("Favorite");
        customCategories.add("Work");
        customCategories.add("Personal");
        customCategories.add("Home");
        customCategories.add("Project");

        return customCategories;
    }
}