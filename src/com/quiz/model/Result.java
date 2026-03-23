package com.quiz.model;

import java.sql.Timestamp;

public class Result {
    private int id;
    private int userId;
    private int examId;
    private int score;
    private int totalQuestions;
    private Timestamp takenAt;

    public Result() {}

    public Result(int id, int userId, int examId, int score, int totalQuestions, Timestamp takenAt) {
        this.id = id;
        this.userId = userId;
        this.examId = examId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.takenAt = takenAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getExamId() { return examId; }
    public void setExamId(int examId) { this.examId = examId; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public Timestamp getTakenAt() { return takenAt; }
    public void setTakenAt(Timestamp takenAt) { this.takenAt = takenAt; }
}
