package com.quiz.dao;

import com.quiz.model.PasswordResetRequest;
import com.quiz.model.User;
import com.quiz.util.DatabaseConnection;
import com.quiz.util.PasswordUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_STUDENT = "STUDENT";

    public User login(String identifier, String password, String role) {
        String query = "SELECT * FROM users WHERE role = ? AND " +
                ("ADMIN".equals(role) ? "username = ?" : "student_code = ?") +
                " ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, role);
            stmt.setString(2, ROLE_ADMIN.equals(role) ? identifier.trim() : normalizeStudentCode(identifier));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = mapUser(rs);
                if (PasswordUtils.verifyPassword(password, user.getPassword())) {
                    upgradePasswordHashIfNeeded(user, password);
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean register(User user) {
        String query = "INSERT INTO users (student_code, username, password, role, admin_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, normalizeStudentCode(user.getStudentCode()));
            stmt.setString(2, user.getUsername());
            stmt.setString(3, PasswordUtils.hashPassword(user.getPassword()));
            stmt.setString(4, user.getRole());
            setNullableAdminId(stmt, 5, user.getAdminId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<User> getAllStudents() {
        return getUsersByRole(ROLE_STUDENT, null);
    }

    public List<User> getStudentsByAdminId(int adminId) {
        return getUsersByRole(ROLE_STUDENT, adminId);
    }

    public List<User> getAllAdmins() {
        return getUsersByRole(ROLE_ADMIN, null);
    }

    private List<User> getUsersByRole(String role, Integer adminId) {
        List<User> users = new ArrayList<>();
        String query = adminId == null
                ? "SELECT * FROM users WHERE role = ? ORDER BY COALESCE(student_code, CAST(id AS CHAR)) ASC, id ASC"
                : "SELECT * FROM users WHERE role = ? AND admin_id = ? ORDER BY COALESCE(student_code, CAST(id AS CHAR)) ASC, id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, role);
            if (adminId != null) {
                stmt.setInt(2, adminId);
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean addStudent(User user) throws SQLException {
        return addUserByRole(user, ROLE_STUDENT);
    }

    public boolean addAdmin(User user) throws SQLException {
        return addUserByRole(user, ROLE_ADMIN);
    }

    private boolean addUserByRole(User user, String role) throws SQLException {
        String query = "INSERT INTO users (student_code, username, password, role, admin_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            if (ROLE_STUDENT.equals(role)) {
                stmt.setString(1, normalizeStudentCode(user.getStudentCode()));
            } else {
                stmt.setNull(1, Types.VARCHAR);
            }
            stmt.setString(2, user.getUsername());
            stmt.setString(3, PasswordUtils.hashPassword(user.getPassword()));
            stmt.setString(4, role);
            if (ROLE_STUDENT.equals(role)) {
                setNullableAdminId(stmt, 5, user.getAdminId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateStudent(User user) throws SQLException {
        return updateUserByRole(user, ROLE_STUDENT);
    }

    public boolean updateAdmin(User user) throws SQLException {
        return updateUserByRole(user, ROLE_ADMIN);
    }

    public boolean studentCodeExists(String studentCode) {
        return studentCodeExists(studentCode, null);
    }

    public boolean studentCodeExists(String studentCode, Integer excludeUserId) {
        String normalizedStudentCode = normalizeStudentCode(studentCode);
        if (normalizedStudentCode == null || normalizedStudentCode.isBlank()) {
            return false;
        }
        String query = excludeUserId == null
                ? "SELECT 1 FROM users WHERE student_code = ? LIMIT 1"
                : "SELECT 1 FROM users WHERE student_code = ? AND id <> ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, normalizedStudentCode);
            if (excludeUserId != null) {
                stmt.setInt(2, excludeUserId);
            }
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean adminUsernameExists(String username) {
        return adminUsernameExists(username, null);
    }

    public boolean adminUsernameExists(String username, Integer excludeUserId) {
        if (username == null || username.isBlank()) {
            return false;
        }
        String query = excludeUserId == null
                ? "SELECT 1 FROM users WHERE role = ? AND username = ? LIMIT 1"
                : "SELECT 1 FROM users WHERE role = ? AND username = ? AND id <> ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, ROLE_ADMIN);
            stmt.setString(2, username.trim());
            if (excludeUserId != null) {
                stmt.setInt(3, excludeUserId);
            }
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean updateUserByRole(User user, String role) throws SQLException {
        String query = "UPDATE users SET student_code = ?, username = ?, password = COALESCE(?, password), " +
                "admin_id = ? WHERE id = ? AND role = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            if (ROLE_STUDENT.equals(role)) {
                stmt.setString(1, normalizeStudentCode(user.getStudentCode()));
            } else {
                stmt.setNull(1, Types.VARCHAR);
            }
            stmt.setString(2, user.getUsername());
            if (user.getPassword() == null || user.getPassword().isBlank()) {
                stmt.setNull(3, Types.VARCHAR);
            } else {
                stmt.setString(3, PasswordUtils.hashPassword(user.getPassword()));
            }
            if (ROLE_STUDENT.equals(role)) {
                setNullableAdminId(stmt, 4, user.getAdminId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.setInt(5, user.getId());
            stmt.setString(6, role);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteStudent(int id) throws SQLException {
        return deleteUserByRole(id, ROLE_STUDENT);
    }

    public boolean deleteAdmin(int id) throws SQLException {
        return deleteUserByRole(id, ROLE_ADMIN);
    }

    private boolean deleteUserByRole(int id, String role) throws SQLException {
        String query = "DELETE FROM users WHERE id = ? AND role = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.setString(2, role);
            return stmt.executeUpdate() > 0;
        }
    }

    private void setNullableAdminId(PreparedStatement stmt, int parameterIndex, int adminId) throws SQLException {
        if (adminId > 0) {
            stmt.setInt(parameterIndex, adminId);
        } else {
            stmt.setNull(parameterIndex, Types.INTEGER);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(rs.getInt("id"), rs.getString("student_code"), rs.getString("username"),
                rs.getString("password"), rs.getString("role"), rs.getInt("admin_id"));
    }

    private String normalizeStudentCode(String studentCode) {
        return studentCode == null ? null : studentCode.trim().toUpperCase();
    }

    public boolean createPasswordResetRequest(String role, String identifier) throws SQLException {
        User user = findUserByRoleIdentifier(role, identifier);
        if (user == null) {
            return false;
        }

        String existingRequestQuery = "SELECT id FROM password_reset_requests WHERE user_id = ? AND status = 'PENDING' LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(existingRequestQuery)) {
            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        }

        String insertQuery = "INSERT INTO password_reset_requests (user_id, status) VALUES (?, 'PENDING')";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setInt(1, user.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    public List<PasswordResetRequest> getPendingResetRequestsForAdmin(int adminId) {
        List<PasswordResetRequest> requests = new ArrayList<>();
        String query = "SELECT pr.*, u.student_code, u.username, u.role, u.admin_id " +
                "FROM password_reset_requests pr " +
                "JOIN users u ON pr.user_id = u.id " +
                "WHERE pr.status = 'PENDING' AND ((u.role = 'STUDENT' AND u.admin_id = ?) OR u.role = 'ADMIN') " +
                "ORDER BY pr.created_at ASC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, adminId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                requests.add(new PasswordResetRequest(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("role"),
                        rs.getString("student_code"),
                        rs.getString("username"),
                        rs.getInt("admin_id"),
                        rs.getTimestamp("created_at"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    public boolean approvePasswordResetRequest(int requestId, int adminId, String temporaryPassword) throws SQLException {
        PasswordResetRequest request = getPasswordResetRequestById(requestId);
        if (request == null || !"PENDING".equals(request.getStatus())) {
            return false;
        }
        if ("STUDENT".equals(request.getRole()) && request.getAdminId() != adminId) {
            return false;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement updateUser = conn.prepareStatement("UPDATE users SET password = ? WHERE id = ?");
                    PreparedStatement updateRequest = conn.prepareStatement(
                            "UPDATE password_reset_requests SET status = 'APPROVED', resolved_at = CURRENT_TIMESTAMP, resolved_by = ? WHERE id = ?")) {
                updateUser.setString(1, PasswordUtils.hashPassword(temporaryPassword));
                updateUser.setInt(2, request.getUserId());
                updateUser.executeUpdate();

                updateRequest.setInt(1, adminId);
                updateRequest.setInt(2, requestId);
                updateRequest.executeUpdate();
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean rejectPasswordResetRequest(int requestId, int adminId) throws SQLException {
        PasswordResetRequest request = getPasswordResetRequestById(requestId);
        if (request == null || !"PENDING".equals(request.getStatus())) {
            return false;
        }
        if ("STUDENT".equals(request.getRole()) && request.getAdminId() != adminId) {
            return false;
        }
        String query = "UPDATE password_reset_requests SET status = 'REJECTED', resolved_at = CURRENT_TIMESTAMP, resolved_by = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, adminId);
            stmt.setInt(2, requestId);
            return stmt.executeUpdate() > 0;
        }
    }

    private User findUserByRoleIdentifier(String role, String identifier) throws SQLException {
        String query = "SELECT * FROM users WHERE role = ? AND " +
                ("ADMIN".equals(role) ? "username = ?" : "student_code = ?") +
                " LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, role);
            stmt.setString(2, ROLE_ADMIN.equals(role) ? identifier.trim() : normalizeStudentCode(identifier));
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? mapUser(rs) : null;
        }
    }

    private PasswordResetRequest getPasswordResetRequestById(int requestId) throws SQLException {
        String query = "SELECT pr.*, u.student_code, u.username, u.role, u.admin_id " +
                "FROM password_reset_requests pr " +
                "JOIN users u ON pr.user_id = u.id WHERE pr.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, requestId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
            return new PasswordResetRequest(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("role"),
                    rs.getString("student_code"),
                    rs.getString("username"),
                    rs.getInt("admin_id"),
                    rs.getTimestamp("created_at"),
                    rs.getString("status"));
        }
    }

    private void upgradePasswordHashIfNeeded(User user, String rawPassword) {
        if (user == null || PasswordUtils.isHashed(user.getPassword())) {
            return;
        }
        String hashedPassword = PasswordUtils.hashPassword(rawPassword);
        String query = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, hashedPassword);
            stmt.setInt(2, user.getId());
            stmt.executeUpdate();
            user.setPassword(hashedPassword);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
