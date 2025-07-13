package org.rw3h4.echonote.ui.note;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import org.rw3h4.echonote.R;
import org.rw3h4.echonote.data.local.model.Category;
import org.rw3h4.echonote.data.local.model.Note;
import org.rw3h4.echonote.viewmodel.AddEditNoteViewModel;

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
    private AddEditNoteViewModel addEditNoteViewModel;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri !=  null) {
                        // Take persistent permission to access this Uri
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;

                        // Insert the image into the EditText
                        insertImageIntoContent(selectedImageUri);
                    }
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
        // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_note);

        // Initialize the view model
        addEditNoteViewModel = new ViewModelProvider(this).get(AddEditNoteViewModel.class);

        initializeViews();
        handleIntent();
        observerViewModel();

        findViewById(R.id.save_note_button).setOnClickListener(v -> saveNote());
        findViewById(R.id.ic_add_edit_close).setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        titleInput = findViewById(R.id.note_title_input);
        contentInput = findViewById(R.id.note_content_input);
        categoryInput = findViewById(R.id.voice_note_category_input);
    }

    /**
     * Checks if we are editing an existing note and populates fields
     */
    private void handleIntent() {
        TextView heading = findViewById(R.id.add_note_textView);
        if (getIntent().hasExtra("note_to_edit")) {
            existingNote = getIntent().getParcelableExtra("note_to_edit");
            heading.setText(R.string.edit_note);
        } else {
            heading.setText(R.string.add_note);
        }
    }

    /**
     * Central place to observe all LiveData from our ViewModel
     */
    private void observerViewModel() {
        addEditNoteViewModel.allCategories.observe(this, categories -> {
            if (categories != null) {
                setupCategorySuggestions(categories);

                if (existingNote != null) {
                    populateNoteFields(existingNote, categories);
                }
            }
        });

        // Observe the saveFinished signal. When true close the activity
        addEditNoteViewModel.getSaveFinished().observe(this, isFinished -> {
            if (isFinished != null && isFinished) {
                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
                finish();
                addEditNoteViewModel.onSaveComplete();
            }
        });
    }

    private void setupCategorySuggestions(List<Category> categories) {
        List<String> categoryNames = new ArrayList<>();
        for (Category category : categories) {
            // "None" is the default category as such should not be suggested
            if (!category.getName().equalsIgnoreCase("None")) {
                categoryNames.add(category.getName());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categoryNames);
        categoryInput.setAdapter(adapter);
    }

    private void populateNoteFields(Note note, List<Category> categories) {
        titleInput.setText(note.getTitle());
        contentInput.setText(note.getContent());

        // Find the category name from it's ID
        for (Category category : categories) {
            if (category.getId() == note.getCategoryId()) {
                categoryInput.setText(category.getName());
                break;
            }
        }
    }

    private void insertImageIntoContent(Uri imageUri) {
        String uriString = imageUri.toString();

        String imgTag = "<img src='" + uriString + "' style='width:95%;'/><br>";

        int start = Math.max(contentInput.getSelectionStart(), 0);
        int end =  Math.max(contentInput.getSelectionEnd(), 0);

        Objects.requireNonNull(contentInput.getText()).replace(Math.min(start, end), Math.max(start, end),
                Html.fromHtml(imgTag, Html.FROM_HTML_MODE_COMPACT), 0, 0);
    }

    /**
     * Gathers data from the UI and informs the ViewModel to save the note
     */
    private void saveNote() {
        String title = Objects.requireNonNull(titleInput.getText()).toString().trim();
        String content = Objects.requireNonNull(contentInput.getText()).toString().trim();
        String categoryName = categoryInput.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // The ViewModel now handles the rest of the complex logic once we pass it
        // the raw data from the UI
        addEditNoteViewModel.saveNote(existingNote, title, content, categoryName);
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

}