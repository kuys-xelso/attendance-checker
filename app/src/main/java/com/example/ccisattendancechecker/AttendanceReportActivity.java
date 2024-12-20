package com.example.ccisattendancechecker;


import android.Manifest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;


import android.os.Bundle;
import android.os.Environment;

import android.util.Log;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AttendanceReportActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 123;
    TextView yrsection;
    private String eventName;
    private String eventId;
    private String dateCreated;
    private String section;
    private String course;
    private String yrLevel;
    private ProgressBar progressBar;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    PresentFragment presentFragment = new PresentFragment();


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_attendance_report);

        //get current user email
        String emailUser = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        fetchAndHandleUserDetails(emailUser);

        progressBar = findViewById(R.id.progressBar);
        ImageView printButton = findViewById(R.id.printButton);
        ImageView backButton = findViewById(R.id.backButton);
        TextView eventNameView = findViewById(R.id.eventNameReport);
        yrsection = findViewById(R.id.Yrsection);

        // Retrieve data from intent
        eventName = getIntent().getStringExtra("eventName");
        eventId = getIntent().getStringExtra("eventId");
        dateCreated = getIntent().getStringExtra("dateCreated");


        // Pass data to fragments
        Bundle args = new Bundle();
        args.putString("eventName", eventName);
        args.putString("eventId", eventId);
        presentFragment.setArguments(args);


        // Display event name or fallback
        if (eventName != null) {
            eventNameView.setText(eventName);

        } else {
            eventNameView.setText("No event name found");
            Toast.makeText(this, "No id or event name found", Toast.LENGTH_SHORT).show();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.container, presentFragment).commit();


        //generate pdf and printing button
        printButton.setOnClickListener(view -> {
            if (eventId != null && eventName != null) {
                checkPermissions();
            } else {
                Toast.makeText(AttendanceReportActivity.this, "Event data missing. Cannot generate report.", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Back button
        backButton.setOnClickListener(view -> finish());
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

                        if (section != null && course != null && yrLevel != null) {

                            yrsection.setText(course + " " + yrLevel + " - " + section);
                            // Further processing can be done here
                        } else {
                            Log.e("UserDetails", "Missing required fields in user document");
                        }
                    } else {
                        Log.e("UserDetails", "No matching user found");
                    }
                })
                .addOnFailureListener(e -> Log.e("FetchUserDetails", "Error fetching user details", e));
    }


//generating report in pdf and ready for printing
    private void generateReport(String eventName, String eventId) {

        progressBar.setVisibility(View.VISIBLE);
        if (section != null) {
            db.collection("attendance")
                    .document(eventId)
                    .collection("attendees")
                    .get()
                    .addOnSuccessListener(attendeesSnapshot -> {
                        Set<String> presentStudentIds = new HashSet<>();
                        for (DocumentSnapshot attendeeDoc : attendeesSnapshot.getDocuments()) {
                            String studentId = attendeeDoc.getString("student_id");
                            if (studentId != null) {
                                presentStudentIds.add(studentId);
                            }
                        }

                        db.collection("users")
                                .whereEqualTo("section", section)
                                .whereEqualTo("course", course)
                                .whereEqualTo("year_level", yrLevel)
                                .get()
                                .addOnSuccessListener(studentsSnapshot -> {
                                    List<String[]> studentData = new ArrayList<>();
                                    for (DocumentSnapshot studentDoc : studentsSnapshot.getDocuments()) {
                                        String lastName = studentDoc.getString("lastname");
                                        String firstName = studentDoc.getString("firstname");
                                        String middleName = studentDoc.getString("middle_initial");
                                        String studentId = studentDoc.getString("student_id");

                                        if (lastName != null && firstName != null && studentId != null) {
                                            String middleInitial = (middleName != null && !middleName.isEmpty())
                                                    ? middleName.substring(0, 1) + "."
                                                    : "";
                                            String fullName = lastName + ", " + firstName + " " + middleInitial;

                                            String status = presentStudentIds.contains(studentId) ? "P" : "";
                                            studentData.add(new String[]{fullName, status});
                                        }
                                    }

                                    Collections.sort(studentData, (o1, o2) -> o1[0].compareToIgnoreCase(o2[0]));


                                    try {

                                        progressBar.setVisibility(View.GONE);

                                        // Generate file name with a timestamp
                                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                                        String fileName = "Attendance_" + eventName.replaceAll("\\s+", "_") + "_" + timeStamp + ".pdf";

                                        // Get Downloads directory to save the PDF
                                        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                                        File pdfFile = new File(downloadsDir, fileName);

                                        // Open the template from assets
                                        AssetManager assetManager = getAssets();
                                        InputStream inputStream = assetManager.open("attendance.pdf");

                                        // Create PdfReader and PdfStamper
                                        PdfReader reader = new PdfReader(inputStream);
                                        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(pdfFile));

                                        // Get content to modify
                                        PdfContentByte canvas = stamper.getOverContent(1);

                                        // Add Title
                                        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
                                        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                                                new Phrase("ATTENDANCE REPORT", titleFont), 297.5f, 700, 0);

                                        // Add Event Name
                                        Font eventFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
                                        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                                                new Phrase(eventName, eventFont), 297.5f, 680, 0);

                                        // Add Department
                                        Font departmentFont = new Font(Font.FontFamily.HELVETICA, 12);
                                        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                                                new Phrase("College of Computing and Information Sciences", departmentFont), 297.5f, 665, 0);

                                        // Add Department
                                        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 12);
                                        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                                                new Phrase(course + " " + yrLevel + " - " + section, sectionFont), 297.5f, 650, 0);

                                        // Add date created
                                        Font dateFont = new Font(Font.FontFamily.HELVETICA, 12);
                                        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                                                new Phrase(dateCreated, dateFont), 297.5f, 630, 0);


                                        // Add a Table
                                        PdfPTable table = new PdfPTable(2); // Two columns: "Student Name" and "Status"
                                        table.setTotalWidth(500);
                                        table.setLockedWidth(true);
                                        table.setHorizontalAlignment(Element.ALIGN_CENTER);

                                        // Add table headers
                                        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
                                        PdfPCell header1 = new PdfPCell(new Phrase("Student Name", headerFont));
                                        PdfPCell header2 = new PdfPCell(new Phrase("Status", headerFont));
                                        header1.setHorizontalAlignment(Element.ALIGN_CENTER);
                                        header2.setHorizontalAlignment(Element.ALIGN_CENTER);
                                        header1.setBackgroundColor(BaseColor.LIGHT_GRAY);
                                        header2.setBackgroundColor(BaseColor.LIGHT_GRAY);
                                        table.addCell(header1);
                                        table.addCell(header2);

                                        // Add student data rows
                                        Font cellFont = new Font(Font.FontFamily.HELVETICA, 11);
                                        int presentCount = 0;
                                        for (String[] student : studentData) {
                                            PdfPCell nameCell = new PdfPCell(new Phrase(student[0], cellFont));
                                            PdfPCell statusCell = new PdfPCell(new Phrase(student[1], cellFont));
                                            nameCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                                            statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                            table.addCell(nameCell);
                                            table.addCell(statusCell);

                                            if (student[1].equalsIgnoreCase("P")) {
                                                presentCount++;
                                            }
                                        }

                                        // Determine table height dynamically
                                        float tableHeight = table.calculateHeights();
                                        float tableStartY = 615; // Y-coordinate where the table starts
                                        float summaryStartY = tableStartY - tableHeight - 20;

                                        // Draw the table at a specific position
                                        table.writeSelectedRows(0, -1, 50, tableStartY, canvas);

                                        // Add Summary below the table
                                        int totalStudents = studentData.size();
                                        int absentCount = totalStudents - presentCount;


                                        Font summaryFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
                                        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                                                new Phrase("Summary:", summaryFont), 50, summaryStartY, 0);
                                        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                                                new Phrase("Total Students: " + totalStudents, cellFont), 50, summaryStartY - 20, 0);
                                        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                                                new Phrase("Present: " + presentCount, cellFont), 50, summaryStartY - 40, 0);
                                        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                                                new Phrase("Absent: " + absentCount, cellFont), 50, summaryStartY - 60, 0);

                                        stamper.close();
                                        reader.close();

                                        // Show success message with the file location
                                        Toast.makeText(this, "PDF saved in Downloads: " + fileName, Toast.LENGTH_LONG).show();

                                        // Open the generated PDF in PdfViewerActivity
                                        Intent intent = new Intent(this, PdfViewerActivity.class);
                                        intent.putExtra("pdfFilePath", pdfFile.getAbsolutePath());
                                        startActivity(intent);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(this, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }


                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(AttendanceReportActivity.this, "Failed to fetch students", Toast.LENGTH_SHORT).show();
                                    Log.e("AttendanceReport", "Error fetching students", e);
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AttendanceReportActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
                        Log.e("AttendanceReport", "Error fetching attendance data", e);
                    });
        } else {
            Toast.makeText(this, "Section data is missing for the mayor", Toast.LENGTH_SHORT).show();
        }
    }

//check permission method
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        } else {
            generateReport(eventName, eventId);
        }
    }

//on request permission result method
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generateReport(eventName,eventId);
            } else {
                Toast.makeText(this, "Permission denied. Cannot generate PDF.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

}

