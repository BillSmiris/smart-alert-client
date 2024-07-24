package org.unipi.mpsp2343.smartalert.recyclers.eventListRecycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.unipi.mpsp2343.smartalert.R;
import org.unipi.mpsp2343.smartalert.dto.EventListItem;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

//Data adapter for the event list recycler
public class EventListRecyclerViewAdapter extends RecyclerView.Adapter<EventListRecyclerViewAdapter.EventListViewHolder> {
    private List<EventListItem> events;
    private Context context;
    private EventListRecyclerViewInterface eventListRecyclerViewInterface;

    //Class for the items of the event list
    public static class EventListViewHolder extends RecyclerView.ViewHolder {
        //Contains the following
        private final TextView eventType; //A display of the event type
        private final TextView date; //A display of the event date and time
        private final TextView numberOfReports; //A display of the number of reports about the event
        private final LinearLayout container; //A reference to the container of the above

        public EventListViewHolder(View view, EventListRecyclerViewInterface eventListRecyclerViewInterface) {
            super(view);
            eventType = (TextView) view.findViewById(R.id.eventType);
            date = (TextView) view.findViewById(R.id.date);
            numberOfReports = (TextView) view.findViewById(R.id.numberOfReports);
            container = (LinearLayout) view.findViewById(R.id.container);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //On clicking an item from the event list, the clicked item's position
                    //is passed to the function that is being triggered
                    if(eventListRecyclerViewInterface != null) {
                        int pos = getAdapterPosition();
                        if(pos != RecyclerView.NO_POSITION){
                            eventListRecyclerViewInterface.onItemClick(pos);
                        }
                    }
                }
            });
        }

        public TextView getEventType() {
            return eventType;
        }

        public TextView getDate() {
            return date;
        }

        public TextView getNumberOfReports() {
            return numberOfReports;
        }

        public LinearLayout getContainer() {
            return container;
        }
    }

    public EventListRecyclerViewAdapter(List<EventListItem> events, Context context, EventListRecyclerViewInterface eventListRecyclerViewInterface) {
        this.events = events;
        this.context = context;
        this.eventListRecyclerViewInterface = eventListRecyclerViewInterface;
    }

    @Override
    public EventListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.event_list_item, viewGroup, false);

        return new EventListViewHolder(view, eventListRecyclerViewInterface);
    }

    //Data of an event is set to be displayed here
    @Override
    public void onBindViewHolder(EventListViewHolder viewHolder, final int position) {
        EventListItem event = events.get(position);
        //Display the event type, as a string, based on the received event type integer value
        viewHolder.getEventType().setText(Arrays.asList(context.getResources().getStringArray(R.array.event_types)).get(event.getEventType()));
        //Format the event timestamp and display it
        Date date = new Date(event.getTimestamp());
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        viewHolder.getDate().setText(context.getResources().getString(R.string.first_reported, df.format("hh:mm:ss - dd/MM/yyyy", date)));
        //Display the number of the reports about the event
        viewHolder.getNumberOfReports().setText(context.getResources().getString(R.string.times_reported, String.valueOf(event.getNumberOfReports())));
        //Set the color of the container based on the number of reports/severity of the event
        if(event.getNumberOfReports() > 20) {
            viewHolder.getContainer().setBackgroundColor(context.getResources().getColor(R.color.danger_high));
        } else if (event.getNumberOfReports() > 10) {
            viewHolder.getContainer().setBackgroundColor(context.getResources().getColor(R.color.danger_medium));
        } else {
            viewHolder.getContainer().setBackgroundColor(context.getResources().getColor(R.color.danger_low));
        }

    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}
