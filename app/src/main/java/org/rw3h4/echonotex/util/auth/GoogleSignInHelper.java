package org.rw3h4.echonotex.util.auth;

import android.app.Activity;
import android.content.Context;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class GoogleSignInHelper {
    private static final String TAG = "GoogleSignInHelper";
    private final Context context;
    private final CredentialManager credentialManager;
    private final FirebaseAuth mAuth;
    private final ExecutorService executor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface SignInCallback {
        void onSignInSuccess();
        void onSignInFailure(String errorMessage);
    }

    private final Activity activity;

    public GoogleSignInHelper(Activity activity) {
        this.activity = activity;
        this.context = activity;
        this.credentialManager = CredentialManager.create(context);
        this.mAuth = FirebaseAuth.getInstance();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void startGoogleSignIn(SignInCallback callback, String webClientId) {

        GetGoogleIdOption option = new GetGoogleIdOption
                .Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setNonce(createNonce())
                .build();

        GetCredentialRequest request = new GetCredentialRequest
                .Builder()
                .addCredentialOption(option)
                .build();

        credentialManager.getCredentialAsync(
                context,
                request,
                new CancellationSignal(),
                executor,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleCredential(result.getCredential(), callback);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        String message = e.getLocalizedMessage();
                        Log.e(TAG, "GetCredential error: " + message);

                        if (message != null && message.toLowerCase().contains("cancel")) {
                            mainHandler.post(() -> callback.onSignInFailure("Sign-in cancelled by user"));
                        } else {
                            mainHandler.post(() -> callback.onSignInFailure("Google sign-in failed " + message));
                        }
                    }
                }
        );
    }

    public void linkAnonymousAccountToGoogle(Activity activity, String webClientId, Runnable onSuccess) {
        GetGoogleIdOption option = new GetGoogleIdOption
                .Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setNonce(createNonce())
                .build();

        GetCredentialRequest request = new GetCredentialRequest
                .Builder()
                .addCredentialOption(option)
                .build();

        credentialManager.getCredentialAsync(
                context,
                request,
                new CancellationSignal(),
                executor,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        Credential credential = result.getCredential();
                        if (credential instanceof GoogleIdTokenCredential) {
                            GoogleIdTokenCredential googleIdTokenCredential = (GoogleIdTokenCredential) credential;
                            String idToken = googleIdTokenCredential.getIdToken();
                            AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null && user.isAnonymous()) {
                                user.linkWithCredential(firebaseCredential).addOnCompleteListener(
                                        task -> {
                                            if (task.isSuccessful()) {
                                                mainHandler.post(onSuccess);
                                            } else {
                                                AuthUtils.showToast(activity,
                                                        "Linking failed: "
                                                                + task.getException().getMessage()
                                                );
                                            }
                                        });
                            }
                        } else {
                            AuthUtils.showToast(activity, "Unexpected credential type.");
                        }
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        AuthUtils.showToast(activity, "Google link failed: " + e.getMessage());
                    }
                }
        );
    }

    private void handleCredential(Credential credential, SignInCallback callback) {
        if (credential instanceof GoogleIdTokenCredential) {
            GoogleIdTokenCredential googleIdTokenCredential = (GoogleIdTokenCredential) credential;
            String idToken = googleIdTokenCredential.getIdToken();
            AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);

            mAuth.signInWithCredential(firebaseCredential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    mainHandler.post(callback::onSignInSuccess);
                } else {
                    Exception e = task.getException();
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        mainHandler.post(() -> callback.onSignInFailure(
                                "Account exists with different sign-in method."
                        ));
                    } else {
                        mainHandler.post(() -> callback.onSignInFailure(
                                "Google sign-in failed: " + e.getMessage()
                        ));
                    }
                }
            });
        } else {
            mainHandler.post(() -> callback.onSignInFailure(
                    "Unexpected credential type: "
            ));
        }
    }

    private String createNonce() {
        try {
            String rawNonce = UUID.randomUUID().toString();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(rawNonce.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "SHA-256 not supported, fallback to raw nonce");
            return UUID.randomUUID().toString();
        }
    }
}

