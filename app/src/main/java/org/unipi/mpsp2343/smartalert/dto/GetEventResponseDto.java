package org.unipi.mpsp2343.smartalert.dto;

import org.unipi.mpsp2343.smartalert.enums.EventStatus;
import org.unipi.mpsp2343.smartalert.enums.EventType;

import java.util.List;

//Dto to capture the result of the request to get the details of an event
public class GetEventResponseDto {
    private String id;
    private @EventType int eventType;
    private LocationDto location;
    private long timestamp;
    private List<ReportDto> reports;
    private long numberOfReports;
    private @EventStatus int eventStatus;

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

    public List<ReportDto> getReports() {
        return reports;
    }

    public void setReports(List<ReportDto> reports) {
        this.reports = reports;
    }

    public long getNumberOfReports() {
        return numberOfReports;
    }

    public void setNumberOfReports(long numberOfReports) {
        this.numberOfReports = numberOfReports;
    }

    public int getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(int eventStatus) {
        this.eventStatus = eventStatus;
    }

    @Override
    public String toString() {
        return "GetEventResponseDto{" +
                "id='" + id + '\'' +
                ", eventType=" + eventType +
                ", location=" + location +
                ", timestamp=" + timestamp +
                ", reports=" + reports +
                ", numberOfReports=" + numberOfReports +
                ", eventStatus=" + eventStatus +
                '}';
    }
}
