package com.example.ccisattendancechecker;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class RecordsFragment extends Fragment {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private RecordsAdapter adapter;
    private TextView noResultsTextView;
    private List<RecordsEvent> recordsList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_records, container, false);

        noResultsTextView = view.findViewById(R.id.noresultsText);
        searchView = view.findViewById(R.id.searchView);
        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Set up the adapter

    String currentUserEmail = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();

    fetchAttendanceData(currentUserEmail);

    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            adapter.filter(query);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            adapter.filter(newText);
            return false;
        }
    });

        return view;

    }

    private void fetchAttendanceData(String currentUserEmail) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Clear the list and populate it with new data
        // Update the RecyclerView
        ListenerRegistration firestoreListener = db.collection("attendance")
                .whereEqualTo("createdBy", currentUserEmail)
                .orderBy("dateCreated", Query.Direction.DESCENDING) // Sort by dateCreated in descending order
                .addSnapshotListener((querySnapshot, error) -> {

                    if (querySnapshot != null) {
                        // Clear the list and populate it with new data
                        recordsList.clear();
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {

                            String eventName = document.getString("eventName");
                            String id = document.getId();
                            Timestamp dateCreatedTimestamp = document.getTimestamp("dateCreated");
                            String dateCreated = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
                                    .format(dateCreatedTimestamp.toDate()); // Convert to formatted string


                            recordsList.add(new RecordsEvent(eventName, dateCreated,id));
                        }

                        // Update the RecyclerView
                        if (adapter == null) {
                            adapter = new RecordsAdapter(recordsList, noResultsTextView);
                            recyclerView.setAdapter(adapter);
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

}