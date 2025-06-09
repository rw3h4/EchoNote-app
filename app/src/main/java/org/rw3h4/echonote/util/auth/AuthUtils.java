package org.rw3h4.echonote.util.auth;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthUtils {
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static boolean isEmpty(String input) {
        return input ==  null || input.trim().isEmpty();
    }

    public static void linkAnonymousAccountToEmailPassword(Activity activity, String email,
                                                           String password, Runnable onSuccess
    ) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

        if (user != null && user.isAnonymous()) {
            user.linkWithCredential(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    onSuccess.run();
                } else {
                    showToast(activity, "Linking failed: " + task.getException()
                            .getMessage());
                }
            });
        }
    }
}
