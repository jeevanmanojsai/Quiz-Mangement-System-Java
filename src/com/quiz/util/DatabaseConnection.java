package com.quiz.util;

import com.quiz.util.PasswordUtils;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/qems_db?createDatabaseIfNotExist=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "Manoj@123";
    private static volatile boolean initialized = false;

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found!", e);
        }
    }

    public static synchronized void initializeSchema() throws SQLException {
        if (initialized)
            return;

        String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "student_code VARCHAR(20) UNIQUE NULL," +
                "username VARCHAR(50) NOT NULL," +
                "password VARCHAR(255) NOT NULL," +
                "role ENUM('ADMIN', 'STUDENT') NOT NULL," +
                "admin_id INT NULL," +
                "FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE SET NULL" +
                ")";

        String createPasswordResetRequests = "CREATE TABLE IF NOT EXISTS password_reset_requests (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT NOT NULL," +
                "status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING'," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "resolved_at TIMESTAMP NULL," +
                "resolved_by INT NULL," +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                "FOREIGN KEY (resolved_by) REFERENCES users(id) ON DELETE SET NULL" +
                ")";

        String createExams = "CREATE TABLE IF NOT EXISTS exams (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "title VARCHAR(100) NOT NULL," +
                "description TEXT," +
                "subject VARCHAR(100) NOT NULL DEFAULT 'General'," +
                "admin_id INT," +
                "FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE SET NULL" +
                ")";

        String createQuestions = "CREATE TABLE IF NOT EXISTS questions (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "exam_id INT," +
                "question_text TEXT NOT NULL," +
                "option_a VARCHAR(255) NOT NULL," +
                "option_b VARCHAR(255) NOT NULL," +
                "option_c VARCHAR(255) NOT NULL," +
                "option_d VARCHAR(255) NOT NULL," +
                "correct_option CHAR(1) NOT NULL," +
                "FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE" +
                ")";

        String createResults = "CREATE TABLE IF NOT EXISTS results (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT," +
                "exam_id INT," +
                "score INT," +
                "total_questions INT," +
                "taken_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                "FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE" +
                ")";

        String createFeedbacks = "CREATE TABLE IF NOT EXISTS feedbacks (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT," +
                "message TEXT NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ")";

        String seedAdmin = "INSERT INTO users (id, username, password, role) " +
                "SELECT CASE " +
                "WHEN EXISTS (SELECT 1 FROM users WHERE id = 1) THEN (SELECT COALESCE(MAX(id), 0) + 1 FROM users) " +
                "ELSE 1 END, 'admin', ?, 'ADMIN' " +
                "WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin')";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createUsers);
            try {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN student_code VARCHAR(20) NULL");
            } catch (SQLException e) {
                if (e.getErrorCode() != 1060) {
                    throw e;
                }
            }
            try {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN admin_id INT NULL");
            } catch (SQLException e) {
                if (e.getErrorCode() != 1060) {
                    throw e;
                }
            }
            try {
                stmt.executeUpdate(
                        "ALTER TABLE users ADD CONSTRAINT fk_users_admin FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE SET NULL");
            } catch (SQLException e) {
                if (e.getErrorCode() != 1826 && e.getErrorCode() != 1061) {
                    throw e;
                }
            }
            stmt.executeUpdate(
                    "UPDATE users SET student_code = CAST(id AS CHAR) " +
                            "WHERE role = 'STUDENT' AND (student_code IS NULL OR student_code = '')");
            try {
                stmt.executeUpdate("ALTER TABLE users ADD CONSTRAINT uq_users_student_code UNIQUE (student_code)");
            } catch (SQLException e) {
                if (e.getErrorCode() != 1826 && e.getErrorCode() != 1061) {
                    throw e;
                }
            }
            dropUniqueIndexesForColumn(conn, "users", "username");
            stmt.executeUpdate(createExams);
            try {
                stmt.executeUpdate("ALTER TABLE exams ADD COLUMN subject VARCHAR(100) NOT NULL DEFAULT 'General'");
            } catch (SQLException e) {
                if (e.getErrorCode() != 1060) {
                    throw e;
                }
            }
            try {
                stmt.executeUpdate("ALTER TABLE exams ADD COLUMN admin_id INT NULL");
            } catch (SQLException e) {
                if (e.getErrorCode() != 1060) {
                    throw e;
                }
            }
            try {
                stmt.executeUpdate(
                        "ALTER TABLE exams ADD CONSTRAINT fk_exams_admin FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE SET NULL");
            } catch (SQLException e) {
                if (e.getErrorCode() != 1826 && e.getErrorCode() != 1061) {
                    throw e;
                }
            }
            stmt.executeUpdate(createQuestions);
            stmt.executeUpdate(createResults);
            try {
                stmt.executeUpdate("ALTER TABLE results ADD CONSTRAINT uq_results_user_exam UNIQUE (user_id, exam_id)");
            } catch (SQLException e) {
                // Ignore if unique key already exists or existing duplicate rows prevent adding it.
                if (e.getErrorCode() != 1061 && e.getErrorCode() != 1062) {
                    throw e;
                }
            }
            stmt.executeUpdate(createFeedbacks);
            stmt.executeUpdate(createPasswordResetRequests);
            try (PreparedStatement seedStmt = conn.prepareStatement(seedAdmin)) {
                seedStmt.setString(1, PasswordUtils.hashPassword("Manoj@123"));
                seedStmt.executeUpdate();
            }
            migratePlaintextPasswords(conn);
            stmt.executeUpdate(
                    "UPDATE users SET admin_id = NULL WHERE role = 'ADMIN'");
            stmt.executeUpdate(
                    "UPDATE users SET admin_id = (" +
                            "SELECT admin_ref.id FROM (" +
                            "SELECT id FROM users WHERE role = 'ADMIN' ORDER BY id ASC LIMIT 1" +
                            ") admin_ref" +
                            ") " +
                            "WHERE role = 'STUDENT' AND admin_id IS NULL");
            stmt.executeUpdate(
                    "UPDATE exams SET admin_id = (SELECT id FROM users WHERE role = 'ADMIN' ORDER BY id ASC LIMIT 1) " +
                            "WHERE admin_id IS NULL");
            initialized = true;
        }
    }

    private static void migratePlaintextPasswords(Connection conn) throws SQLException {
        String selectQuery = "SELECT id, password FROM users";
        String updateQuery = "UPDATE users SET password = ? WHERE id = ?";
        try (Statement selectStmt = conn.createStatement();
                ResultSet rs = selectStmt.executeQuery(selectQuery);
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
            while (rs.next()) {
                String storedPassword = rs.getString("password");
                if (storedPassword == null || PasswordUtils.isHashed(storedPassword)) {
                    continue;
                }
                updateStmt.setString(1, PasswordUtils.hashPassword(storedPassword));
                updateStmt.setInt(2, rs.getInt("id"));
                updateStmt.addBatch();
            }
            updateStmt.executeBatch();
        }
    }

    private static void dropUniqueIndexesForColumn(Connection conn, String tableName, String columnName)
            throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet indexes = metaData.getIndexInfo(conn.getCatalog(), null, tableName, true, false);
                Statement stmt = conn.createStatement()) {
            while (indexes.next()) {
                String indexName = indexes.getString("INDEX_NAME");
                String indexedColumn = indexes.getString("COLUMN_NAME");
                if (indexName == null || "PRIMARY".equalsIgnoreCase(indexName)) {
                    continue;
                }
                if (columnName.equalsIgnoreCase(indexedColumn)) {
                    stmt.executeUpdate("ALTER TABLE " + tableName + " DROP INDEX " + indexName);
                }
            }
        }
    }
}
