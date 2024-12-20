package com.example.ccisattendancechecker;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OngoingEventAdapter extends RecyclerView.Adapter<OngoingEventAdapter.OngoingEventViewHolder> {
    private List<OngoingEvent> ongoingEventsList;
    private Context context;
    private Handler mainHandler;
    private FirebaseFirestore db;




    public OngoingEventAdapter(Context context) {
        this.ongoingEventsList = new ArrayList<>();
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());



    }



    @NonNull
    @Override
    public OngoingEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_events, parent, false);
        return new OngoingEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OngoingEventViewHolder holder, int position) {
        if (position >= ongoingEventsList.size()) return;

        OngoingEvent event = ongoingEventsList.get(position);
        holder.item_event_name.setText(event.getEventName());

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
        String formattedTime = sdf.format(event.getCutOffTime().toDate());
        holder.item_cutOffTime.setText(formattedTime);

        // Handle item click
      holder.itemView.setOnClickListener( view -> {

          Intent intent = new Intent(context,QrScannerActivity.class);
            intent.putExtra("eventId", event.getEventId());

          context.startActivity(intent);

      });

        checkAndRemoveIfExpired(event, position);

    }

    private void checkAndRemoveIfExpired(OngoingEvent event, int position) {
        Date currentTime = new Date();
        Date cutoffTime = event.getCutOffTime().toDate();


        if (currentTime.after(cutoffTime)) {
            // Remove event from RecyclerView
            mainHandler.post(() -> {
                if (position < ongoingEventsList.size()) {
                    ongoingEventsList.remove(position);
                    notifyItemRemoved(position);
                }
            });

        }
    }


    public void updateEvents(List<OngoingEvent> newEvents) {
        mainHandler.post(() -> {
            ongoingEventsList.clear();
            ongoingEventsList.addAll(newEvents);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return ongoingEventsList.size();
    }


    public static class OngoingEventViewHolder extends RecyclerView.ViewHolder {
        TextView item_event_name, item_cutOffTime;

        public OngoingEventViewHolder(@NonNull View itemView) {
            super(itemView);
            item_event_name = itemView.findViewById(R.id.item_event_name);
            item_cutOffTime = itemView.findViewById(R.id.item_cutOffTime);
        }
    }
}

