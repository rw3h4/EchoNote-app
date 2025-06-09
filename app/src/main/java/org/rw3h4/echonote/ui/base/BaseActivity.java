package org.rw3h4.echonote.ui.base;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import org.rw3h4.echonote.ui.note.NotesActivity;

/**
 * Base activity with helper methods for the authentication pages
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
    }

    //Method to handle window insets for better UI
    protected void setupWindowInsets(View mainContent) {
        WindowInsetsControllerCompat windowInsetsController = ViewCompat.getWindowInsetsController(mainContent);
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(true);
            windowInsetsController.setAppearanceLightNavigationBars(true);
        }

        ViewCompat.setOnApplyWindowInsetsListener(mainContent, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    //Method to set up shared element transition for the logo
    protected void setupSharedElementTransition(int logoId) {
        ImageView logoIcon = findViewById(logoId);
        if (logoIcon != null) {
            ViewCompat.setTransitionName(logoIcon, "shared_logo");
        }
    }

    //Helper method to set on-click listener to textViews and navigation of shared element transition.
    protected void setNavigationWithSharedElement(int textViewId, Class<?> targetClass, int logoId) {
        TextView textView = findViewById(textViewId);
        ImageView logoIcon = findViewById(logoId);

        if (textView != null && logoIcon != null) {
            textView.setOnClickListener(v -> {
                Intent intent = new Intent(this, targetClass);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, logoIcon, "shared_logo");
                startActivity(intent, options.toBundle());
            });
        }
    }

    protected void goToNotes() {
        startActivity(new Intent(this, NotesActivity.class));
        finish();
    }

}

