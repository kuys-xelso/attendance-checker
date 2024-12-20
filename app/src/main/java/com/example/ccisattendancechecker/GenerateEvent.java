package com.example.ccisattendancechecker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class GenerateEvent extends AppCompatActivity {

    private EditText eventNameEditText;
    private TimePicker timePicker;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_generate_qr);

        //initialization
        progressBar = findViewById(R.id.progressBar);
        eventNameEditText = findViewById(R.id.eventNameEditText);
        timePicker = findViewById(R.id.timePicker);
        ImageButton imageButtonBack = findViewById(R.id.backButton);

        imageButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //initialize generate qr button
        Button generateQrButton = findViewById(R.id.generateQrButton);

        generateQrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                generateEvent();
            }
        });

    }


    private void generateEvent() {
        String eventName = eventNameEditText.getText().toString().trim();

        // Check if event name is empty
        if (eventName.isEmpty()) {
            Toast.makeText(GenerateEvent.this, "Please enter event name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get time from TimePicker
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        // Get current time
        Calendar now = Calendar.getInstance();

        // Set up Calendar and format cut-off time
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        // Validate cut-off time
        if (calendar.before(now)) {
            Toast.makeText(GenerateEvent.this, "Please select a future time", Toast.LENGTH_SHORT).show();
            return;
        }

        Date cutOffTime = calendar.getTime();

        Timestamp dateCreatedTimestamp = new Timestamp(new Date());

        progressBar.setVisibility(View.VISIBLE);
        // Get the email of the current user (fallback to "Unknown" if not authenticated)
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String createdBy = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "Unknown";

        // Prepare attendance data for Firestore
        Map<String, Object> attendanceData = new HashMap<>();
        attendanceData.put("eventName", eventName);
        attendanceData.put("cutOffTime", cutOffTime);
        attendanceData.put("dateCreated", dateCreatedTimestamp); // Store date as string
        attendanceData.put("createdBy", createdBy);

        // Save attendance data to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("attendance")
                .add(attendanceData)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);

                    CollectionReference attendeesRef = documentReference.collection("attendees");
                    String email = auth.getCurrentUser().getEmail();

                    if (email != null) {
                        db.collection("users")
                                .whereEqualTo("email", email)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {

                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                                        String lname = document.getString("lastname");
                                        String fname = document.getString("firstname");
                                        String studentId = document.getString("student_id");
                                        String middleInitial = document.getString("middle_initial");

                                        String fullname = lname + ", " + fname + ", " + middleInitial;

                                        Map<String, Object> attendeeData = new HashMap<>();
                                        attendeeData.put("name", fullname);
                                        attendeeData.put("student_id", studentId);
                                        attendeeData.put("status", "Present");
                                        attendeeData.put("time", FieldValue.serverTimestamp());

                                        attendeesRef.add(attendeeData)
                                                .addOnSuccessListener(attendeeRef -> {
                                                    // Success message
                                                    Toast.makeText(GenerateEvent.this, "Event generated successfully", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                });
                                    }
                                });
                    }

                    finish();
                })
                .addOnFailureListener(e -> {
                    // Display error message
                    Toast.makeText(GenerateEvent.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }
}
