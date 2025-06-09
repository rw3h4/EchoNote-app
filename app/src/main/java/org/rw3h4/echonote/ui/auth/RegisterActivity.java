package org.rw3h4.echonote.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import org.rw3h4.echonote.R;
import org.rw3h4.echonote.ui.base.BaseActivity;
import org.rw3h4.echonote.util.auth.AuthUtils;
import org.rw3h4.echonote.util.auth.GoogleSignInHelper;

public class RegisterActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInHelper googleSignInHelper;
    boolean isReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        googleSignInHelper = new GoogleSignInHelper(this);

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

        ImageView closeButton = findViewById(R.id.ic_register_close);
        closeButton.setOnClickListener(v -> supportFinishAfterTransition());

        findViewById(R.id.register_login_cardView).setOnClickListener(
                v -> startGoogleSignIn()
        );

        findViewById(R.id.sign_up_button).setOnClickListener(
                v -> startEmailPasswordRegister()
        );

        isReady = true;

    }

    private void startGoogleSignIn() {
        googleSignInHelper.startGoogleSignIn(new GoogleSignInHelper.SignInCallback() {
            @Override
            public void onSignInSuccess() {
                goToNotes();
            }

            @Override
            public void onSignInFailure(String errorMessage) {
                AuthUtils.showToast(RegisterActivity.this, errorMessage);
            }
        }, getString(R.string.web_client_id));
    }

    private void startEmailPasswordRegister() {
        TextInputEditText nameInput = findViewById(R.id.fname_textInput);
        TextInputEditText emailInput = findViewById(R.id.register_email_textInput);
        TextInputEditText passwordInput = findViewById(R.id.register_password_textInput);
        TextInputEditText confirmPasswordInput = findViewById(R.id.confirm_password_textInput);

        if (nameInput != null &&
                emailInput != null &&
                passwordInput != null &&
                confirmPasswordInput != null &&
                !AuthUtils.isEmpty(nameInput.getText().toString().trim()) &&
                !AuthUtils.isEmpty(emailInput.getText().toString().trim()) &&
                !AuthUtils.isEmpty(passwordInput.getText().toString().trim()) &&
                !AuthUtils.isEmpty(confirmPasswordInput.getText().toString().trim())) {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            if (!password.equals(confirmPassword)) {
                AuthUtils.showToast(this, "Passwords do not match.");
                return;
            }

            if (password.length() < 6) {
                AuthUtils.showToast(this, "Password must be at least 6 characters long.");
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                    task -> {
                        if (task.isSuccessful()) {
                            goToNotes();
                        }  else {
                            AuthUtils.showToast(this, "Registration Failed: "
                                    + task.getException().getMessage());
                        }
                    });
        } else {
            AuthUtils.showToast(this, "Please fill all required fields.");
        }
    }
}
