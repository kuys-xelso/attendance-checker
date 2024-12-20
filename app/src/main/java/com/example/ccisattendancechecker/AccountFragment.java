package com.example.ccisattendancechecker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class AccountFragment extends Fragment {

    private ProgressBar progressBar;
    private ShapeableImageView profileImage;
    private TextView usernameTextview;
    private TextView userRoleTextView;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_account, container, false);

        progressBar = view.findViewById(R.id.progressBar);
        ImageButton signOutBtn = view.findViewById(R.id.signOutBtn);
        usernameTextview = view.findViewById(R.id.nameTextview);
        userRoleTextView = view.findViewById(R.id.roleTextview);
        profileImage = view.findViewById(R.id.profilePicImageView);


        Bundle bundle = getArguments();
        if (bundle != null) {
            String fname = bundle.getString("fname");
            String lname = bundle.getString("lname");
            String middleInitial = bundle.getString("middleInitial");
            String role = bundle.getString("userRole");
            String yearLevel = bundle.getString("yearLevel");
            String section = bundle.getString("section");
            String email = bundle.getString("email");
            String profileUrl = bundle.getString("profileUrl");
            loadProfilePicture(profileUrl);

            usernameTextview.setText(fname + " " + middleInitial + ". " + lname);
            userRoleTextView.setText(role);

        }

        RelativeLayout changepassMenu = view.findViewById(R.id.changePassLayout);
        RelativeLayout  deleteAccountMenu =  view.findViewById(R.id. deleteAccountLayout);

        //going to change pass activity
        changepassMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
                startActivity(intent);
            }
        });


        //delete Account
        deleteAccountMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a password input field
                final EditText passwordInput = new EditText(view.getContext());
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); // Password input
                passwordInput.setHint("Enter your password");

                // Wrap the EditText in a FrameLayout to add margins
                FrameLayout container = new FrameLayout(view.getContext());
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(40, 20, 40, 20); // Margins (left, top, right, bottom)
                passwordInput.setLayoutParams(params);
                container.addView(passwordInput);

                // show progress bar
                progressBar.setVisibility(View.GONE);
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Confirm Deletion")
                        .setMessage("Enter your password to delete your account.")
                        .setView(container)
                        .setPositiveButton("Delete", (dialogInterface, i) -> {
                            String enteredPassword = passwordInput.getText().toString().trim();
                            if (enteredPassword.isEmpty()) {
                                Toast.makeText(view.getContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            progressBar.setVisibility(View.VISIBLE); // Show progress bar

                            FirebaseAuth auth = FirebaseAuth.getInstance();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            FirebaseUser user = auth.getCurrentUser();

                            if (user != null) {
                                String email = user.getEmail();
                                AuthCredential credential = EmailAuthProvider.getCredential(email, enteredPassword);

                                // Reauthenticate the user
                                user.reauthenticate(credential).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Fetch the Firestore document
                                        db.collection("users").whereEqualTo("email", email).get()
                                                .addOnCompleteListener(queryTask -> {
                                                    if (queryTask.isSuccessful() && !queryTask.getResult().isEmpty()) {
                                                        DocumentReference userDocRef = queryTask.getResult()
                                                                .getDocuments().get(0).getReference();

                                                        // Update Firestore fields
                                                        Map<String, Object> updates = new HashMap<>();
                                                        updates.put("email", FieldValue.delete());
                                                        updates.put("user_role", "Student");
                                                        updates.put("profile_pic_url", FieldValue.delete());

                                                        userDocRef.update(updates).addOnCompleteListener(updateTask -> {
                                                            if (updateTask.isSuccessful()) {
                                                                // Delete the user
                                                                user.delete().addOnCompleteListener(deleteTask -> {
                                                                    progressBar.setVisibility(View.GONE); // Hide progress bar
                                                                    if (deleteTask.isSuccessful()) {
                                                                        Toast.makeText(view.getContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                                                        view.getContext().startActivity(new Intent(view.getContext(), LoginActivity.class));
                                                                        requireActivity().finish();
                                                                    } else {
                                                                        Toast.makeText(view.getContext(), "Failed to delete account. Try again.", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                            } else {
                                                                progressBar.setVisibility(View.GONE); // Hide progress bar
                                                                Toast.makeText(view.getContext(), "Failed to update Firestore.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    } else {
                                                        progressBar.setVisibility(View.GONE); // Hide progress bar
                                                        Toast.makeText(view.getContext(), "User data not found in Firestore.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        progressBar.setVisibility(View.GONE); // Hide progress bar
                                        Toast.makeText(view.getContext(), "Incorrect password. Try again.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                        .show();
            }
        });


        //logout button
        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Confirm Logout")
                        .setMessage("Are you sure you want to Logout?")
                        .setPositiveButton("Yes", (dialogInterface, i) -> {

                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(getActivity(), "Logout Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getActivity(),LoginActivity.class));
                            requireActivity().finish();

                        })
                        .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                        .show();
            }
        });

       return view;
    }


    private void loadProfilePicture(String profilePicUrl) {

        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
            // Load image using Glide
            Glide.with(this)
                    .load(profilePicUrl)
                    .placeholder(R.drawable.avatar)
                    .error(R.drawable.avatar)
                    .circleCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(profileImage);
        } else {
            // Set default profile picture if no URL is found
            profileImage.setImageResource(R.drawable.avatar);
        }
    }

}