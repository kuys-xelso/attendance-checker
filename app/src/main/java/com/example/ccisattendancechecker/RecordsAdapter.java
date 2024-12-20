package com.example.ccisattendancechecker;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.RecordsViewHolder>{

    private List<RecordsEvent> recordsList;
    private List<RecordsEvent> filteredList;
    private TextView noResultsTextView;



    public RecordsAdapter(List<RecordsEvent> recordsList, TextView noResultsTextView) {
        this.recordsList = recordsList;
        this.filteredList = new ArrayList<>(recordsList);
        this.noResultsTextView = noResultsTextView;
    }


    @NonNull
    @Override
    public RecordsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_records, parent, false);
        return new RecordsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordsViewHolder holder, int position) {

        RecordsEvent recordsEvent = filteredList.get(position);
        holder.eventNameTv.setText(recordsEvent.getEventName());
        holder.dateCreatedTv.setText(recordsEvent.getDateCreated());

        holder.itemView.setOnClickListener(view -> {
            Context context = view.getContext();
            Intent intent = new Intent(context, AttendanceReportActivity.class);
            intent.putExtra("eventName", recordsEvent.getEventName());
            intent.putExtra("eventId", recordsEvent.getId());
            intent.putExtra("dateCreated", recordsEvent.getDateCreated());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(recordsList); // Show all items if the query is empty
        } else {
            for (RecordsEvent record : recordsList) {
                if (record.getEventName().toLowerCase().trim().contains(query.toLowerCase().trim())) {
                    filteredList.add(record);
                }
            }
        }

        if (filteredList.isEmpty()) {
            noResultsTextView.setVisibility(View.VISIBLE);
        } else {
            noResultsTextView.setVisibility(View.GONE);
        }

        notifyDataSetChanged(); // Notify the adapter about the data change
    }


        public static class RecordsViewHolder extends RecyclerView.ViewHolder {
        TextView eventNameTv;
        TextView dateCreatedTv;

        public RecordsViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameTv = itemView.findViewById(R.id.eventNameTv);
            dateCreatedTv = itemView.findViewById(R.id.dateCreatedTv);

        }
    }
}
