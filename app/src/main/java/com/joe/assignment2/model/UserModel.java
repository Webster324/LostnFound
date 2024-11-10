package com.joe.assignment2.model;

import com.google.firebase.Timestamp;

public class UserModel {
    private String phone;               // User's phone number
    private String username;            // User's username
    private Timestamp createdTimestamp; // Timestamp when the user account was created
    private String userId;              // Unique identifier for the user
    private String fcmToken;            // Firebase Cloud Messaging token for push notifications

    // Default constructor
    public UserModel() {
    }

    // Constructor to initialize user data
    public UserModel(String phone, String username, Timestamp createdTimestamp, String userId) {
        this.phone = phone;
        this.username = username;
        this.createdTimestamp = createdTimestamp;
        this.userId = userId;
    }

    // Getters and setters for each field
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
