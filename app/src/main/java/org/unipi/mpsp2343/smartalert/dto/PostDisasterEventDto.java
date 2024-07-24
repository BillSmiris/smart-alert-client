package org.unipi.mpsp2343.smartalert.dto;

import org.unipi.mpsp2343.smartalert.enums.EventType;

//Dto that encapsulates the data requires to report a disaster event.
public class PostDisasterEventDto {
    private String comments;
    private @EventType int eventType;
    private LocationDto location;
    private long timestamp;
    private String photoBase64;

    public PostDisasterEventDto(String comments, int eventType, LocationDto location, long timestamp, String photoBase64) {
        this.comments = comments;
        this.eventType = eventType;
        this.location = location;
        this.timestamp = timestamp;
        this.photoBase64 = photoBase64;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPhotoBase64() {
        return photoBase64;
    }

    public void setPhotoBase64(String photoBase64) {
        this.photoBase64 = photoBase64;
    }
}
