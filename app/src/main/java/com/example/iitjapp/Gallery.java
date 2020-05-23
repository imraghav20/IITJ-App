package com.example.iitjapp;

public class Gallery
{
    String date, image, postBy, time, userId;

    public Gallery() {
    }

    public Gallery(String date, String image, String postBy, String time, String userId) {
        this.date = date;
        this.image = image;
        this.postBy = postBy;
        this.time = time;
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPostBy() {
        return postBy;
    }

    public void setPostBy(String postBy) {
        this.postBy = postBy;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
