package org.unipi.mpsp2343.smartalert.dto;

//Dto to capture the data of each report of an event, when retrieving an event's details.
public class ReportDto {
    private String comments;
    private String photoBase64;
    private String userEmail;

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getPhotoBase64() {
        return photoBase64;
    }

    public void setPhotoBase64(String photoBase64) {
        this.photoBase64 = photoBase64;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
