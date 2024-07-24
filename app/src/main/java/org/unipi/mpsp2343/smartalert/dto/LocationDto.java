package org.unipi.mpsp2343.smartalert.dto;

//Dto to encapsulate location data sent to and from the server.
public class LocationDto {
    private double lat;
    private double lon;

    public LocationDto(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
