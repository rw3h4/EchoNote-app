package org.rw3h4.echonote.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import org.rw3h4.echonote.R;
import org.rw3h4.echonote.ui.base.BaseActivity;

public class PasswordResetActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    boolean isReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        mAuth =  FirebaseAuth.getInstance();

        setupSharedElementTransition(R.id.ic_logo_image);
        View mainContent = findViewById(android.R.id.content);
        setupWindowInsets(mainContent);

        mainContent.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (!isReady) {
                    return false;
                }

                mainContent.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });

        ImageView closeButton = findViewById(R.id.ic_reset_close);
        closeButton.setOnClickListener(v -> supportFinishAfterTransition());

        findViewById(R.id.submit_button).setOnClickListener( v -> startPasswordReset());

        findViewById(R.id.reset_pass_login_textView).setOnClickListener(v -> finish());

        isReady = true;

    }

    private void startPasswordReset() {
        TextInputEditText emailInput = findViewById(R.id.reset_email_textInput);

        if(emailInput != null && emailInput.getText() != null) {
            String email =  emailInput.getText().toString().trim();
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(
                    task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Password Reset Email Sent!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        } else {
            Toast.makeText(this, "Please enter your email.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}