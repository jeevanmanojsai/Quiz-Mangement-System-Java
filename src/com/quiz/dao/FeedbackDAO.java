package com.quiz.dao;

import com.quiz.model.Feedback;
import com.quiz.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAO {
    public boolean addFeedback(int userId, String message) throws SQLException {
        String query = "INSERT INTO feedbacks (user_id, message) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, message);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Feedback> getAllFeedbacks() {
        List<Feedback> feedbacks = new ArrayList<>();
        String query = "SELECT f.*, u.username FROM feedbacks f LEFT JOIN users u ON f.user_id = u.id ORDER BY f.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                feedbacks.add(new Feedback(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("message"),
                        rs.getTimestamp("created_at")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return feedbacks;
    }

    public List<Feedback> getFeedbacksByAdminId(int adminId) {
        List<Feedback> feedbacks = new ArrayList<>();
        String query = "SELECT f.*, u.username FROM feedbacks f " +
                "LEFT JOIN users u ON f.user_id = u.id " +
                "WHERE u.admin_id = ? " +
                "ORDER BY f.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, adminId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                feedbacks.add(new Feedback(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("message"),
                        rs.getTimestamp("created_at")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return feedbacks;
    }
}
