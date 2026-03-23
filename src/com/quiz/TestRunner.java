package com.quiz;

import com.quiz.util.DatabaseConnection;
import java.sql.Connection;

public class TestRunner {
    public static void main(String[] args) {
        try {
            System.out.println("Testing DB Connection...");
            Connection conn = DatabaseConnection.getConnection();
            System.out.println("Connection successful: " + (conn != null));
            DatabaseConnection.initializeSchema();
            System.out.println("Schema initialized.");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
