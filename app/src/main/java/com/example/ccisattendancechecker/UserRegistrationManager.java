package com.example.ccisattendancechecker;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.HashMap;
import java.util.Map;

public class UserRegistrationManager {
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public interface RegistrationCallback {
        void onSuccess();
        void onFailure(String e);
    }

    public UserRegistrationManager() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public void checkAndRegisterUser(String studentId, String email, String password,
                                     RegistrationCallback callback) {
        // First check if student ID exists and if it already has an account
        db.collection("users")
                .whereEqualTo("student_id", studentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();

                        if (querySnapshot.isEmpty()) {
                            // Student ID not found in database
                            callback.onFailure("Student ID not found in the system");
                            return;
                        }

                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        String existingEmail = document.getString("email");

                        if (existingEmail != null && !existingEmail.isEmpty()) {
                            // User already has an account
                            callback.onFailure("This student ID is already registered. Please login instead.");
                            return;
                        }

                        // Check if email is already used by another student
                        checkEmailAvailability(email, available -> {
                            if (available) {
                                // Proceed with registration
                                proceedWithRegistration(document.getId(), email, password, callback);
                            } else {
                                callback.onFailure("This email is already registered with another account");
                            }
                        });

                    } else {
                        callback.onFailure("Error checking student ID: " +
                                task.getException().getMessage());
                    }
                });
    }

    private void checkEmailAvailability(String email, EmailAvailabilityCallback callback) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onResult(task.getResult().isEmpty());
                    } else {
                        callback.onResult(false);
                    }
                });
    }

    private interface EmailAvailabilityCallback {
        void onResult(boolean isAvailable);
    }

    private void proceedWithRegistration(String docId, String email, String password,
                                         RegistrationCallback callback) {
        // Create Firebase Auth account
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        assert user != null;
                        String id = user.getUid();

                        //set avatar
                        String avatarUrl = getRandomAvatarUrl(id);

                        // Update Firestore document with email, user role, and profile picture URL
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("email", email);
                        updates.put("user_role", "Class Mayor");
                        updates.put("profile_pic_url", avatarUrl);

                        db.collection("users")
                                .document(docId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> callback.onSuccess())
                                .addOnFailureListener(e -> {
                                    // If Firestore update fails, delete the auth account
                                    if (auth.getCurrentUser() != null) {
                                        auth.getCurrentUser().delete();
                                    }
                                    callback.onFailure("Failed to update user data: " +
                                            e.getMessage());
                                });
                    } else {
                        Exception exception = task.getException();
                        String errorMessage = "Failed to create account";

                        if (exception instanceof FirebaseAuthWeakPasswordException) {
                            errorMessage = "Password is too weak. Please use at least 6 characters";
                        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            errorMessage = "Invalid email format";
                        } else if (exception instanceof FirebaseAuthUserCollisionException) {
                            errorMessage = "This email is already registered";
                        }

                        callback.onFailure(errorMessage);
                    }
                });
    }


    private String getRandomAvatarUrl(String email) {
        // Use email as seed to always get the same avatar for the same email
        String seed = email.toLowerCase().trim();

        // You can choose different styles:
        // - pixel-art: 8-bit style
        // - bottts: robot style
        // - adventurer: human cartoon style
        // - lorelei: minimalistic style
        // - avataaars: customizable human avatars
        return "https://api.dicebear.com/7.x/thumbs/png?seed=" + seed;
    }


}
