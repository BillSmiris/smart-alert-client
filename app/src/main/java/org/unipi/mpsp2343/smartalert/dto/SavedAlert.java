package org.unipi.mpsp2343.smartalert.dto;

import org.unipi.mpsp2343.smartalert.R;
import org.unipi.mpsp2343.smartalert.enums.EventType;

import java.util.Date;

//Objects that represent saved alerts
public class SavedAlert {
    private @EventType int eventType;
    private LocationDto location;
    private String timestamp;

    public SavedAlert() {
    }

    public SavedAlert(int eventType, LocationDto location, Date timestamp) {
        this.eventType = eventType;
        this.location = location;
        this.timestamp = formatTimestamp(timestamp);
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public LocationDto getLocation() {
        return location;
    }

    public void setLocation(LocationDto location) {
        this.location = location;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = formatTimestamp(timestamp);
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    private static String formatTimestamp(Date timestamp) {
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        return df.format("hh:mm:ss - dd/MM/yyyy", timestamp).toString();
    }
}
