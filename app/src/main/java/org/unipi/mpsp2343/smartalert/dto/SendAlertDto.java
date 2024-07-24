package org.unipi.mpsp2343.smartalert.dto;

import org.unipi.mpsp2343.smartalert.enums.EventType;

//Dto to capture the data of the notifications sent to the user
public class SendAlertDto {
    private @EventType int eventType;
    private LocationDto location;

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
}
