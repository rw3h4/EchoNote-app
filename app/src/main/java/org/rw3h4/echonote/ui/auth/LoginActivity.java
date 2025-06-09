package org.rw3h4.echonote.ui.auth;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.rw3h4.echonote.R;
import org.rw3h4.echonote.util.auth.AuthUtils;
import org.rw3h4.echonote.util.auth.GoogleSignInHelper;
import org.rw3h4.echonote.ui.base.BaseActivity;

public class LoginActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInHelper googleSignInHelper;
    boolean isReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth =  FirebaseAuth.getInstance();
        googleSignInHelper = new GoogleSignInHelper(this);

        View mainContent = findViewById(R.id.main);
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

        splashScreen.setKeepOnScreenCondition(() -> !isReady);
        setupSharedElementTransition(R.id.ic_logo_image);
        setNavigationWithSharedElement(R.id.forgot_password_textView, PasswordResetActivity.class, R.id.ic_logo_image);
        setNavigationWithSharedElement(R.id.account_sign_text, RegisterActivity.class, R.id.ic_logo_image);

        findViewById(R.id.alt_login_cardView).setOnClickListener(v -> startGoogleSignIn());

        findViewById(R.id.login_button).setOnClickListener(v -> startEmailPasswordLogin());

        isReady = true;
    }

    private void startGoogleSignIn() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.isAnonymous()) {
            // Handle upgrading guest user to Google
            googleSignInHelper.linkAnonymousAccountToGoogle(
                    this, getString(R.string.web_client_id), () -> {
                AuthUtils.showToast(this, "Account linked successfully");
            });
        } else {
            googleSignInHelper.startGoogleSignIn(
                    new GoogleSignInHelper.SignInCallback() {
                        @Override
                        public void onSignInSuccess() {
                            goToNotes();
                        }

                        @Override
                        public void onSignInFailure(String errorMessage) {
                            AuthUtils.showToast(LoginActivity.this, errorMessage);
                        }
                    }, getString(R.string.web_client_id)
            );
        }
    }

    private void startEmailPasswordLogin() {
        TextInputEditText emailInput = findViewById(R.id.email_textInput);
        TextInputEditText passwordInput = findViewById(R.id.password_textInput);
        if (emailInput != null &&
                passwordInput != null &&
                !AuthUtils.isEmpty(emailInput.getText().toString())
                && !AuthUtils.isEmpty(passwordInput.getText().toString())) {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && currentUser.isAnonymous()) {
                // Handle guest user upgrade to email/password authentication
                AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                currentUser.linkWithCredential(credential).addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                AuthUtils.showToast(this, "Account linked successfully");
                                goToNotes();
                            } else {
                                AuthUtils.showToast(this, "Linking failed: "
                                        + task.getException().getMessage());
                            }
                        });
            } else {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                goToNotes();
                            } else {
                                AuthUtils.showToast(this, "Login Failed: "
                                        + task.getException().getMessage());
                            }
                        });
            }
        } else {
            AuthUtils.showToast(this, "Please enter Email and Password.");
        }
    }
}