package com.quiz.model;

public class Exam {
    private int id;
    private String title;
    private String description;
    private String subject;
    private int adminId;

    public Exam() {}

    public Exam(int id, String title, String description) {
        this(id, title, description, "General", 0);
    }

    public Exam(int id, String title, String description, String subject, int adminId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.subject = subject;
        this.adminId = adminId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }

    @Override
    public String toString() { return subject + " - " + title; }
}
