package org.rw3h4.echonote.ui.note;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.rw3h4.echonote.R;

public class NotesActivity extends AppCompatActivity {

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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        TextView welcomeText = findViewById(R.id.welcome_user_textView);

        if (user != null) {
            if (user.isAnonymous()) {
                welcomeText.setText("Hi, Guest User");
            } else {
                String name = user.getDisplayName();
                String email = user.getEmail();
                if (name != null && !name.isEmpty()) {
                    welcomeText.setText("Hi, " + name);
                } else if (email != null) {
                    welcomeText.setText("Hi, " + email);
                }
            }
        }

    }
}