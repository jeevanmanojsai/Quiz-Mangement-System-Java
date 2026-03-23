package com.quiz.dao;

import com.quiz.model.Result;
import com.quiz.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResultDAO {
    public boolean hasAttemptedExam(int userId, int examId) {
        String query = "SELECT COUNT(*) FROM results WHERE user_id = ? AND exam_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, examId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean saveResult(Result result) throws SQLException {
        if (hasAttemptedExam(result.getUserId(), result.getExamId())) {
            return false;
        }
        String query = "INSERT INTO results (user_id, exam_id, score, total_questions) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, result.getUserId());
            stmt.setInt(2, result.getExamId());
            stmt.setInt(3, result.getScore());
            stmt.setInt(4, result.getTotalQuestions());
            return stmt.executeUpdate() > 0;
        }
    }

    public List<String[]> getAllResultsForExport() {
        return getResultsForExport(0, null, null, null, null);
    }

    public List<String[]> getResultsForExport(int adminId, Integer examId) {
        return getResultsForExport(adminId, examId, null, null, null);
    }

    public List<String[]> getResultsForExport(int adminId, Integer examId, String dateFilter, String timeFromFilter,
            String timeToFilter) {
        List<String[]> rows = new ArrayList<>();
        String query = "SELECT r.exam_id, COALESCE(NULLIF(u.student_code, ''), CAST(u.id AS CHAR), 'Unknown') as student_identifier, " +
                "IFNULL(e.title, 'Deleted Exam') as title, IFNULL(e.subject, 'General') as subject, " +
                "r.score, r.total_questions, r.taken_at " +
                "FROM results r " +
                "LEFT JOIN users u ON r.user_id = u.id " +
                "LEFT JOIN exams e ON r.exam_id = e.id " +
                (adminId > 0 ? "WHERE e.admin_id = ? " : "") +
                (adminId > 0 && examId != null ? "AND e.id = ? " : adminId <= 0 && examId != null ? "WHERE e.id = ? " : "") +
                ((adminId > 0 || examId != null) && dateFilter != null ? "AND DATE(r.taken_at) = ? " : adminId <= 0 && examId == null && dateFilter != null ? "WHERE DATE(r.taken_at) = ? " : "") +
                ((adminId > 0 || examId != null || dateFilter != null) && timeFromFilter != null ? "AND TIME(r.taken_at) >= ? " : adminId <= 0 && examId == null && dateFilter == null && timeFromFilter != null ? "WHERE TIME(r.taken_at) >= ? " : "") +
                ((adminId > 0 || examId != null || dateFilter != null || timeFromFilter != null) && timeToFilter != null ? "AND TIME(r.taken_at) <= ? " : adminId <= 0 && examId == null && dateFilter == null && timeFromFilter == null && timeToFilter != null ? "WHERE TIME(r.taken_at) <= ? " : "") +
                "ORDER BY IFNULL(e.subject, 'General') ASC, IFNULL(e.title, 'Deleted Exam') ASC, " +
                "r.score DESC, r.taken_at ASC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            int parameterIndex = 1;
            if (adminId > 0) {
                stmt.setInt(parameterIndex++, adminId);
            }
            if (examId != null) {
                stmt.setInt(parameterIndex++, examId);
            }
            if (dateFilter != null) {
                stmt.setString(parameterIndex++, dateFilter);
            }
            if (timeFromFilter != null) {
                stmt.setString(parameterIndex++, timeFromFilter + ":00");
            }
            if (timeToFilter != null) {
                stmt.setString(parameterIndex, timeToFilter + ":59");
            }
            ResultSet rs = stmt.executeQuery();
            int rank = 1;
            int previousExamId = Integer.MIN_VALUE;
            while (rs.next()) {
                int currentExamId = rs.getInt("exam_id");
                if (currentExamId != previousExamId) {
                    rank = 1;
                    previousExamId = currentExamId;
                }
                rows.add(new String[] {
                        String.valueOf(rank++),
                        rs.getString("student_identifier"),
                        rs.getString("title"),
                        String.valueOf(rs.getInt("score")),
                        String.valueOf(rs.getInt("total_questions")),
                        String.valueOf(rs.getTimestamp("taken_at"))
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    public List<Result> getResultsByUserId(int userId) throws SQLException {
        List<Result> results = new ArrayList<>();
        String query = "SELECT * FROM results WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(new Result(rs.getInt("id"), rs.getInt("user_id"), rs.getInt("exam_id"), rs.getInt("score"),
                        rs.getInt("total_questions"), rs.getTimestamp("taken_at")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    public List<String> getAllResultsForAdmin() {
        List<String> results = new ArrayList<>();
        String query = "SELECT r.exam_id, r.score, r.total_questions, r.taken_at, u.username, " +
                "IFNULL(e.title, 'Deleted Exam') as title, IFNULL(e.subject, 'General') as subject "
                +
                "FROM results r " +
                "LEFT JOIN users u ON r.user_id = u.id " +
                "LEFT JOIN exams e ON r.exam_id = e.id " +
                "ORDER BY IFNULL(e.subject, 'General') ASC, IFNULL(e.title, 'Deleted Exam') ASC, " +
                "r.score DESC, r.taken_at ASC";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            int rank = 1;
            int previousExamId = Integer.MIN_VALUE;
            while (rs.next()) {
                int currentExamId = rs.getInt("exam_id");
                if (currentExamId != previousExamId) {
                    rank = 1;
                    previousExamId = currentExamId;
                }
                String row = String.format("Rank %-4d | %-15s | %-12s | %-20s | %d/%d | %s",
                        rank++,
                        rs.getString("username"),
                        rs.getString("subject"),
                        rs.getString("title"),
                        rs.getInt("score"),
                        rs.getInt("total_questions"),
                        rs.getTimestamp("taken_at"));
                results.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    public List<String> getResultsForAdmin(int adminId) {
        return getResultsForAdmin(adminId, null);
    }

    public List<String> getResultsForAdmin(int adminId, Integer examId) {
        return getResultsForAdmin(adminId, examId, null, null, null);
    }

    public List<String> getResultsForAdmin(int adminId, Integer examId, String dateFilter, String timeFromFilter,
            String timeToFilter) {
        List<String> results = new ArrayList<>();
        String query = "SELECT r.score, r.total_questions, r.taken_at, " +
                "COALESCE(NULLIF(u.student_code, ''), CAST(u.id AS CHAR), 'Unknown') as student_identifier, " +
                "IFNULL(e.title, 'Deleted Exam') as title, IFNULL(e.subject, 'General') as subject " +
                "FROM results r " +
                "LEFT JOIN users u ON r.user_id = u.id " +
                "LEFT JOIN exams e ON r.exam_id = e.id " +
                "WHERE e.admin_id = ? " +
                (examId != null ? "AND e.id = ? " : "") +
                (dateFilter != null ? "AND DATE(r.taken_at) = ? " : "") +
                (timeFromFilter != null ? "AND TIME(r.taken_at) >= ? " : "") +
                (timeToFilter != null ? "AND TIME(r.taken_at) <= ? " : "") +
                "ORDER BY IFNULL(e.subject, 'General') ASC, IFNULL(e.title, 'Deleted Exam') ASC, " +
                "r.score DESC, r.taken_at ASC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, adminId);
            if (examId != null) {
                stmt.setInt(parameterIndex++, examId);
            }
            if (dateFilter != null) {
                stmt.setString(parameterIndex++, dateFilter);
            }
            if (timeFromFilter != null) {
                stmt.setString(parameterIndex++, timeFromFilter + ":00");
            }
            if (timeToFilter != null) {
                stmt.setString(parameterIndex, timeToFilter + ":59");
            }
            ResultSet rs = stmt.executeQuery();
            int rank = 1;
            String previousExamKey = null;
            while (rs.next()) {
                String currentExamKey = rs.getString("subject") + "|" + rs.getString("title");
                if (!currentExamKey.equals(previousExamKey)) {
                    rank = 1;
                    previousExamKey = currentExamKey;
                }
                String row = String.format("Rank %-4d | %-15s | %-12s | %-20s | %d/%d | %s",
                        rank++,
                        rs.getString("student_identifier"),
                        rs.getString("subject"),
                        rs.getString("title"),
                        rs.getInt("score"),
                        rs.getInt("total_questions"),
                        rs.getTimestamp("taken_at"));
                results.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    public List<String> getDetailedResultsByUserId(int userId) {
        List<String> results = new ArrayList<>();
        String query = "SELECT r.score, r.total_questions, r.taken_at, IFNULL(e.title, 'Deleted Exam') as title " +
                "FROM results r " +
                "LEFT JOIN exams e ON r.exam_id = e.id " +
                "WHERE r.user_id = ? " +
                "ORDER BY r.taken_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(String.format("[%s] Score: %d/%d | %s",
                        rs.getTimestamp("taken_at"),
                        rs.getInt("score"),
                        rs.getInt("total_questions"),
                        rs.getString("title")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
}
