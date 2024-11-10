package com.joe.assignment2.model;

import com.google.firebase.Timestamp;

public class ChatMessageModel {
    // Define message types: text (0) or image (1)
    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;

    private String message;     // Text content of the message
    private String imageUrl;    // URL for image content
    private String senderId;    // ID of the user sending the message
    private Timestamp timestamp;// Timestamp of the message
    private int messageType;    // Indicates the type of message: text or image

    // Default constructor
    public ChatMessageModel() {
    }

    // Constructor to initialize message data based on type
    public ChatMessageModel(String content, String senderId, Timestamp timestamp, int messageType) {
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.messageType = messageType;
        // Assigns content based on message type
        if (messageType == TYPE_TEXT) {
            this.message = content;
        } else if (messageType == TYPE_IMAGE) {
            this.imageUrl = content;
        }
    }

    // Getters and setters for each field
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }
}
