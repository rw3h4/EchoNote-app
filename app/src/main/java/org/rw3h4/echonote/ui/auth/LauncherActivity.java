package org.rw3h4.echonote.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.rw3h4.echonote.ui.note.NotesActivity;

/**
 * Made this the new main entry point for the application instead of
 * the LoginActivityy.
 * It serves to check the user's authentication state and naviagting to
 * the right screen
 * It has no UI
 */

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Registered non-guest user, taken to their notes.
        if (currentUser != null && !currentUser.isAnonymous()) {
            startActivity(new Intent(this, NotesActivity.class));
        } else {
            // If no user or user is guest, initiate login flow.
            // Guest mode will be handled by LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish();
    }
}