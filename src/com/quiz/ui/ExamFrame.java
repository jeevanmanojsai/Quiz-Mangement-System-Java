package com.quiz.ui;

import com.quiz.dao.QuestionDAO;
import com.quiz.dao.ResultDAO;
import com.quiz.model.Exam;
import com.quiz.model.Question;
import com.quiz.model.Result;
import com.quiz.model.User;
import com.quiz.util.UIUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Timestamp;
import java.util.List;

public class ExamFrame extends JFrame {
    private User student;
    private Exam exam;
    private ResultDAO resultDAO;
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private JRadioButton optA, optB, optC, optD;
    private ButtonGroup optionsGroup;
    private JLabel questionLabel, counterLabel;
    private JButton nextButton, submitButton;

    public ExamFrame(User student, Exam exam) {
        this.student = student;
        this.exam = exam;
        this.resultDAO = new ResultDAO();
        this.questions = new QuestionDAO().getQuestionsByExamId(exam.getId());

        if (resultDAO.hasAttemptedExam(student.getId(), exam.getId())) {
            JOptionPane.showMessageDialog(null, "You have already attempted this quiz. Retake is not allowed.",
                    "Attempt Blocked", JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }

        setTitle("QEMS - " + exam.getSubject() + " - " + exam.getTitle());
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        UIUtils.styleFrame(this);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtils.COLOR_PRIMARY);
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel(exam.getSubject() + " | " + exam.getTitle());
        titleLabel.setFont(UIUtils.FONT_BOLD);
        titleLabel.setForeground(UIUtils.COLOR_WHITE);
        header.add(titleLabel, BorderLayout.WEST);

        counterLabel = new JLabel("Question 1 of " + questions.size());
        counterLabel.setForeground(UIUtils.COLOR_WHITE);
        header.add(counterLabel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel mainPanel = UIUtils.createPaddingPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        questionLabel = new JLabel();
        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(questionLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        optA = styleOption(new JRadioButton());
        optB = styleOption(new JRadioButton());
        optC = styleOption(new JRadioButton());
        optD = styleOption(new JRadioButton());

        optionsGroup = new ButtonGroup();
        optionsGroup.add(optA);
        optionsGroup.add(optB);
        optionsGroup.add(optC);
        optionsGroup.add(optD);

        mainPanel.add(optA);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(optB);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(optC);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(optD);
        add(mainPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        btnPanel.setBackground(UIUtils.COLOR_BACKGROUND);

        nextButton = new JButton("Next Question");
        UIUtils.styleButton(nextButton);

        submitButton = new JButton("Finish & Submit");
        UIUtils.styleButton(submitButton);
        submitButton.setBackground(new Color(39, 174, 96));
        submitButton.setEnabled(false);

        nextButton.addActionListener(e -> nextQuestion());
        submitButton.addActionListener(e -> submitExam());

        btnPanel.add(nextButton);
        btnPanel.add(submitButton);
        add(btnPanel, BorderLayout.SOUTH);

        if (questions.isEmpty()) {
            questionLabel.setText("No questions found for this exam.");
            nextButton.setEnabled(false);
        } else {
            displayQuestion(0);
        }
    }

    private JRadioButton styleOption(JRadioButton rb) {
        rb.setFont(UIUtils.FONT_REGULAR);
        rb.setBackground(UIUtils.COLOR_BACKGROUND);
        rb.setAlignmentX(Component.CENTER_ALIGNMENT);
        rb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return rb;
    }

    private void displayQuestion(int index) {
        Question q = questions.get(index);
        questionLabel.setText("<html><center>" + q.getQuestionText() + "</center></html>");
        optA.setText(q.getOptionA());
        optB.setText(q.getOptionB());
        optC.setText(q.getOptionC());
        optD.setText(q.getOptionD());
        optionsGroup.clearSelection();
        counterLabel.setText("Question " + (index + 1) + " of " + questions.size());
    }

    private void nextQuestion() {
        if (currentQuestionIndex < questions.size() - 1) {
            checkAnswer();
            currentQuestionIndex++;
            displayQuestion(currentQuestionIndex);
        } else {
            nextButton.setEnabled(false);
            submitButton.setEnabled(true);
            JOptionPane.showMessageDialog(this, "You have reached the end of the quiz.");
        }
    }

    private void checkAnswer() {
        if (currentQuestionIndex < 0 || currentQuestionIndex >= questions.size())
            return;
        String sel = null;
        if (optA.isSelected())
            sel = "A";
        else if (optB.isSelected())
            sel = "B";
        else if (optC.isSelected())
            sel = "C";
        else if (optD.isSelected())
            sel = "D";
        if (sel != null && sel.equals(questions.get(currentQuestionIndex).getCorrectOption()))
            score++;
    }

    private void submitExam() {
        checkAnswer(); // Check final question
        try {
            boolean success = resultDAO.saveResult(new Result(0, student.getId(), exam.getId(), score,
                    questions.size(), new Timestamp(System.currentTimeMillis())));
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Success! Quiz results stored.\n\nYour Score: " + score + "/" + questions.size(),
                        "Exam Complete", JOptionPane.INFORMATION_MESSAGE);
            } else if (resultDAO.hasAttemptedExam(student.getId(), exam.getId())) {
                JOptionPane.showMessageDialog(this, "You have already submitted this quiz once.", "Already Submitted",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save results. Check your database.", "DB Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (java.sql.SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Critical error: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        dispose();
    }
}
