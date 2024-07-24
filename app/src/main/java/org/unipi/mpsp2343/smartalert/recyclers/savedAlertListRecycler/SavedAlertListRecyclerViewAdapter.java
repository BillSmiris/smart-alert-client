package org.unipi.mpsp2343.smartalert.recyclers.savedAlertListRecycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.unipi.mpsp2343.smartalert.R;
import org.unipi.mpsp2343.smartalert.dto.SavedAlert;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

//Data adapter for the recycler that shows the saved alerts
public class SavedAlertListRecyclerViewAdapter extends RecyclerView.Adapter<SavedAlertListRecyclerViewAdapter.SavedEventListViewHolder>{
    private List<SavedAlert> alerts;
    private Context context;

    //Class for the items of the recycler
    public static class SavedEventListViewHolder extends RecyclerView.ViewHolder {
        private final TextView eventType; //type of the event
        private final TextView location; //location of the event
        private final TextView date; //date and time of the event

        public SavedEventListViewHolder(View view) {
            super(view);
            eventType = (TextView) view.findViewById(R.id.eventType);
            location = (TextView) view.findViewById(R.id.location);
            date = (TextView) view.findViewById(R.id.date);
        }

        public TextView getEventType() {
            return eventType;
        }

        public TextView getLocation() {
            return location;
        }

        public TextView getDate() {
            return date;
        }
    }

    public SavedAlertListRecyclerViewAdapter(List<SavedAlert> alerts, Context context) {
        this.alerts = alerts;
        Collections.reverse(this.alerts);
        this.context = context;
    }

    @Override
    public SavedAlertListRecyclerViewAdapter.SavedEventListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.saved_alert_list_item, viewGroup, false);

        return new SavedAlertListRecyclerViewAdapter.SavedEventListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SavedAlertListRecyclerViewAdapter.SavedEventListViewHolder viewHolder, final int position) {
        SavedAlert alert = alerts.get(position);
        //Display the event type, as a string, based on the received event type integer value
        viewHolder.getEventType().setText(Arrays.asList(context.getResources().getStringArray(R.array.event_types)).get(alert.getEventType()));
        //Display the event location
        viewHolder.getLocation().setText(context.getResources().getString(R.string.ed_event_location, "Lat: " + String.valueOf(alert.getLocation().getLat()) + ", Lon: " + String.valueOf(alert.getLocation().getLon())));
        //Format the event timestamp and display it
        viewHolder.getDate().setText(context.getResources().getString(R.string.first_reported, alert.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }
}
