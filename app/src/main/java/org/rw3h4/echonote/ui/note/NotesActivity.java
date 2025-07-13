package org.rw3h4.echonote.ui.note;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.rw3h4.echonote.R;
import org.rw3h4.echonote.adapter.NoteAdapter;
import org.rw3h4.echonote.data.local.model.Category;
import org.rw3h4.echonote.data.local.model.Note;
import org.rw3h4.echonote.data.local.model.NoteWithCategory;
import org.rw3h4.echonote.ui.auth.LoginActivity;
import org.rw3h4.echonote.ui.voice.record.RecordVoiceNoteActivity;
import org.rw3h4.echonote.viewmodel.NotesViewModel;
import org.rw3h4.echonote.ui.voice.VoiceOptionsBottomSheetFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NotesActivity extends AppCompatActivity implements VoiceOptionsBottomSheetFragment.VoiceOptionsListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TabLayout categoryTabs;
    private TextView emptyPlaceholder;
    private TextInputEditText searchBar;
    private RecyclerView recyclerView;

    private NotesViewModel notesViewModel;
    private NoteAdapter adapter;

    private LiveData<List<Note>> searchResultsLiveData;
    private Observer<List<Note>> searchObserver;

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
        setupNavigationDrawer();
        setupRecyclerView();
        setupCategoryTabs();
        setupSearchBar();
        observeViewModel();

        findViewById(R.id.addNoteBtn).setOnClickListener(v -> {
            Intent intent = new Intent(NotesActivity.this, AddEditNoteActivity.class);
            startActivity(intent);
        });

        View voiceRecordButton = findViewById(R.id.voice_recordButton);

        voiceRecordButton.setOnClickListener(v -> {
            VoiceOptionsBottomSheetFragment bottomSheet = VoiceOptionsBottomSheetFragment.newInstance();
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        emptyPlaceholder = findViewById(R.id.empty_placeholder);
        searchBar = findViewById(R.id.search_bar_editText);
        categoryTabs = findViewById(R.id.category_tabs);
        recyclerView = findViewById(R.id.note_recyclerView);
    }

    private void setupNavigationDrawer() {
        ImageButton menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        updateNavHeader();

        // Handle what happends when the menu item is clicked
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();

                // Navigate to the login screen and clear backstack
                Intent intent = new Intent(NotesActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            // TODO: Implement screens for the other menu items

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void updateNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView headerUserName = headerView.findViewById(R.id.nav_header_user_name);
        TextView headerUserEmail = headerView.findViewById(R.id.nav_header_user_email);
        ImageView toolbarAvatar = findViewById(R.id.user_avatar_imageView);
        ImageView navHeaderAvatar = headerView.findViewById(R.id.nav_header_user_avatar);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Menu navMenu = navigationView.getMenu();

        if (user != null) {
            if (user.isAnonymous()) {
                headerUserName.setText("Guest User");
                headerUserEmail.setVisibility(View.GONE);

                // Implement show login and hide logout for Guest mode
                navMenu.findItem(R.id.nav_login).setVisible(true);
                navMenu.findItem(R.id.nav_logout).setVisible(false);
            } else {
                String name = user.getDisplayName();
                String email = user.getEmail();
                Uri photoUrl = user.getPhotoUrl();

                headerUserName.setText(name != null && !name.isEmpty() ? name : "User");
                headerUserEmail.setText(email);
                headerUserEmail.setVisibility(View.VISIBLE);

                if (photoUrl != null) {
                    Glide.with(this).load(photoUrl).into(toolbarAvatar);
                    Glide.with(this).load(photoUrl).into(navHeaderAvatar);
                }

                // Implement show logout and hide login for logged in users
                navMenu.findItem(R.id.nav_login).setVisible(false);
                navMenu.findItem(R.id.nav_logout).setVisible(true);
            }
        }
    }

    private void setupRecyclerView() {
        adapter = new NoteAdapter(new NoteAdapter.OnNoteClickListener() {
            @Override
            public void onNoteClick(Note note) {
                if (note.getNoteType().equals(Note.NOTE_TYPE_VOICE)) {
                    // TODO: Create dedicated player for voice notes

                    Toast.makeText(NotesActivity.this, "Open Voice player for: "
                            + note.getTitle(), Toast.LENGTH_SHORT).show();
                } else {
                    NoteWithCategory item = findNoteWithCategoryById(note.getId());
                    if (item == null) return;
                    Intent intent = new Intent(NotesActivity.this, ReadNoteActivity.class);
                    intent.putExtra(ReadNoteActivity.EXTRA_NOTE, item.getNote());
                    intent.putExtra(ReadNoteActivity.EXTRA_CATEGORY_NAME, item.getCategoryName());
                    startActivity(intent);
                }
            }

            @Override
            public void onNoteLongClick(Note note) {
                notesViewModel.updatePinStatus(note.getId(), !note.isPinned());
            }

            @Override
            public void onPlayVoiceNoteClick(Note note, ImageButton playButton) {
                // TODO: Create dedicated player for voice notes
                if (playButton.getTag() == null || !(boolean) playButton.getTag()) {
                    playButton.setImageResource(R.drawable.ic_pause_circle);
                    playButton.setTag(true);
                    Toast.makeText(NotesActivity.this, "Playing: "
                            + note.getTitle(), Toast.LENGTH_SHORT).show();
                    // TODO: Implement pause the media player
                }
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
                if (position != RecyclerView.NO_POSITION) {
                    NoteWithCategory itemToDelete = adapter.getCurrentList().get(position);
                    notesViewModel.delete(itemToDelete.getNote());
                }
            }
        }).attachToRecyclerView(recyclerView);
    }

    private NoteWithCategory findNoteWithCategoryById(int noteId) {
        for (NoteWithCategory item : adapter.getCurrentList()) {
            if (item.getNote().getId() == noteId) {
                return item;
            }
        }

        return null;
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
        searchObserver = notes -> {
            // The observer is attached to the search results LiveData.
            // Converts the search results (List<Note) to display format
            // (List<NoteWithCategory>)
            List<Category> allCategories = notesViewModel.allCategories.getValue();
            if (notes != null && allCategories != null) {
                List<NoteWithCategory> displayList = convertToNoteWithCategory(notes, allCategories);
                adapter.submitList(displayList);
                emptyPlaceholder.setVisibility(displayList.isEmpty() ? View.VISIBLE : View.GONE);
            }
        };

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Remove any previous conflicting saerch observer.
                if (searchResultsLiveData != null) {
                    searchResultsLiveData.removeObserver(searchObserver);
                }

                String query = s.toString().trim();
                if (query.isEmpty()) {
                    observeViewModel();
                } else {
                    notesViewModel.getNotesWithCategories().removeObservers(NotesActivity.this);
                    searchResultsLiveData = notesViewModel.searchNotes(query);
                    searchResultsLiveData.observe(NotesActivity.this, searchObserver);
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

    private List<NoteWithCategory> convertToNoteWithCategory(List<Note> notes, List<Category> categories) {
        Map<Integer, String> categoryMap = new HashMap<>();
        for (Category category : categories) {
            categoryMap.put(category.getId(), category.getName());
        }

        List<NoteWithCategory> result = new ArrayList<>();

        for (Note note : notes) {
            String categoryName = categoryMap.getOrDefault(note.getCategoryId(), "None");
            result.add(new NoteWithCategory(note, Objects.requireNonNull(categoryName)));
        }

        return result;
    }

    @Override
    public void onRecordVoiceNoteClicked() {
        Intent intent =  new Intent(this, RecordVoiceNoteActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDictateNoteClicked() {
        // TODO: This is where the speech to text recognizer will be started
        Toast.makeText(this, "Dictate Note selected", Toast.LENGTH_SHORT).show();
    }
}