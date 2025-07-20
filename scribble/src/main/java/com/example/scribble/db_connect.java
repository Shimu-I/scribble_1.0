package com.example.scribble;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class db_connect {
    private static final String url = "jdbc:mysql://127.0.0.1:3306/scribble_db_2";
    private static final String username = "root";
    private static final String password = "admin";

    public static void main(String[] args){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database connected successfully (scribble_db)!");  // Success message
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

}
