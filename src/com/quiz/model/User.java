package com.quiz.model;

public class User {
    private int id;
    private String studentCode;
    private String username;
    private String password;
    private String role; // "ADMIN" or "STUDENT"
    private int adminId;

    public User() {}

    public User(int id, String username, String password, String role) {
        this(id, null, username, password, role, 0);
    }

    public User(int id, String username, String password, String role, int adminId) {
        this(id, null, username, password, role, adminId);
    }

    public User(int id, String studentCode, String username, String password, String role, int adminId) {
        this.id = id;
        this.studentCode = studentCode;
        this.username = username;
        this.password = password;
        this.role = role;
        this.adminId = adminId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }

    public String getDisplayId() {
        return "STUDENT".equals(role) && studentCode != null && !studentCode.isBlank()
                ? studentCode
                : String.valueOf(id);
    }

    @Override
    public String toString() {
        return getDisplayId() + " - " + username;
    }
}
