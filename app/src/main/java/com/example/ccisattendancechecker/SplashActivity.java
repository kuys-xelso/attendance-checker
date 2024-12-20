package com.example.ccisattendancechecker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentUser != null) {

                    // If the user is logged in, navigate to the home screen
                    db.collection("users")
                            .whereEqualTo("email", currentUser.getEmail())
                            .get()
                            .addOnSuccessListener(documentSnapshots -> {
                                if (!documentSnapshots.isEmpty()){
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

                                   Intent intent = new Intent(SplashActivity.this, MainActivity.class); // Change this to your main activity
                                    intent.putExtra("fname", fname);
                                    intent.putExtra("lname", lname);
                                    intent.putExtra("middleInitial", middleInitial);
                                    intent.putExtra("userRole", userRole);
                                    intent.putExtra("yearLevel", yearLevel);
                                    intent.putExtra("section", section);
                                    intent.putExtra("email", emailUser);
                                    intent.putExtra("course", course);
                                    intent.putExtra("profileUrl", profileUrl);
                                    startActivity(intent);
                                    finish();

                                }
                            });

                         } else {
                    // If the user is not logged in, navigate to the login screen
                   Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        }, 1000);

    }
}