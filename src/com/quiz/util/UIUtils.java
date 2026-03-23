package com.quiz.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UIUtils {
    public static final Color COLOR_PRIMARY = new Color(33, 33, 33); // Very Dark Grey/Black
    public static final Color COLOR_ACCENT = new Color(0, 120, 215); // Windows Blue
    public static final Color COLOR_BACKGROUND = new Color(245, 245, 245); // Soft Grey
    public static final Color COLOR_TEXT = new Color(50, 50, 50);
    public static final Color COLOR_WHITE = Color.WHITE;
    public static final Color COLOR_DANGER = new Color(231, 76, 60);

    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);

    public static void styleButton(JButton button) {
        button.setBackground(COLOR_ACCENT);
        button.setForeground(COLOR_WHITE);
        button.setFocusPainted(false);
        button.setFont(FONT_BOLD);
        button.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void styleFrame(JFrame frame) {
        frame.getContentPane().setBackground(COLOR_WHITE);
    }

    public static JPanel createPaddingPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_WHITE);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));
        return panel;
    }

    public static void setRounding(JComponent component) {
        // Basic aesthetic improvement
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    }
}
