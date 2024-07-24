package org.unipi.mpsp2343.smartalert.dto;

import org.unipi.mpsp2343.smartalert.enums.EventType;

//Object that encapsulates the date of an item of the event list available to the employee
public class EventListItem {
    private String id; //event id
    private @EventType int eventType;
    private long timestamp;
    private long numberOfReports;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getNumberOfReports() {
        return numberOfReports;
    }

    public void setNumberOfReports(long numberOfReports) {
        this.numberOfReports = numberOfReports;
    }

    @Override
    public String toString() {
        return "EventListItem{" +
                "id='" + id + '\'' +
                ", eventType=" + eventType +
                ", timestamp=" + timestamp +
                ", numberOfReports=" + numberOfReports +
                '}';
    }
}
