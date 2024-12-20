package com.example.ccisattendancechecker;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class MayorFragment extends Fragment {

    private TextView forgetPass;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private EditText emailEditText;
    private EditText passwordEditText;
    private boolean isPasswordVisible = false;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mayor, container, false);

        auth = FirebaseAuth.getInstance();
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText =  view.findViewById(R.id.passwordEditText);
        Button button_login =  view.findViewById(R.id.button_login);
        progressBar =  view.findViewById(R.id.progressBar);
        forgetPass =  view.findViewById(R.id.forgetPassLink);


        // Navigate to the signup activity
        TextView registerLink = view.findViewById(R.id.registerLink);
        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), CreateAccountActivity.class));
        });


        passwordEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // Check if the touch is within the drawable area (right side of the EditText)
                Drawable drawableEnd = passwordEditText.getCompoundDrawables()[2];
                if (drawableEnd != null) {
                    int drawableWidth = drawableEnd.getBounds().width();
                    if (event.getRawX() >= (passwordEditText.getRight() - drawableWidth - passwordEditText.getPaddingEnd())) {
                        togglePasswordVisibility();
                        return true;
                    }
                }
            }
            return false;
        });

        // Set a focus change listener to automatically hide the password when it loses focus
        passwordEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hidePassword(); // Automatically hide the password on focus loss
            }
        });

        // Handle the login button click
        button_login.setOnClickListener(v -> login());

        forgetPass.setOnClickListener(view1 -> {
            // Create an input dialog for the user to enter their email
            final EditText emailInput = new EditText(getContext());
            emailInput.setHint("Enter your registered email");
            emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

            // Wrap the EditText in a FrameLayout to add margins
            FrameLayout container1 = new FrameLayout(view.getContext());
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(40, 20, 40, 20);
            emailInput.setLayoutParams(params);
            container1.addView(emailInput);

            new AlertDialog.Builder(requireContext())
                    .setTitle("Forgot Password")
                    .setMessage("Enter your registered email address to receive a password reset link.")
                    .setView(container1)
                    .setPositiveButton("Send", (dialog, which) -> {
                        String email = emailInput.getText().toString().trim();
                        if (email.isEmpty()) {
                            Toast.makeText(getContext(), "Email field cannot be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        sendPasswordResetEmail(email);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        return view;
    }

    private void login() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(getContext(), "Email is required", Toast.LENGTH_SHORT).show();
            emailEditText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(getContext(), "Password is required", Toast.LENGTH_SHORT).show();
            passwordEditText.requestFocus();
            return;
        }

        // Show the progress bar
        progressBar.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    // Hide the progress bar
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        //check if the user is verified
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String userEmail = user.getEmail();
                            db.collection("users")
                                    .whereEqualTo("email", userEmail)
                                    .get()
                                    .addOnSuccessListener(documentSnapshots -> {
                                        if (!documentSnapshots.isEmpty()) {
                                            DocumentSnapshot documentSnapshot = documentSnapshots.getDocuments().get(0);
                                            String fname = documentSnapshot.getString("firstname");
                                            String lname = documentSnapshot.getString("lastname");
                                            String middleInitial = documentSnapshot.getString("middle_initial");
                                            String userRole = documentSnapshot.getString("user_role");
                                            String yearLevel = documentSnapshot.getString("year_level");
                                            String section = documentSnapshot.getString("section");
                                            String emailUser = documentSnapshot.getString("email");
                                            String course = documentSnapshot.getString("course");
                                            String profileUrl = documentSnapshot.getString("profile_pic_url");

                                            // Pass the data to the MainActivity
                                            Intent intent = new Intent(getContext(), MainActivity.class);
                                            intent.putExtra("fname", fname);
                                            intent.putExtra("lname", lname);
                                            intent.putExtra("middleInitial", middleInitial);
                                            intent.putExtra("userRole", userRole);
                                            intent.putExtra("yearLevel", yearLevel);
                                            intent.putExtra("section", section);
                                            intent.putExtra("email", emailUser);
                                            intent.putExtra("course", course);
                                            intent.putExtra("profileUrl", profileUrl);

                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);


                                            Toast.makeText(getContext(), "Login successful", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                                        }

                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("LoginActivity", "Error fetching user data", e);
                                        Toast.makeText(getContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        // Handle login failure
                        Exception exception = task.getException();
                        if (exception != null) {
                            Log.e("LoginActivity", "Login failed", exception);
                            Toast.makeText(getContext(), "Login failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }


    private void sendPasswordResetEmail(String email) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Password reset email sent. Check your inbox.", Toast.LENGTH_SHORT).show();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Error occurred";
                        Toast.makeText(getContext(), "Failed to send password reset email: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        updatePasswordVisibility();
    }

    private void hidePassword() {
        if (isPasswordVisible) {
            isPasswordVisible = false;
            updatePasswordVisibility();
        }
    }

    private void updatePasswordVisibility() {
        Drawable startDrawable = passwordEditText.getCompoundDrawables()[0]; // Retain left drawable
        @SuppressLint("UseCompatLoadingForDrawables") Drawable endDrawable = isPasswordVisible
                ? getResources().getDrawable(R.drawable.icon_visibility_on)
                : getResources().getDrawable(R.drawable.icon_visibility_off);

        passwordEditText.setInputType(isPasswordVisible
                ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(startDrawable, null, endDrawable, null);

        // Move the cursor to the end of the text
        passwordEditText.setSelection(passwordEditText.getText().length());
    }

}