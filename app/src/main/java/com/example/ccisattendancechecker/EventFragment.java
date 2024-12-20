package com.example.ccisattendancechecker;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;


public class EventFragment extends Fragment {

    private RecyclerView recyclerView;
    private OngoingEventAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<OngoingEvent> ongoingEvents;
    private TextView noOngoingEvents;
    private ListenerRegistration firestoreListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event, container, false);

        auth = FirebaseAuth.getInstance();

        noOngoingEvents = view.findViewById(R.id.noEventsText);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OngoingEventAdapter(getContext());
        recyclerView.setAdapter(adapter);

       db = FirebaseFirestore.getInstance();

        loadEvents();

//        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);


        return view;
    }

    private void loadEvents() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "No user signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = currentUser.getEmail();

        // Remove the listener if already set
        if (firestoreListener != null) {
            firestoreListener.remove();
        }

        // Query Firestore for ongoing events
        firestoreListener = db.collection("attendance")
                .whereEqualTo("createdBy", userEmail)
                .whereGreaterThan("cutOffTime", Timestamp.now())
                .orderBy("cutOffTime", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(),
                                "Error loading events: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Ensure ongoingEvents is initialized
                    if (ongoingEvents == null) {
                        ongoingEvents = new ArrayList<>();
                    } else {
                        ongoingEvents.clear();
                    }

                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            String documentId = doc.getId();
                            OngoingEvent event = new OngoingEvent(
                                    doc.getString("eventName"),
                                    doc.getTimestamp("cutOffTime"),
                                    doc.getString("createdBy"),
                                    documentId
                            );
                            ongoingEvents.add(event);
                        }

                        adapter.updateEvents(ongoingEvents);

                    } else {
                        adapter.updateEvents(new ArrayList<>());
                    }

                    // Toggle visibility based on ongoing events
                    if (ongoingEvents.isEmpty()) {
                        noOngoingEvents.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        noOngoingEvents.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (firestoreListener != null) {
            firestoreListener.remove();
        }
    }

}