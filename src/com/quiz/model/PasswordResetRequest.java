package com.quiz.model;

import java.sql.Timestamp;

public class PasswordResetRequest {
    private final int id;
    private final int userId;
    private final String role;
    private final String studentCode;
    private final String username;
    private final int adminId;
    private final Timestamp createdAt;
    private final String status;

    public PasswordResetRequest(int id, int userId, String role, String studentCode, String username,
            int adminId, Timestamp createdAt, String status) {
        this.id = id;
        this.userId = userId;
        this.role = role;
        this.studentCode = studentCode;
        this.username = username;
        this.adminId = adminId;
        this.createdAt = createdAt;
        this.status = status;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getRole() { return role; }
    public String getStudentCode() { return studentCode; }
    public String getUsername() { return username; }
    public int getAdminId() { return adminId; }
    public Timestamp getCreatedAt() { return createdAt; }
    public String getStatus() { return status; }

    public String getDisplayName() {
        String identifier = "STUDENT".equals(role) && studentCode != null && !studentCode.isBlank()
                ? studentCode
                : username;
        return identifier + " - " + username + " (" + role + ")";
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
