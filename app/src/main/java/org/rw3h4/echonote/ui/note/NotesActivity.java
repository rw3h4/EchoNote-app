package org.rw3h4.echonote.ui.note;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.rw3h4.echonote.R;
import org.rw3h4.echonote.adapter.NoteAdapter;
import org.rw3h4.echonote.data.local.model.Category;
import org.rw3h4.echonote.data.local.model.Note;
import org.rw3h4.echonote.data.local.model.NoteWithCategory;
import org.rw3h4.echonote.viewmodel.NotesViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NotesActivity extends AppCompatActivity {

    private TabLayout categoryTabs;
    private NotesViewModel notesViewModel;
    private NoteAdapter adapter;
    private TextView emptyPlaceholder;
    private TextInputEditText searchBar;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        notesViewModel = new ViewModelProvider(this).get(NotesViewModel.class);

        initializeViews();
        setupWelcomeMessage();
        setupRecyclerView();
        setupCategoryTabs();
        setupSearchBar();
        observeViewModel();

        findViewById(R.id.addNoteBtn).setOnClickListener(v -> {
            Intent intent = new Intent(NotesActivity.this, AddEditNoteActivity.class);
            startActivity(intent);
        });
    }

    private void initializeViews() {
        emptyPlaceholder = findViewById(R.id.empty_placeholder);
        searchBar = findViewById(R.id.search_bar_editText);
        categoryTabs = findViewById(R.id.category_tabs);
        recyclerView = findViewById(R.id.note_recyclerView);
    }

    private void setupWelcomeMessage() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        TextView welcomeText = findViewById(R.id.welcome_user_textView);

        if (user != null) {
            String name = user.getDisplayName();
            if (user.isAnonymous() || name == null || name.isEmpty()) {
                welcomeText.setText("Hi, Guest!");
            } else {
                welcomeText.setText("Hi, " + name.split(" ")[0]);
            }
        }
    }

    private void setupRecyclerView() {
        adapter = new NoteAdapter(new NoteAdapter.OnNoteClickListener() {
            @Override
            public void onNoteClick(Note note) {
                Intent intent = new Intent(NotesActivity.this, AddEditNoteActivity.class);
                intent.putExtra("note_to_edit", note);
                startActivity(intent);
            }

            @Override
            public void onNoteLongClick(Note note) {
                notesViewModel.updatePinStatus(note.getId(), !note.isPinned());
            }
        });

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL)
        );
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                NoteWithCategory itemToDelete = adapter.getCurrentList().get(position);
                notesViewModel.delete(itemToDelete.getNote());
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void setupCategoryTabs() {
        categoryTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getTag() != null) {
                    int categoryId = (int) tab.getTag();
                    notesViewModel.setCategoryFilter(categoryId);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    observeViewModel();
                } else {
                    notesViewModel.searchNotes(s.toString()).observe(NotesActivity.this, notes -> {

                    });
                }
            }
        });
    }

    private void observeViewModel() {
        notesViewModel.getNotesWithCategories().observe(this, notesWithCategories -> {
            adapter.submitList(notesWithCategories);
            emptyPlaceholder.setVisibility(notesWithCategories.isEmpty() ? View.VISIBLE : View.GONE);
        });

        notesViewModel.allCategories.observe(this, this::updateCategoryTabs);
    }

    /**
     * Clears and recreates the category tabs based on the data from the database
     */
    private void updateCategoryTabs(List<Category> categories) {
        // Store the currently selected tag  to restore selection after recreating tags
        Object selectedTag = categoryTabs.getTabAt(categoryTabs.getSelectedTabPosition()) != null ?
                Objects.requireNonNull(categoryTabs.getTabAt(categoryTabs.getSelectedTabPosition())).getTag() : -1;

        categoryTabs.removeAllTabs();

        // Add the "All" tab first
        TabLayout.Tab allTab = categoryTabs.newTab().setText("All");
        allTab.setTag(-1);
        categoryTabs.addTab(allTab);

        // Add a tab for each category in the database
        for (Category category : categories) {
            if (!category.getName().equalsIgnoreCase("None")) {
                TabLayout.Tab tab = categoryTabs.newTab().setText(category.getName());
                tab.setTag(category.getId());
                categoryTabs.addTab(tab);
            }
        }

        // Restore the previous selection
        for (int i = 0; i < categoryTabs.getTabCount();  i++) {
            if (Objects.equals(Objects.requireNonNull(categoryTabs.getTabAt(i)).getTag(), selectedTag)) {
                Objects.requireNonNull(categoryTabs.getTabAt(i)).select();
                break;
            }
        }
    }
}