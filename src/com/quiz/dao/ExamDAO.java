package com.quiz.dao;

import com.quiz.model.Exam;
import com.quiz.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamDAO {
    public boolean addExam(Exam exam) throws SQLException {
        String query = "INSERT INTO exams (title, description, subject, admin_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, exam.getTitle());
            stmt.setString(2, exam.getDescription());
            stmt.setString(3, exam.getSubject());
            stmt.setInt(4, exam.getAdminId());
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Exam> getAllExams() {
        List<Exam> exams = new ArrayList<>();
        String query = "SELECT * FROM exams ORDER BY subject ASC, title ASC";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                exams.add(mapExam(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exams;
    }

    public List<Exam> getExamsByAdminId(int adminId) {
        List<Exam> exams = new ArrayList<>();
        String query = "SELECT * FROM exams WHERE admin_id = ? ORDER BY subject ASC, title ASC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, adminId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                exams.add(mapExam(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exams;
    }

    public Exam getExamById(int id) {
        String query = "SELECT * FROM exams WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapExam(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateExam(Exam exam) throws SQLException {
        String query = "UPDATE exams SET title = ?, description = ?, subject = ? WHERE id = ? AND admin_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, exam.getTitle());
            stmt.setString(2, exam.getDescription());
            stmt.setString(3, exam.getSubject());
            stmt.setInt(4, exam.getId());
            stmt.setInt(5, exam.getAdminId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteExam(int id, int adminId) throws SQLException {
        String query = "DELETE FROM exams WHERE id = ? AND admin_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.setInt(2, adminId);
            return stmt.executeUpdate() > 0;
        }
    }

    private Exam mapExam(ResultSet rs) throws SQLException {
        return new Exam(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("subject"),
                rs.getInt("admin_id"));
    }
}
