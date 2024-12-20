package com.example.ccisattendancechecker;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PresentFragment extends Fragment {
    private String eventName;
    private String eventId;
    private List<StudentNameClass> studentNames;
    private ListenerRegistration listenerRegistration;
    private PresentStudentAdapter presentAdapter;
    TextView nostudents;
    private RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve arguments using getArguments()
        if (getArguments() != null) {
            eventName = getArguments().getString("eventName");
            eventId = getArguments().getString("eventId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_present, container, false);

        nostudents = view.findViewById(R.id.noStudentsText);
        recyclerView = view.findViewById(R.id.present_recyclerView);
        studentNames = new ArrayList<>();
        presentAdapter = new PresentStudentAdapter( studentNames);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(presentAdapter);




        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        // Fetch data every time the fragment becomes visible
        if (eventId != null) {
            fetchPresentStudents();
        } else {
            Log.e("PresentFragment", "Event ID is null. Cannot fetch data.");
        }
    }

    private void fetchPresentStudents() {
        if (eventId == null) {
            Log.e("PresentFragment", "Event ID is null.");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Attach a listener to monitor real-time updates
        db.collection("attendance")
                .document(eventId)
                .collection("attendees")
                .whereEqualTo("status", "Present")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e("PresentFragment", "Error fetching data", error);
                        return;
                    }

                    if (querySnapshot != null) {
                        studentNames.clear();
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            String studentName = doc.getString("name");
                            String status = doc.getString("status");
                            studentNames.add(new StudentNameClass(studentName, status));
                        }


                        if (studentNames.isEmpty()) {
                            nostudents.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            nostudents.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }

                        presentAdapter.notifyDataSetChanged();
                    }
                });
    }


    @Override
    public void onPause() {
        super.onPause();
        // Detach Firestore listener to prevent memory leaks
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

}