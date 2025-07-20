package com.example.scribble;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class UserSession {
    private static UserSession instance;
    private int userId;
    private String username;
    private String userPhotoPath;
    private String role;
    private static final String SESSION_FILE = System.getProperty("user.dir") + "/session.properties";

    private UserSession() {
        // Private constructor to enforce singleton
    }

    private UserSession(int userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public static void initialize(int userId, String username, String role, String userPhotoPath) {
        instance = new UserSession(userId, username, role);
        instance.userPhotoPath = userPhotoPath;
        instance.saveToFile();
        System.out.println("UserSession initialized: userId=" + userId + ", username=" + username + ", role=" + role + ", photoPath=" + userPhotoPath);
    }

    public static int getCurrentUserId() {
        return getInstance().getUserId();
    }

    public void setUser(int userId, String username, String role, String userPhotoPath) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.userPhotoPath = userPhotoPath;
        saveToFile();
        System.out.println("UserSession set: userId=" + userId + ", username=" + username + ", role=" + role + ", photoPath=" + userPhotoPath);
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getUserPhotoPath() {
        return userPhotoPath;
    }

    public void clearSession() {
        userId = 0;
        username = null;
        role = null;
        userPhotoPath = null;
        saveToFile();
        System.out.println("UserSession cleared: loggedIn=" + isLoggedIn());
    }

    public boolean isLoggedIn() {
        return userId != 0;
    }

    public boolean isAuthor() {
        String sql = "SELECT role FROM book_authors WHERE user_id = ? LIMIT 1";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, this.userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String role = rs.getString("role");
                return role.equals("Owner") || role.equals("Co-Author");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void saveToFile() {
        Properties props = new Properties();
        props.setProperty("userId", String.valueOf(userId));
        props.setProperty("username", username != null ? username : "");
        props.setProperty("role", role != null ? role : "");
        props.setProperty("userPhotoPath", userPhotoPath != null ? userPhotoPath : "");
        try (FileOutputStream out = new FileOutputStream(SESSION_FILE)) {
            props.store(out, "User Session");
            System.out.println("Session saved to " + SESSION_FILE + ": userId=" + userId);
        } catch (IOException e) {
            System.err.println("Failed to save session to " + SESSION_FILE + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static UserSession loadFromFile() {
        UserSession session = getInstance();
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(SESSION_FILE)) {
            props.load(in);
            String userIdStr = props.getProperty("userId");
            String username = props.getProperty("username");
            String role = props.getProperty("role");
            String userPhotoPath = props.getProperty("userPhotoPath");
            System.out.println("Loaded session from " + SESSION_FILE + ": userId=" + userIdStr + ", username=" + username + ", role=" + role);
            if (userIdStr != null && !userIdStr.isEmpty()) {
                try {
                    session.setUser(Integer.parseInt(userIdStr), username, role, userPhotoPath);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid userId format in session file; clearing session");
                    session.clearSession();
                }
            } else {
                session.clearSession();
            }
        } catch (IOException e) {
            System.out.println("No session file found or error loading at " + SESSION_FILE + ": " + e.getMessage());
            session.clearSession();
        }
        return session;
    }

    public void updateSession(int userId, String username, String role, String userPhotoPath) {
        if (this.userId != userId || !nullSafeEquals(this.username, username) ||
                !nullSafeEquals(this.role, role) || !nullSafeEquals(this.userPhotoPath, userPhotoPath)) {
            setUser(userId, username, role, userPhotoPath);
        }
    }

    private boolean nullSafeEquals(String s1, String s2) {
        return (s1 == null && s2 == null) || (s1 != null && s1.equals(s2));
    }

    public void setUserPhotoPath(String profilePicPath) {
        this.userPhotoPath = profilePicPath;
        saveToFile();
        System.out.println("UserSession userPhotoPath updated: " + profilePicPath);
    }
}