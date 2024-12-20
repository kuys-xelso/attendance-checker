package com.example.ccisattendancechecker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class StudentFragment extends Fragment {


    Button generateButton;
    private EditText studentIdInput;
    private Spinner courseSpinner, yrSpinner, sectionSpinner;
    private static final String DELIMITER = "|| ";
    private static final String[] SECTION = {
            "Section","A","B", "C"
    };

    private static final String[] YEAR_LEVEL = {
            "Year Level","1", "2", "3","4"
    };

    private static final String[] COURSES = {
            "Select Course", "BSIT", "BSCS"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_student, container, false);

        studentIdInput = view.findViewById(R.id.studentIdEditText);
        courseSpinner = view.findViewById(R.id.courseSpinner);
        yrSpinner = view.findViewById(R.id.yrSpinner);
        sectionSpinner = view.findViewById(R.id.sectionSpinner);
        generateButton = view.findViewById(R.id.button_generate);

        setupYearSpinner();
        setupCourseSpinner();
        setupSectionSpinner();

        generateButton.setOnClickListener(view1 -> generateAttendanceQR());

        return view;

    }

    private void setupCourseSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), R.layout.spinner_item, COURSES
        );
        adapter.setDropDownViewResource(R.layout.spinner_item);
        courseSpinner.setAdapter(adapter);

        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // Hint selected
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.GRAY);
                } else {
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupYearSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, YEAR_LEVEL
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yrSpinner.setAdapter(adapter);

        yrSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // Hint selected
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.GRAY);
                } else {
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupSectionSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, SECTION
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sectionSpinner.setAdapter(adapter);

        sectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // Hint selected
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.GRAY);
                } else {
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void saveStudentInfo() {
        SharedPreferences.Editor editor = requireContext().getSharedPreferences("StudentPrefs", Context.MODE_PRIVATE).edit();
        editor.putString("studentId", studentIdInput.getText().toString());
        editor.putInt("yrSection", yrSpinner.getSelectedItemPosition());
        editor.putInt("yrSection", sectionSpinner.getSelectedItemPosition());
        editor.putInt("coursePosition", courseSpinner.getSelectedItemPosition());
        editor.apply();
    }

    private void generateAttendanceQR() {
        String studentId = studentIdInput.getText().toString().trim();
        String yrLevel = yrSpinner.getSelectedItem().toString();
        String section = sectionSpinner.getSelectedItem().toString();
        String course = courseSpinner.getSelectedItem().toString();

        if (studentId.isEmpty()|| courseSpinner.getSelectedItemPosition() == 0 || yrSpinner.getSelectedItemPosition() == 0  || sectionSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        saveStudentInfo();

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
        String qrContent = String.format("%s%s%s%s%s%s%s%s%s",
                studentId, DELIMITER,
                yrLevel, DELIMITER,
                section, DELIMITER,
                course, DELIMITER,
                timestamp);

        Intent intent = new Intent(requireContext(), StudentQrActivity.class);
        intent.putExtra("qrContent", qrContent);
        startActivity(intent);
        requireActivity().finish();

    }
}