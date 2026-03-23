package com.quiz.ui;

import com.quiz.dao.ExamDAO;
import com.quiz.dao.FeedbackDAO;
import com.quiz.dao.ResultDAO;
import com.quiz.model.Exam;
import com.quiz.model.User;
import com.quiz.util.UIUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class StudentDashboard extends JFrame {
    private User student;
    private ExamDAO examDAO;
    private ResultDAO resultDAO;
    private JPanel examCardsPanel;

    public StudentDashboard(User student) {
        this.student = student;
        this.examDAO = new ExamDAO();
        this.resultDAO = new ResultDAO();

        setTitle("QEMS Student | " + student.getUsername());
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        UIUtils.styleFrame(this);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtils.COLOR_WHITE);
        header.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel welcome = new JLabel("Hello, " + student.getUsername());
        welcome.setFont(UIUtils.FONT_HEADER);
        welcome.setForeground(UIUtils.COLOR_PRIMARY);
        header.add(welcome, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actions.setBackground(UIUtils.COLOR_WHITE);

        JButton feedbackBtn = new JButton("Send Feedback");
        UIUtils.styleButton(feedbackBtn);
        feedbackBtn.setBackground(UIUtils.COLOR_PRIMARY);
        feedbackBtn.addActionListener(e -> openFeedbackDialog());

        JButton logoutBtn = new JButton("Logout");
        UIUtils.styleButton(logoutBtn);
        logoutBtn.setBackground(UIUtils.COLOR_DANGER);
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        actions.add(feedbackBtn);
        actions.add(logoutBtn);
        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Main Content (Cards)
        JPanel mainContainer = UIUtils.createPaddingPanel();
        mainContainer.setLayout(new BorderLayout(0, 20));

        JLabel label = new JLabel("Available Exams");
        label.setFont(UIUtils.FONT_BOLD);
        mainContainer.add(label, BorderLayout.NORTH);

        examCardsPanel = new JPanel(new GridLayout(0, 2, 20, 20));
        examCardsPanel.setBackground(UIUtils.COLOR_WHITE);
        loadExamCards();

        mainContainer.add(new JScrollPane(examCardsPanel), BorderLayout.CENTER);

        JButton viewResultsBtn = new JButton("View My Academic History");
        UIUtils.styleButton(viewResultsBtn);
        viewResultsBtn.addActionListener(e -> showResults());
        mainContainer.add(viewResultsBtn, BorderLayout.SOUTH);

        add(mainContainer, BorderLayout.CENTER);
    }

    private void loadExamCards() {
        examCardsPanel.removeAll();
        try {
            List<Exam> exams = student.getAdminId() > 0
                    ? examDAO.getExamsByAdminId(student.getAdminId())
                    : java.util.Collections.emptyList();
            for (Exam ex : exams) {
                JPanel card = new JPanel(new BorderLayout(10, 10));
                card.setBackground(new Color(250, 250, 250));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                        BorderFactory.createEmptyBorder(15, 15, 15, 15)));

                JLabel title = new JLabel(ex.getTitle());
                title.setFont(UIUtils.FONT_BOLD);
                card.add(title, BorderLayout.NORTH);

                JLabel desc = new JLabel("<html><b>Subject:</b> " + ex.getSubject() + "<br/><br/>" + ex.getDescription() + "</html>");
                desc.setForeground(Color.GRAY);
                card.add(desc, BorderLayout.CENTER);

                JButton startBtn = new JButton("Start Quiz");
                UIUtils.styleButton(startBtn);
                boolean attempted = resultDAO.hasAttemptedExam(student.getId(), ex.getId());
                if (attempted) {
                    startBtn.setText("Already Attempted");
                    startBtn.setEnabled(false);
                } else {
                    startBtn.addActionListener(e -> {
                        ExamFrame frame = new ExamFrame(student, ex);
                        frame.addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosed(WindowEvent e1) {
                                loadExamCards();
                            }
                        });
                        frame.setVisible(true);
                    });
                }
                card.add(startBtn, BorderLayout.SOUTH);

                examCardsPanel.add(card);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading exams: " + e.getMessage());
        }
        examCardsPanel.revalidate();
        examCardsPanel.repaint();
    }

    private void openFeedbackDialog() {
        String msg = JOptionPane.showInputDialog(this, "Tell us what you think:", "Student Feedback",
                JOptionPane.PLAIN_MESSAGE);
        if (msg != null && !msg.trim().isEmpty()) {
            try {
                if (new FeedbackDAO().addFeedback(student.getId(), msg.trim())) {
                    JOptionPane.showMessageDialog(this, "Feedback sent! Thank you.");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to send feedback.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (java.sql.SQLException e) {
                JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showResults() {
        List<String> displayResults = new ResultDAO().getDetailedResultsByUserId(student.getId());
        StringBuilder sb = new StringBuilder("--- Academic Record ---\n\n");
        for (String s : displayResults)
            sb.append(s).append("\n");
        if (displayResults.isEmpty())
            sb.append("No results found. Start your first quiz!");

        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "My Results", JOptionPane.INFORMATION_MESSAGE);
    }
}
