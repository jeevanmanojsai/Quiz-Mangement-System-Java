package com.quiz.ui;

import com.quiz.dao.ExamDAO;
import com.quiz.dao.FeedbackDAO;
import com.quiz.dao.QuestionDAO;
import com.quiz.dao.ResultDAO;
import com.quiz.dao.UserDAO;
import com.quiz.model.Exam;
import com.quiz.model.Feedback;
import com.quiz.model.PasswordResetRequest;
import com.quiz.model.Question;
import com.quiz.model.User;
import com.quiz.util.UIUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class AdminDashboard extends JFrame {
    private final User currentAdmin;
    private final ExamDAO examDAO;
    private final QuestionDAO questionDAO;
    private final ResultDAO resultDAO;
    private final FeedbackDAO feedbackDAO;
    private final UserDAO userDAO;
    private JComboBox<Exam> examComboBox;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private DefaultListModel<String> analyticsModel;
    private DefaultListModel<String> feedbackModel;
    private DefaultListModel<Exam> examListModel;
    private DefaultListModel<Question> questionListModel;
    private DefaultListModel<User> studentListModel;
    private DefaultListModel<User> adminListModel;
    private DefaultListModel<PasswordResetRequest> resetRequestListModel;
    private JComboBox<Object> analyticsExamComboBox;
    private JTextField analyticsDateField;
    private JTextField analyticsTimeFromField;
    private JTextField analyticsTimeToField;

    public AdminDashboard(User currentAdmin) {
        this.currentAdmin = currentAdmin;
        examDAO = new ExamDAO();
        questionDAO = new QuestionDAO();
        resultDAO = new ResultDAO();
        feedbackDAO = new FeedbackDAO();
        userDAO = new UserDAO();

        setTitle("QEMS Admin | Management Panel");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        UIUtils.styleFrame(this);
        setLayout(new BorderLayout());

        // Side Navigation
        JPanel navPanel = new JPanel();
        navPanel.setBackground(UIUtils.COLOR_PRIMARY);
        navPanel.setPreferredSize(new Dimension(220, 0));
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(new EmptyBorder(30, 10, 30, 10));

        JLabel logo = new JLabel("QEMS ADMIN");
        logo.setForeground(UIUtils.COLOR_WHITE);
        logo.setFont(UIUtils.FONT_BOLD);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        navPanel.add(logo);
        navPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        addNavButton("Exams", "EXAMS", navPanel);
        addNavButton("Exams List", "EXAM_LIST", navPanel);
        addNavButton("Questions", "QUESTIONS", navPanel);
        addNavButton("Students", "STUDENTS", navPanel);
        addNavButton("Admins", "ADMINS", navPanel);
        addNavButton("Reset Requests", "RESET_REQUESTS", navPanel);
        addNavButton("Analytics", "ANALYTICS", navPanel);
        addNavButton("Feedbacks", "FEEDBACK", navPanel);

        navPanel.add(Box.createVerticalGlue());

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        UIUtils.styleButton(logoutBtn);
        logoutBtn.setBackground(UIUtils.COLOR_DANGER);
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        navPanel.add(logoutBtn);

        add(navPanel, BorderLayout.WEST);

        // Content Area
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIUtils.COLOR_WHITE);

        contentPanel.add(createExamPanel(), "EXAMS");
        contentPanel.add(createExamListPanel(), "EXAM_LIST");
        contentPanel.add(createQuestionPanel(), "QUESTIONS");
        contentPanel.add(createStudentPanel(), "STUDENTS");
        contentPanel.add(createAdminPanel(), "ADMINS");
        contentPanel.add(createResetRequestPanel(), "RESET_REQUESTS");
        contentPanel.add(createAnalyticsPanel(), "ANALYTICS");
        contentPanel.add(createFeedbackPanel(), "FEEDBACK");

        add(contentPanel, BorderLayout.CENTER);
        loadExams();
        refreshExams();
        refreshStudents();
        refreshAdmins();

        // Auto-refresh logic for CardLayout (nav buttons)
    }

    private void showCard(String name) {
        cardLayout.show(contentPanel, name);
        if ("ANALYTICS".equals(name))
            refreshAnalytics();
        else if ("FEEDBACK".equals(name))
            refreshFeedbacks();
        else if ("EXAM_LIST".equals(name))
            refreshExams();
        else if ("QUESTIONS".equals(name))
            refreshQuestionsForSelectedExam();
        else if ("STUDENTS".equals(name))
            refreshStudents();
        else if ("ADMINS".equals(name))
            refreshAdmins();
        else if ("RESET_REQUESTS".equals(name))
            refreshResetRequests();
    }

    private void addNavButton(String text, String cardName, JPanel parent) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFont(UIUtils.FONT_REGULAR);
        btn.setForeground(UIUtils.COLOR_WHITE);
        btn.setBackground(UIUtils.COLOR_PRIMARY);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> showCard(cardName));
        parent.add(btn);
        parent.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    private JPanel createExamPanel() {
        JPanel p = UIUtils.createPaddingPanel();
        p.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        JTextField title = new JTextField();
        UIUtils.setRounding(title);
        JTextField subject = new JTextField();
        UIUtils.setRounding(subject);
        JTextArea desc = new JTextArea(4, 20);
        UIUtils.setRounding(desc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        p.add(new JLabel("Exam Title:"), gbc);
        gbc.gridx = 1;
        p.add(title, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        p.add(new JLabel("Subject:"), gbc);
        gbc.gridx = 1;
        p.add(subject, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        p.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        p.add(new JScrollPane(desc), gbc);

        JButton saveBtn = new JButton("Create New Exam");
        UIUtils.styleButton(saveBtn);
        saveBtn.addActionListener(e -> {
            try {
                String titleText = title.getText().trim();
                String subjectText = subject.getText().trim();
                if (titleText.isEmpty() || subjectText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Exam title and subject are required.", "Validation",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (examDAO.addExam(new Exam(0, titleText, desc.getText().trim(), subjectText, currentAdmin.getId()))) {
                    JOptionPane.showMessageDialog(this, "Exam Saved!");
                    loadExams();
                    title.setText("");
                    subject.setText("");
                    desc.setText("");
                }
            } catch (java.sql.SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        p.add(saveBtn, gbc);
        return p;
    }

    private JPanel createExamListPanel() {
        JPanel p = UIUtils.createPaddingPanel();
        p.setLayout(new BorderLayout(0, 20));

        JLabel title = new JLabel("Existing Exams (CRUD)");
        title.setFont(UIUtils.FONT_BOLD);
        p.add(title, BorderLayout.NORTH);

        examListModel = new DefaultListModel<>();
        JList<Exam> list = new JList<>(examListModel);

        JButton refreshBtn = new JButton("Refresh Exams");
        UIUtils.styleButton(refreshBtn);
        refreshBtn.addActionListener(e -> refreshExams());

        JButton deleteBtn = new JButton("Delete Selected Exam");
        UIUtils.styleButton(deleteBtn);
        deleteBtn.setBackground(UIUtils.COLOR_DANGER);
        deleteBtn.addActionListener(e -> {
            Exam ex = list.getSelectedValue();
            if (ex != null) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Delete " + ex.getTitle() + "? All questions/results will be lost.", "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        if (examDAO.deleteExam(ex.getId(), currentAdmin.getId())) {
                            JOptionPane.showMessageDialog(this, "Deleted!");
                            refreshExams();
                            loadExams();
                        }
                    } catch (java.sql.SQLException ex2) {
                        JOptionPane.showMessageDialog(this, "Database Error: " + ex2.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        JButton updateBtn = new JButton("Update Selected Exam");
        UIUtils.styleButton(updateBtn);
        updateBtn.addActionListener(e -> {
            Exam ex = list.getSelectedValue();
            if (ex == null)
                return;

            JTextField titleField = new JTextField(ex.getTitle());
            JTextField subjectField = new JTextField(ex.getSubject());
            JTextArea descArea = new JTextArea(ex.getDescription(), 5, 20);
            JScrollPane descScroll = new JScrollPane(descArea);
            JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
            form.add(new JLabel("Exam Title:"));
            form.add(titleField);
            form.add(new JLabel("Subject:"));
            form.add(subjectField);
            form.add(new JLabel("Description:"));
            form.add(descScroll);

            int result = JOptionPane.showConfirmDialog(this, form, "Update Exam", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    ex.setTitle(titleField.getText().trim());
                    ex.setSubject(subjectField.getText().trim());
                    ex.setDescription(descArea.getText().trim());
                    if (examDAO.updateExam(ex)) {
                        JOptionPane.showMessageDialog(this, "Exam Updated!");
                        refreshExams();
                        loadExams();
                    }
                } catch (java.sql.SQLException ex2) {
                    JOptionPane.showMessageDialog(this, "Database Error: " + ex2.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton previewBtn = new JButton("Preview Selected Exam");
        UIUtils.styleButton(previewBtn);
        previewBtn.addActionListener(e -> {
            Exam ex = list.getSelectedValue();
            if (ex == null) {
                JOptionPane.showMessageDialog(this, "Select an exam to preview.", "Preview Exam",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            showExamPreview(ex);
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setBackground(UIUtils.COLOR_WHITE);
        btnPanel.add(refreshBtn);
        btnPanel.add(previewBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);

        p.add(new JScrollPane(list), BorderLayout.CENTER);
        p.add(btnPanel, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createQuestionPanel() {
        JPanel p = UIUtils.createPaddingPanel();
        p.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        examComboBox = new JComboBox<>();
        loadExams();
        JTextField q = new JTextField();
        UIUtils.setRounding(q);
        JTextField a = new JTextField();
        UIUtils.setRounding(a);
        JTextField b = new JTextField();
        UIUtils.setRounding(b);
        JTextField c = new JTextField();
        UIUtils.setRounding(c);
        JTextField d = new JTextField();
        UIUtils.setRounding(d);
        JComboBox<String> correct = new JComboBox<>(new String[] { "A", "B", "C", "D" });
        questionListModel = new DefaultListModel<>();
        JList<Question> questionList = new JList<>(questionListModel);
        questionList.setCellRenderer((jList, question, index, isSelected, cellHasFocus) -> {
            DefaultListCellRenderer renderer = new DefaultListCellRenderer();
            JLabel label = (JLabel) renderer.getListCellRendererComponent(jList, question, index, isSelected,
                    cellHasFocus);
            label.setText((index + 1) + ". " + question.getQuestionText());
            return label;
        });
        questionList.setVisibleRowCount(8);

        addRow(p, "Target Exam:", examComboBox, gbc, 0);
        addRow(p, "Question:", q, gbc, 1);
        addRow(p, "Opt A:", a, gbc, 2);
        addRow(p, "Opt B:", b, gbc, 3);
        addRow(p, "Opt C:", c, gbc, 4);
        addRow(p, "Opt D:", d, gbc, 5);
        addRow(p, "Correct:", correct, gbc, 6);

        JButton addBtn = new JButton("Add Question");
        UIUtils.styleButton(addBtn);
        addBtn.addActionListener(e -> {
            Exam ex = (Exam) examComboBox.getSelectedItem();
            if (ex != null && questionDAO.addQuestion(new Question(0, ex.getId(), q.getText(), a.getText(), b.getText(),
                    c.getText(), d.getText(), (String) correct.getSelectedItem()))) {
                JOptionPane.showMessageDialog(this, "Question Added!");
                clearQuestionForm(q, a, b, c, d, correct);
                refreshQuestionsForSelectedExam();
            }
        });

        JButton updateBtn = new JButton("Update Selected");
        UIUtils.styleButton(updateBtn);
        updateBtn.addActionListener(e -> {
            Question selected = questionList.getSelectedValue();
            Exam ex = (Exam) examComboBox.getSelectedItem();
            if (selected == null || ex == null)
                return;

            Question updated = new Question(selected.getId(), ex.getId(), q.getText(), a.getText(), b.getText(),
                    c.getText(), d.getText(), (String) correct.getSelectedItem());
            if (questionDAO.updateQuestion(updated)) {
                JOptionPane.showMessageDialog(this, "Question Updated!");
                refreshQuestionsForSelectedExam();
                clearQuestionForm(q, a, b, c, d, correct);
            }
        });

        JButton deleteBtn = new JButton("Delete Selected");
        UIUtils.styleButton(deleteBtn);
        deleteBtn.setBackground(UIUtils.COLOR_DANGER);
        deleteBtn.addActionListener(e -> {
            Question selected = questionList.getSelectedValue();
            if (selected == null)
                return;
            int choice = JOptionPane.showConfirmDialog(this, "Delete selected question?", "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION && questionDAO.deleteQuestion(selected.getId())) {
                JOptionPane.showMessageDialog(this, "Question Deleted!");
                refreshQuestionsForSelectedExam();
                clearQuestionForm(q, a, b, c, d, correct);
            }
        });

        JButton clearBtn = new JButton("Clear Form");
        UIUtils.styleButton(clearBtn);
        clearBtn.addActionListener(e -> {
            questionList.clearSelection();
            clearQuestionForm(q, a, b, c, d, correct);
        });

        JButton loadBtn = new JButton("Load Questions");
        UIUtils.styleButton(loadBtn);
        loadBtn.addActionListener(e -> refreshQuestionsForSelectedExam());

        examComboBox.addActionListener(e -> refreshQuestionsForSelectedExam());
        questionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && questionList.getSelectedValue() != null) {
                populateQuestionForm(questionList.getSelectedValue(), q, a, b, c, d, correct);
            }
        });

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttonRow.setBackground(UIUtils.COLOR_WHITE);
        buttonRow.add(addBtn);
        buttonRow.add(updateBtn);
        buttonRow.add(deleteBtn);
        buttonRow.add(loadBtn);
        buttonRow.add(clearBtn);

        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        p.add(buttonRow, gbc);

        gbc.gridy = 8;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        p.add(new JScrollPane(questionList), gbc);
        return p;
    }

    private JPanel createStudentPanel() {
        JPanel p = UIUtils.createPaddingPanel();
        p.setLayout(new BorderLayout(0, 20));

        JLabel title = new JLabel("Student Management");
        title.setFont(UIUtils.FONT_BOLD);
        p.add(title, BorderLayout.NORTH);

        studentListModel = new DefaultListModel<>();
        JList<User> list = createOrderedUserList(studentListModel);

        JButton refreshBtn = new JButton("Refresh Students");
        UIUtils.styleButton(refreshBtn);
        refreshBtn.addActionListener(e -> refreshStudents());

        JButton addBtn = new JButton("Add Student");
        UIUtils.styleButton(addBtn);
        addBtn.addActionListener(e -> showUserDialog("Add Student", null, "STUDENT"));

        JButton updateBtn = new JButton("Update Selected");
        UIUtils.styleButton(updateBtn);
        updateBtn.addActionListener(e -> {
            User selected = list.getSelectedValue();
            if (selected == null)
                return;
            showUserDialog("Update Student", selected, "STUDENT");
        });

        JButton deleteBtn = new JButton("Delete Selected");
        UIUtils.styleButton(deleteBtn);
        deleteBtn.setBackground(UIUtils.COLOR_DANGER);
        deleteBtn.addActionListener(e -> {
            User selected = list.getSelectedValue();
            if (selected == null)
                return;
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete student '" + selected.getUsername() + "'?\nThis also removes their results and feedback.",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (userDAO.deleteStudent(selected.getId())) {
                        JOptionPane.showMessageDialog(this, "Student deleted.");
                        refreshStudents();
                    }
                } catch (java.sql.SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setBackground(UIUtils.COLOR_WHITE);
        btnPanel.add(refreshBtn);
        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);

        p.add(new JScrollPane(list), BorderLayout.CENTER);
        p.add(btnPanel, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createAdminPanel() {
        JPanel p = UIUtils.createPaddingPanel();
        p.setLayout(new BorderLayout(0, 20));

        JLabel title = new JLabel("Admin Management");
        title.setFont(UIUtils.FONT_BOLD);
        p.add(title, BorderLayout.NORTH);

        adminListModel = new DefaultListModel<>();
        JList<User> list = createOrderedUserList(adminListModel);

        JButton refreshBtn = new JButton("Refresh Admins");
        UIUtils.styleButton(refreshBtn);
        refreshBtn.addActionListener(e -> refreshAdmins());

        JButton addBtn = new JButton("Add Admin");
        UIUtils.styleButton(addBtn);
        addBtn.addActionListener(e -> showUserDialog("Add Admin", null, "ADMIN"));

        JButton updateBtn = new JButton("Update Selected");
        UIUtils.styleButton(updateBtn);
        updateBtn.addActionListener(e -> {
            User selected = list.getSelectedValue();
            if (selected == null)
                return;
            showUserDialog("Update Admin", selected, "ADMIN");
        });

        JButton deleteBtn = new JButton("Delete Selected");
        UIUtils.styleButton(deleteBtn);
        deleteBtn.setBackground(UIUtils.COLOR_DANGER);
        deleteBtn.addActionListener(e -> {
            User selected = list.getSelectedValue();
            if (selected == null)
                return;
            if (selected.getId() == currentAdmin.getId()) {
                JOptionPane.showMessageDialog(this, "You cannot delete the admin account currently signed in.",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete admin '" + selected.getUsername() + "'?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (userDAO.deleteAdmin(selected.getId())) {
                        JOptionPane.showMessageDialog(this, "Admin deleted.");
                        refreshAdmins();
                    }
                } catch (java.sql.SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setBackground(UIUtils.COLOR_WHITE);
        btnPanel.add(refreshBtn);
        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);

        p.add(new JScrollPane(list), BorderLayout.CENTER);
        p.add(btnPanel, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createResetRequestPanel() {
        JPanel p = UIUtils.createPaddingPanel();
        p.setLayout(new BorderLayout(0, 20));

        JLabel title = new JLabel("Password Reset Requests");
        title.setFont(UIUtils.FONT_BOLD);
        p.add(title, BorderLayout.NORTH);

        resetRequestListModel = new DefaultListModel<>();
        JList<PasswordResetRequest> list = new JList<>(resetRequestListModel);
        list.setCellRenderer((jList, request, index, isSelected, cellHasFocus) -> {
            DefaultListCellRenderer renderer = new DefaultListCellRenderer();
            JLabel label = (JLabel) renderer.getListCellRendererComponent(jList, request, index, isSelected,
                    cellHasFocus);
            String requestedAt = request.getCreatedAt() == null ? "Unknown time" : request.getCreatedAt().toString();
            label.setText(request.getDisplayName() + " | Requested: " + requestedAt);
            return label;
        });

        JButton refreshBtn = new JButton("Refresh Requests");
        UIUtils.styleButton(refreshBtn);
        refreshBtn.addActionListener(e -> refreshResetRequests());

        JButton approveBtn = new JButton("Set Temporary Password");
        UIUtils.styleButton(approveBtn);
        approveBtn.addActionListener(e -> approveSelectedResetRequest(list.getSelectedValue()));

        JButton rejectBtn = new JButton("Reject Request");
        UIUtils.styleButton(rejectBtn);
        rejectBtn.setBackground(UIUtils.COLOR_DANGER);
        rejectBtn.addActionListener(e -> rejectSelectedResetRequest(list.getSelectedValue()));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setBackground(UIUtils.COLOR_WHITE);
        btnPanel.add(refreshBtn);
        btnPanel.add(approveBtn);
        btnPanel.add(rejectBtn);

        p.add(new JScrollPane(list), BorderLayout.CENTER);
        p.add(btnPanel, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createAnalyticsPanel() {
        JPanel p = UIUtils.createPaddingPanel();
        p.setLayout(new BorderLayout(0, 20));

        JLabel title = new JLabel("Student Performance Analytics");
        title.setFont(UIUtils.FONT_BOLD);

        analyticsExamComboBox = new JComboBox<>();
        analyticsExamComboBox.addItem("All Exams");
        analyticsExamComboBox.setPreferredSize(new Dimension(220, 32));
        analyticsExamComboBox.addActionListener(e -> refreshAnalytics());

        analyticsDateField = new JTextField(10);
        analyticsDateField.setPreferredSize(new Dimension(120, 32));
        UIUtils.setRounding(analyticsDateField);

        analyticsTimeFromField = new JTextField(8);
        analyticsTimeFromField.setPreferredSize(new Dimension(100, 32));
        UIUtils.setRounding(analyticsTimeFromField);

        analyticsTimeToField = new JTextField(8);
        analyticsTimeToField.setPreferredSize(new Dimension(100, 32));
        UIUtils.setRounding(analyticsTimeToField);

        JButton refreshBtn = new JButton("Refresh Data");
        UIUtils.styleButton(refreshBtn);
        refreshBtn.addActionListener(e -> refreshAnalytics());

        JButton submitBtn = new JButton("Submit");
        UIUtils.styleButton(submitBtn);
        submitBtn.addActionListener(e -> refreshAnalytics());

        JButton downloadBtn = new JButton("Download Result");
        UIUtils.styleButton(downloadBtn);
        downloadBtn.addActionListener(e -> downloadSelectedResults());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(UIUtils.COLOR_WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(title);
        topPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBackground(UIUtils.COLOR_WHITE);
        filterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints filterGbc = new GridBagConstraints();
        filterGbc.insets = new Insets(0, 0, 8, 10);
        filterGbc.anchor = GridBagConstraints.WEST;
        filterGbc.gridy = 0;
        filterGbc.gridx = 0;
        filterPanel.add(new JLabel("Exam:"), filterGbc);
        filterGbc.gridx = 1;
        filterGbc.gridwidth = 5;
        filterGbc.fill = GridBagConstraints.HORIZONTAL;
        filterGbc.weightx = 1.0;
        filterPanel.add(analyticsExamComboBox, filterGbc);

        filterGbc.gridy = 1;
        filterGbc.gridx = 0;
        filterGbc.gridwidth = 1;
        filterGbc.weightx = 0;
        filterGbc.fill = GridBagConstraints.NONE;
        filterPanel.add(new JLabel("Date:"), filterGbc);
        filterGbc.gridx = 1;
        filterPanel.add(analyticsDateField, filterGbc);
        filterGbc.gridx = 2;
        filterPanel.add(new JLabel("Time From (24h):"), filterGbc);
        filterGbc.gridx = 3;
        filterPanel.add(analyticsTimeFromField, filterGbc);
        filterGbc.gridx = 4;
        filterPanel.add(new JLabel("Time To (24h):"), filterGbc);
        filterGbc.gridx = 5;
        filterGbc.insets = new Insets(0, 0, 0, 0);
        filterPanel.add(analyticsTimeToField, filterGbc);
        topPanel.add(filterPanel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionPanel.setBackground(UIUtils.COLOR_WHITE);
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionPanel.add(submitBtn);
        actionPanel.add(refreshBtn);
        actionPanel.add(downloadBtn);
        topPanel.add(actionPanel);

        p.add(topPanel, BorderLayout.NORTH);

        analyticsModel = new DefaultListModel<>();
        JList<String> list = new JList<>(analyticsModel);
        list.setFont(new Font("Monospaced", Font.PLAIN, 13));

        p.add(new JScrollPane(list), BorderLayout.CENTER);
        return p;
    }

    private JPanel createFeedbackPanel() {
        JPanel p = UIUtils.createPaddingPanel();
        p.setLayout(new BorderLayout(0, 20));

        JLabel title = new JLabel("Student Feedbacks");
        title.setFont(UIUtils.FONT_BOLD);
        p.add(title, BorderLayout.NORTH);

        feedbackModel = new DefaultListModel<>();
        JList<String> list = new JList<>(feedbackModel);

        JButton refreshBtn = new JButton("Fetch Feedbacks");
        UIUtils.styleButton(refreshBtn);
        refreshBtn.addActionListener(e -> refreshFeedbacks());

        p.add(new JScrollPane(list), BorderLayout.CENTER);
        p.add(refreshBtn, BorderLayout.SOUTH);
        return p;
    }

    private void addRow(JPanel p, String label, JComponent c, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        p.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        p.add(c, gbc);
    }

    private JList<User> createOrderedUserList(DefaultListModel<User> model) {
        JList<User> list = new JList<>(model);
        list.setCellRenderer((jList, user, index, isSelected, cellHasFocus) -> {
            DefaultListCellRenderer renderer = new DefaultListCellRenderer();
            JLabel label = (JLabel) renderer.getListCellRendererComponent(jList, user, index, isSelected,
                    cellHasFocus);
            label.setText(user.getDisplayId() + " - " + user.getUsername());
            return label;
        });
        return list;
    }

    private void loadExams() {
        if (examComboBox != null) {
            Exam selected = (Exam) examComboBox.getSelectedItem();
            Integer selectedId = selected == null ? null : selected.getId();
            examComboBox.removeAllItems();
            for (Exam e : examDAO.getExamsByAdminId(currentAdmin.getId()))
                examComboBox.addItem(e);
            if (selectedId != null) {
                for (int i = 0; i < examComboBox.getItemCount(); i++) {
                    Exam item = examComboBox.getItemAt(i);
                    if (item.getId() == selectedId) {
                        examComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
        loadAnalyticsExamFilter();
        refreshQuestionsForSelectedExam();
    }

    private void refreshAnalytics() {
        if (analyticsModel == null)
            return;
        analyticsModel.clear();
        Integer examId = null;
        if (analyticsExamComboBox != null) {
            Object selected = analyticsExamComboBox.getSelectedItem();
            if (selected instanceof Exam) {
                examId = ((Exam) selected).getId();
            }
        }
        String dateFilter = normalizeAnalyticsDate();
        if (dateFilter == null && hasText(analyticsDateField)) {
            return;
        }
        String timeFromFilter = normalizeAnalyticsTime(analyticsTimeFromField, "start");
        if (timeFromFilter == null && hasText(analyticsTimeFromField)) {
            return;
        }
        String timeToFilter = normalizeAnalyticsTime(analyticsTimeToField, "end");
        if (timeToFilter == null && hasText(analyticsTimeToField)) {
            return;
        }
        if (!validateAnalyticsTimeRange(timeFromFilter, timeToFilter)) {
            return;
        }
        List<String> results = resultDAO.getResultsForAdmin(currentAdmin.getId(), examId, dateFilter, timeFromFilter,
                timeToFilter);
        if (results.isEmpty())
            analyticsModel.addElement("No student results yet.");
        else
            for (String s : results)
                analyticsModel.addElement(s);
    }

    private void loadAnalyticsExamFilter() {
        if (analyticsExamComboBox == null) {
            return;
        }
        Object selected = analyticsExamComboBox.getSelectedItem();
        Integer selectedExamId = selected instanceof Exam ? ((Exam) selected).getId() : null;
        analyticsExamComboBox.removeAllItems();
        analyticsExamComboBox.addItem("All Exams");
        for (Exam exam : examDAO.getExamsByAdminId(currentAdmin.getId())) {
            analyticsExamComboBox.addItem(exam);
        }
        if (selectedExamId != null) {
            for (int i = 0; i < analyticsExamComboBox.getItemCount(); i++) {
                Object item = analyticsExamComboBox.getItemAt(i);
                if (item instanceof Exam && ((Exam) item).getId() == selectedExamId) {
                    analyticsExamComboBox.setSelectedIndex(i);
                    return;
                }
            }
        }
        analyticsExamComboBox.setSelectedIndex(0);
    }

    private void refreshFeedbacks() {
        if (feedbackModel == null)
            return;
        feedbackModel.clear();
        List<Feedback> feedbacks = feedbackDAO.getFeedbacksByAdminId(currentAdmin.getId());
        if (feedbacks.isEmpty())
            feedbackModel.addElement("No student feedbacks yet.");
        else
            for (Feedback f : feedbacks) {
                feedbackModel.addElement("From: " + (f.getUsername() == null ? "Unknown" : f.getUsername()) + " | MSG: "
                        + f.getMessage());
            }
    }

    private void refreshExams() {
        if (examListModel == null)
            return;
        examListModel.clear();
        for (Exam ex : examDAO.getExamsByAdminId(currentAdmin.getId()))
            examListModel.addElement(ex);
    }

    private void refreshStudents() {
        if (studentListModel == null)
            return;
        studentListModel.clear();
        for (User student : userDAO.getStudentsByAdminId(currentAdmin.getId())) {
            studentListModel.addElement(student);
        }
    }

    private void refreshAdmins() {
        if (adminListModel == null)
            return;
        adminListModel.clear();
        for (User admin : userDAO.getAllAdmins()) {
            adminListModel.addElement(admin);
        }
    }

    private void refreshResetRequests() {
        if (resetRequestListModel == null)
            return;
        resetRequestListModel.clear();
        for (PasswordResetRequest request : userDAO.getPendingResetRequestsForAdmin(currentAdmin.getId())) {
            resetRequestListModel.addElement(request);
        }
    }

    private void downloadSelectedResults() {
        Object selected = analyticsExamComboBox == null ? null : analyticsExamComboBox.getSelectedItem();
        Integer examId = selected instanceof Exam ? ((Exam) selected).getId() : null;
        String dateFilter = normalizeAnalyticsDate();
        if (dateFilter == null && hasText(analyticsDateField)) {
            return;
        }
        String timeFromFilter = normalizeAnalyticsTime(analyticsTimeFromField, "start");
        if (timeFromFilter == null && hasText(analyticsTimeFromField)) {
            return;
        }
        String timeToFilter = normalizeAnalyticsTime(analyticsTimeToField, "end");
        if (timeToFilter == null && hasText(analyticsTimeToField)) {
            return;
        }
        if (!validateAnalyticsTimeRange(timeFromFilter, timeToFilter)) {
            return;
        }
        String defaultFileName = "all_students_results.csv";
        if (selected instanceof Exam) {
            defaultFileName = (((Exam) selected).getSubject() + "_" + ((Exam) selected).getTitle())
                    .replaceAll("[^A-Za-z0-9._-]+", "_")
                    + "_results.csv";
        }

        List<String[]> rows = resultDAO.getResultsForExport(currentAdmin.getId(), examId, dateFilter, timeFromFilter,
                timeToFilter);
        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No results available to download.", "No Data",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Results CSV");
        chooser.setSelectedFile(new File(defaultFileName));
        int choice = chooser.showSaveDialog(this);
        if (choice != JFileChooser.APPROVE_OPTION)
            return;

        File outFile = chooser.getSelectedFile();
        try (FileWriter writer = new FileWriter(outFile)) {
            writer.write("Rank,Roll Number or ID,Exam Title,Score,Total Questions,Taken At\n");
            for (String[] row : rows) {
                writer.write(csv(row[0]) + "," + csv(row[1]) + "," + csv(row[2]) + "," + csv(row[3]) + ","
                        + csv(row[4]) + "," + csv(row[5]) + "\n");
            }
            JOptionPane.showMessageDialog(this, "Results downloaded successfully:\n" + outFile.getAbsolutePath(),
                    "Download Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to write file: " + e.getMessage(), "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String csv(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String normalizeAnalyticsDate() {
        if (!hasText(analyticsDateField)) {
            return null;
        }
        try {
            return LocalDate.parse(analyticsDateField.getText().trim(), DateTimeFormatter.ISO_LOCAL_DATE).toString();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Enter date in YYYY-MM-DD format.", "Invalid Date",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

    private String normalizeAnalyticsTime(JTextField field, String label) {
        if (!hasText(field)) {
            return null;
        }
        String value = field.getText().trim();
        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("HH:mm:ss")
        };
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalTime.parse(value, formatter).format(DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException ignored) {
            }
        }
        JOptionPane.showMessageDialog(this, "Enter " + label + " time in 24-hour HH:MM or HH:MM:SS format.",
                "Invalid Time",
                JOptionPane.WARNING_MESSAGE);
        return null;
    }

    private boolean validateAnalyticsTimeRange(String timeFrom, String timeTo) {
        if (timeFrom == null || timeTo == null) {
            return true;
        }
        if (LocalTime.parse(timeFrom).isAfter(LocalTime.parse(timeTo))) {
            JOptionPane.showMessageDialog(this, "Start time must be earlier than or equal to end time.",
                    "Invalid Time Range", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean hasText(JTextField field) {
        return field != null && field.getText() != null && !field.getText().trim().isEmpty();
    }

    private void clearQuestionForm(JTextField q, JTextField a, JTextField b, JTextField c, JTextField d,
            JComboBox<String> correct) {
        q.setText("");
        a.setText("");
        b.setText("");
        c.setText("");
        d.setText("");
        correct.setSelectedIndex(0);
    }

    private void populateQuestionForm(Question question, JTextField q, JTextField a, JTextField b, JTextField c,
            JTextField d, JComboBox<String> correct) {
        q.setText(question.getQuestionText());
        a.setText(question.getOptionA());
        b.setText(question.getOptionB());
        c.setText(question.getOptionC());
        d.setText(question.getOptionD());
        correct.setSelectedItem(question.getCorrectOption());
    }

    private void refreshQuestionsForSelectedExam() {
        if (questionListModel == null || examComboBox == null)
            return;
        questionListModel.clear();
        Exam selected = (Exam) examComboBox.getSelectedItem();
        if (selected == null)
            return;
        for (Question question : questionDAO.getQuestionsByExamId(selected.getId()))
            questionListModel.addElement(question);
    }

    private void showExamPreview(Exam exam) {
        List<Question> questions = questionDAO.getQuestionsByExamId(exam.getId());

        JTextArea previewArea = new JTextArea();
        previewArea.setEditable(false);
        previewArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        previewArea.setLineWrap(true);
        previewArea.setWrapStyleWord(true);

        if (questions.isEmpty()) {
            previewArea.setText("No questions found for this exam.");
        } else {
            StringBuilder preview = new StringBuilder();
            preview.append(exam.getSubject()).append(" | ").append(exam.getTitle()).append("\n\n");
            for (int i = 0; i < questions.size(); i++) {
                Question question = questions.get(i);
                preview.append("Question ").append(i + 1).append(":\n");
                preview.append(question.getQuestionText()).append("\n");
                preview.append("A. ").append(question.getOptionA()).append("\n");
                preview.append("B. ").append(question.getOptionB()).append("\n");
                preview.append("C. ").append(question.getOptionC()).append("\n");
                preview.append("D. ").append(question.getOptionD()).append("\n\n");
            }
            previewArea.setText(preview.toString());
            previewArea.setCaretPosition(0);
        }

        JScrollPane scrollPane = new JScrollPane(previewArea);
        scrollPane.setPreferredSize(new Dimension(700, 450));
        JOptionPane.showMessageDialog(this, scrollPane, "Preview: " + exam.getTitle(), JOptionPane.PLAIN_MESSAGE);
    }

    private void showUserDialog(String dialogTitle, User selectedUser, String role) {
        boolean isStudent = "STUDENT".equals(role);
        JTextField idField = new JTextField(selectedUser == null ? "Auto-generated" : String.valueOf(selectedUser.getId()));
        idField.setEditable(false);
        JTextField usernameField = new JTextField(selectedUser == null ? "" : selectedUser.getUsername());
        JPasswordField passwordField = new JPasswordField();
        JTextField studentCodeField = new JTextField(selectedUser == null ? "" : selectedUser.getStudentCode());
        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(new JLabel("System ID:"));
        form.add(idField);
        if (isStudent) {
            form.add(new JLabel("Student ID Code:"));
            form.add(studentCodeField);
        }
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel(selectedUser == null ? "Password:" : "Password (leave blank to keep current):"));
        form.add(passwordField);

        int result = JOptionPane.showConfirmDialog(this, form, dialogTitle, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION)
            return;

        String studentCode = studentCodeField.getText().trim().toUpperCase();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        boolean creatingUser = selectedUser == null;
        if ((isStudent && studentCode.isEmpty()) || username.isEmpty() || (creatingUser && password.isEmpty())) {
            JOptionPane.showMessageDialog(this, "Student ID, username and password are required.", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (isStudent && !studentCode.matches("[A-Z0-9]+")) {
            JOptionPane.showMessageDialog(this, "Student ID must contain only letters and numbers.", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        User target = selectedUser == null
                ? new User(0, isStudent ? studentCode : null, username, password, role,
                        isStudent ? currentAdmin.getId() : 0)
                : selectedUser;
        target.setStudentCode(isStudent ? studentCode : null);
        target.setUsername(username);
        target.setPassword(password);
        if (isStudent) {
            target.setAdminId(currentAdmin.getId());
        } else {
            target.setAdminId(0);
        }

        try {
            if ("ADMIN".equals(role)) {
                Integer excludeId = selectedUser == null ? null : selectedUser.getId();
                if (userDAO.adminUsernameExists(username, excludeId)) {
                    JOptionPane.showMessageDialog(this, "Admin username already exists.", "Validation",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } else {
                Integer excludeId = selectedUser == null ? null : selectedUser.getId();
                if (userDAO.studentCodeExists(studentCode, excludeId)) {
                    JOptionPane.showMessageDialog(this, "Student ID already exists.", "Validation",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            boolean success;
            if ("ADMIN".equals(role)) {
                success = selectedUser == null ? userDAO.addAdmin(target) : userDAO.updateAdmin(target);
            } else {
                success = selectedUser == null ? userDAO.addStudent(target) : userDAO.updateStudent(target);
            }

            if (success) {
                JOptionPane.showMessageDialog(this, role + " account saved successfully.");
                if ("ADMIN".equals(role))
                    refreshAdmins();
                else
                    refreshStudents();
            }
        } catch (java.sql.SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this,
                    ("STUDENT".equals(role) ? "Student ID already exists."
                            : "Username already exists."),
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void approveSelectedResetRequest(PasswordResetRequest request) {
        if (request == null) {
            JOptionPane.showMessageDialog(this, "Select a reset request first.", "Reset Requests",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JPasswordField tempPasswordField = new JPasswordField();
        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(new JLabel("Temporary password for " + request.getDisplayName() + ":"));
        form.add(tempPasswordField);

        int result = JOptionPane.showConfirmDialog(this, form, "Set Temporary Password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String temporaryPassword = new String(tempPasswordField.getPassword()).trim();
        if (temporaryPassword.length() < 6) {
            JOptionPane.showMessageDialog(this, "Temporary password must be at least 6 characters long.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (userDAO.approvePasswordResetRequest(request.getId(), currentAdmin.getId(), temporaryPassword)) {
                JOptionPane.showMessageDialog(this,
                        "Temporary password set successfully. Share it securely with the user.");
                refreshResetRequests();
            }
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rejectSelectedResetRequest(PasswordResetRequest request) {
        if (request == null) {
            JOptionPane.showMessageDialog(this, "Select a reset request first.", "Reset Requests",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Reject reset request for '" + request.getUsername() + "'?", "Reject Request",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (userDAO.rejectPasswordResetRequest(request.getId(), currentAdmin.getId())) {
                JOptionPane.showMessageDialog(this, "Reset request rejected.");
                refreshResetRequests();
            }
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
