package com.example.ccisattendancechecker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class QrScannerActivity extends AppCompatActivity {

    private String eventId;
    private CodeScanner codeScanner;
    private static final int CAMERA_REQUEST_CODE = 101;
    private String section;
    private String course;
    private String yrLevel;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_qr_scanner);

        eventId = getIntent().getStringExtra("eventId");

        String emailUser = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        fetchAndHandleUserDetails(emailUser);


        setupPermissions();
        setupScanner();

    }
        private void setupScanner() {
            CodeScannerView scannerView = findViewById(R.id.scanner_view);
            codeScanner = new CodeScanner(this, scannerView);


            // Scanner configuration
            codeScanner.setCamera(CodeScanner.CAMERA_BACK);
            codeScanner.setFormats(CodeScanner.ALL_FORMATS);
            codeScanner.setAutoFocusEnabled(true);
            codeScanner.setFlashEnabled(false);

            // Callbacks
            codeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
                String qrContent = result.getText();
                String DELIMITER = "\\|\\|";
                String [] parts = qrContent.split(DELIMITER);
                String studentId = parts[0];

                searchStudentInFirestore(studentId,eventId);

                Toast.makeText(this, "Scan result: " + qrContent , Toast.LENGTH_LONG).show();

            }));

            codeScanner.setErrorCallback(error -> runOnUiThread(() ->
                    Toast.makeText(this, "Camera initialization error: " + error.getMessage(),
                            Toast.LENGTH_LONG).show()));

            scannerView.setOnClickListener(view -> codeScanner.startPreview());
        }


    private void searchStudentInFirestore(String studentId, String eventId) {


        // Query Firestore for the student ID
        db.collection("users")
                .whereEqualTo("student_id", studentId)
                .whereEqualTo("course", course)
                .whereEqualTo("year_level", yrLevel)
                .whereEqualTo("section", section)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Student found
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String lname = document.getString("lastname");
                        String fname = document.getString("firstname");
                        String middleInitial = document.getString("middle_initial");

                        String fullname = lname + ", " + fname + ", " + middleInitial;

                        // Check if student is already in the attendance subcollection
                        db.collection("attendance")
                                .document(eventId)
                                .collection("attendees")
                                .whereEqualTo("student_id", studentId)
                                .get()
                                .addOnSuccessListener(attendeeSnapshots -> {
                                    if (!attendeeSnapshots.isEmpty()) {
                                        // Student already in attendance
                                        showAlreadyExistsDialog(fullname);
                                    } else {
                                        // Add student to the attendance subcollection
                                        Map<String, Object> attendeeData = new HashMap<>();
                                        attendeeData.put("name", fullname);
                                        attendeeData.put("student_id", studentId);
                                        attendeeData.put("status", "Present");
                                        attendeeData.put("time", FieldValue.serverTimestamp());

                                        db.collection("attendance")
                                                .document(eventId)
                                                .collection("attendees")
                                                .add(attendeeData)
                                                .addOnSuccessListener(aVoid -> {
                                                    // Show success dialog
                                                    showSuccessDialog(fullname);
                                                })
                                                .addOnFailureListener(e ->
                                                        Log.e("Firestore", "Error adding attendee", e)
                                                );
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Error checking existing attendees", e);
                                    Toast.makeText(this, "Error checking attendance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // No student found with this ID
                        Toast.makeText(this, "No student found with ID: " + studentId, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    Log.e("Firestore", "Error searching student", e);
                    Toast.makeText(this, "Error searching Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void fetchAndHandleUserDetails(String email) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(mayorSnapshot -> {
                    if (!mayorSnapshot.isEmpty()) {
                        DocumentSnapshot document = mayorSnapshot.getDocuments().get(0);
                        section = document.getString("section");
                        course = document.getString("course");
                        yrLevel = document.getString("year_level");

                    } else {
                        Log.e("UserDetails", "No matching user found");
                    }
                })
                .addOnFailureListener(e -> Log.e("FetchUserDetails", "Error fetching user details", e));
    }


    private void showSuccessDialog(String fullname) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Attendance Updated")
                .setMessage( fullname + " has been successfully added to the attendance.")
                .setPositiveButton("OK", (dialog, which) -> {
                    onResume();
                    dialog.dismiss();
                })
                .show();
    }

    // Method to show already exists dialog
    private void showAlreadyExistsDialog(String fullname) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Already Present")
                .setMessage( fullname + " is already marked as present in this attendance.")
                .setPositiveButton("OK", (dialog, which) -> {
                    onResume();
                    dialog.dismiss();
                })
                .show();
    }


    private void setupPermissions() {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_REQUEST_CODE);
            }
        }


        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == CAMERA_REQUEST_CODE) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Camera permission granted", Toast.LENGTH_LONG).show();
                    setupScanner();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }

        @Override
        protected void onResume() {
            super.onResume();
            if (codeScanner != null) {
                codeScanner.startPreview();
            }
        }

        @Override
        protected void onPause() {
            if (codeScanner != null) {
                codeScanner.releaseResources();
            }
            super.onPause();
        }

}