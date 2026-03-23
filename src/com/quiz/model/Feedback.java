package com.quiz.model;

import java.sql.Timestamp;

public class Feedback {
    private int id;
    private int userId;
    private String username; // Helper for display
    private String message;
    private Timestamp createdAt;

    public Feedback() {
    }

    public Feedback(int id, int userId, String username, String message, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.message = message;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
