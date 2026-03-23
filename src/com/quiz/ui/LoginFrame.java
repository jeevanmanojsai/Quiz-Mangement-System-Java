package com.quiz.ui;

import com.quiz.dao.UserDAO;
import com.quiz.model.User;
import com.quiz.util.UIUtils;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LoginFrame extends JFrame {
    private static final Dimension FORM_FIELD_SIZE = new Dimension(360, 46);
    private static final Dimension PRIMARY_BUTTON_SIZE = new Dimension(360, 50);
    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_ADMIN = "ADMIN";

    private JLabel idLabel;
    private JTextField idField;
    private JLabel adminLabel;
    private JComboBox<User> adminComboBox;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton submitButton;
    private JButton toggleModeButton;
    private JButton forgotPasswordButton;
    private JLabel usernameLabel;
    private JButton studentLoginButton;
    private JButton adminLoginButton;
    private boolean registerMode;
    private String loginMode;
    private final UserDAO userDAO;

    public LoginFrame() {
        userDAO = new UserDAO();
        setTitle("QEMS | Secure Login");
        setSize(520, 730);
        setMinimumSize(new Dimension(520, 730));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        UIUtils.styleFrame(this);
        setLayout(new BorderLayout());

        JPanel container = UIUtils.createPaddingPanel();
        container.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;

        JLabel titleLabel = new JLabel("QEMS");
        titleLabel.setFont(UIUtils.FONT_HEADER);
        titleLabel.setForeground(UIUtils.COLOR_ACCENT);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 8, 0);
        container.add(titleLabel, gbc);

        JLabel subLabel = new JLabel("Quiz & Exam Management System");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(java.awt.Color.GRAY);
        subLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 48, 0);
        container.add(subLabel, gbc);

        JPanel loginModePanel = new JPanel(new GridLayout(1, 2, 12, 0));
        loginModePanel.setOpaque(false);
        studentLoginButton = new JButton("Student Login");
        adminLoginButton = new JButton("Admin Login");
        UIUtils.styleButton(studentLoginButton);
        UIUtils.styleButton(adminLoginButton);
        loginModePanel.add(studentLoginButton);
        loginModePanel.add(adminLoginButton);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 24, 0);
        container.add(loginModePanel, gbc);

        idLabel = new JLabel("Student ID");
        idLabel.setFont(UIUtils.FONT_BOLD);
        idLabel.setForeground(UIUtils.COLOR_PRIMARY);
        idLabel.setVisible(false);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 5, 0);
        container.add(idLabel, gbc);

        idField = new JTextField();
        UIUtils.setRounding(idField);
        idField.setPreferredSize(FORM_FIELD_SIZE);
        idField.setVisible(false);
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 24, 0);
        container.add(idField, gbc);

        adminLabel = new JLabel("Under Admin");
        adminLabel.setFont(UIUtils.FONT_BOLD);
        adminLabel.setForeground(UIUtils.COLOR_PRIMARY);
        adminLabel.setVisible(false);
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 5, 0);
        container.add(adminLabel, gbc);

        adminComboBox = new JComboBox<>();
        UIUtils.setRounding(adminComboBox);
        adminComboBox.setPreferredSize(FORM_FIELD_SIZE);
        adminComboBox.setVisible(false);
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 24, 0);
        container.add(adminComboBox, gbc);

        usernameField = new JTextField();
        UIUtils.setRounding(usernameField);
        usernameField.setPreferredSize(FORM_FIELD_SIZE);
        usernameLabel = addLabel("Student ID", container, gbc, 7);
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 0, 24, 0);
        container.add(usernameField, gbc);

        passwordField = new JPasswordField();
        UIUtils.setRounding(passwordField);
        passwordField.setPreferredSize(FORM_FIELD_SIZE);
        addLabel("Password", container, gbc, 9);
        gbc.gridy = 10;
        gbc.insets = new Insets(0, 0, 36, 0);
        container.add(passwordField, gbc);

        submitButton = new JButton("Sign In");
        UIUtils.styleButton(submitButton);
        submitButton.setPreferredSize(PRIMARY_BUTTON_SIZE);
        submitButton.addActionListener(e -> {
            if (registerMode) {
                register();
            } else {
                login();
            }
        });
        gbc.gridy = 11;
        gbc.insets = new Insets(0, 0, 18, 0);
        container.add(submitButton, gbc);

        forgotPasswordButton = new JButton("Forgot Password?");
        forgotPasswordButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotPasswordButton.setForeground(UIUtils.COLOR_ACCENT);
        forgotPasswordButton.setBorderPainted(false);
        forgotPasswordButton.setContentAreaFilled(false);
        forgotPasswordButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPasswordButton.addActionListener(e -> showForgotPasswordDialog());
        gbc.gridy = 12;
        gbc.insets = new Insets(0, 0, 14, 0);
        container.add(forgotPasswordButton, gbc);

        toggleModeButton = new JButton("Don't have an account? Register");
        toggleModeButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        toggleModeButton.setForeground(UIUtils.COLOR_ACCENT);
        toggleModeButton.setBorderPainted(false);
        toggleModeButton.setContentAreaFilled(false);
        toggleModeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleModeButton.addActionListener(e -> setRegisterMode(!registerMode));
        gbc.gridy = 13;
        gbc.insets = new Insets(0, 0, 0, 0);
        container.add(toggleModeButton, gbc);

        studentLoginButton.addActionListener(e -> setLoginMode(ROLE_STUDENT));
        adminLoginButton.addActionListener(e -> setLoginMode(ROLE_ADMIN));

        add(container, BorderLayout.CENTER);
        setLoginMode(ROLE_STUDENT);
    }

    private JLabel addLabel(String text, JPanel p, GridBagConstraints gbc, int row) {
        JLabel l = new JLabel(text);
        l.setFont(UIUtils.FONT_BOLD);
        l.setForeground(UIUtils.COLOR_PRIMARY);
        gbc.gridy = row;
        gbc.insets = new Insets(0, 0, 5, 0);
        p.add(l, gbc);
        return l;
    }

    private void login() {
        String identifier = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        if (identifier.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fields cannot be empty", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = userDAO.login(identifier, password, loginMode);
        if (user != null) {
            dispose();
            if (ROLE_ADMIN.equals(user.getRole())) {
                new AdminDashboard(user).setVisible(true);
            } else {
                new StudentDashboard(user).setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    ROLE_ADMIN.equals(loginMode) ? "Invalid admin username or password!"
                            : "Invalid student ID or password!",
                    "Auth Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void register() {
        String idText = idField.getText().trim();
        User selectedAdmin = (User) adminComboBox.getSelectedItem();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        if (idText.isEmpty() || selectedAdmin == null || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Student ID, admin ID, name and password are required for registration.");
            return;
        }
        String studentCode = idText.toUpperCase();
        if (!studentCode.matches("[A-Z0-9]+")) {
            JOptionPane.showMessageDialog(this, "Student ID must contain only letters and numbers.", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        User newUser = new User(0, studentCode, username, password, ROLE_STUDENT, selectedAdmin.getId());
        if (userDAO.studentCodeExists(studentCode)) {
            JOptionPane.showMessageDialog(this, "Registration Failed! Student ID already exists.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (userDAO.register(newUser)) {
            JOptionPane.showMessageDialog(this, "Account Created! You can now Sign In.");
            clearFields();
            setRegisterMode(false);
        } else {
            JOptionPane.showMessageDialog(this, "Registration Failed! Unable to create account.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setRegisterMode(boolean registerMode) {
        this.registerMode = registerMode;
        studentLoginButton.setVisible(!registerMode);
        adminLoginButton.setVisible(!registerMode);
        forgotPasswordButton.setVisible(!registerMode);
        idLabel.setVisible(registerMode);
        idField.setVisible(registerMode);
        adminLabel.setVisible(registerMode);
        adminComboBox.setVisible(registerMode);
        if (registerMode) {
            loadAdminOptions();
        }
        usernameLabel.setText(registerMode ? "Name" : (ROLE_ADMIN.equals(loginMode) ? "Admin Username" : "Student ID"));
        submitButton.setText(registerMode ? "Register" : "Sign In");
        toggleModeButton.setText(registerMode ? "Already have an account? Sign In"
                : "Don't have an account? Register");
        revalidate();
        repaint();
    }

    private void loadAdminOptions() {
        DefaultComboBoxModel<User> model = new DefaultComboBoxModel<>();
        for (User admin : userDAO.getAllAdmins()) {
            model.addElement(admin);
        }
        adminComboBox.setModel(model);
        adminComboBox.setEnabled(model.getSize() > 0);
    }

    private void clearFields() {
        idField.setText("");
        adminComboBox.setSelectedItem(null);
        usernameField.setText("");
        passwordField.setText("");
    }

    private void setLoginMode(String loginMode) {
        this.loginMode = loginMode;
        if (registerMode) {
            return;
        }
        boolean adminSelected = ROLE_ADMIN.equals(loginMode);
        studentLoginButton.setBackground(adminSelected ? UIUtils.COLOR_PRIMARY : UIUtils.COLOR_ACCENT);
        adminLoginButton.setBackground(adminSelected ? UIUtils.COLOR_ACCENT : UIUtils.COLOR_PRIMARY);
        usernameLabel.setText(adminSelected ? "Admin Username" : "Student ID");
        usernameField.setText("");
        passwordField.setText("");
        revalidate();
        repaint();
    }

    private void showForgotPasswordDialog() {
        JTextField identifierField = new JTextField();
        JPanel requestForm = new JPanel(new GridLayout(0, 1, 6, 6));
        String roleLabel = ROLE_ADMIN.equals(loginMode) ? "Admin Username" : "Student ID";
        requestForm.add(new JLabel(roleLabel + ":"));
        requestForm.add(identifierField);

        int requestResult = JOptionPane.showConfirmDialog(this, requestForm, "Forgot Password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (requestResult != JOptionPane.OK_OPTION) {
            return;
        }

        String identifier = identifierField.getText().trim();
        if (identifier.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Login ID is required.", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean created = userDAO.createPasswordResetRequest(loginMode, identifier);
            if (created) {
                JOptionPane.showMessageDialog(this,
                        "Password reset request sent. Please contact your admin for a temporary password.");
            } else {
                JOptionPane.showMessageDialog(this, "No account matched the provided login ID.", "Reset Password",
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to create reset request: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
