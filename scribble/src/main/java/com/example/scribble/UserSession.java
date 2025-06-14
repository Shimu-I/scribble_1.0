package com.example.scribble;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class UserSession {
    private static UserSession instance;
    private int userId; // Explicitly int
    private String username; // Optional, for convenience
    private String userPhotoPath; // Path to user's profile photo

    private UserSession() {
        // Private constructor to enforce singleton
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public static int getCurrentUserId() {
        return getInstance().getUserId();
    }

    public void setUser(int userId, String username, String userPhotoPath) {
        this.userId = userId;
        this.username = username;
        this.userPhotoPath = userPhotoPath;
        saveToFile(); // Persist immediately after setting
        System.out.println("UserSession set: userId=" + userId + ", username=" + username + ", photoPath=" + userPhotoPath);
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getUserPhotoPath() {
        return userPhotoPath;
    }

    public void clearSession() {
        userId = 0;
        username = null;
        userPhotoPath = null;
        saveToFile(); // Persist the cleared state
        System.out.println("UserSession cleared.");
    }

    public boolean isLoggedIn() {
        return userId != 0;
    }

    // Optional: Persist session to file
    private static final String SESSION_FILE = "session.properties";

    public void saveToFile() {
        Properties props = new Properties();
        props.setProperty("userId", String.valueOf(userId));
        props.setProperty("username", username != null ? username : "");
        props.setProperty("userPhotoPath", userPhotoPath != null ? userPhotoPath : "");
        try (FileOutputStream out = new FileOutputStream(SESSION_FILE)) {
            props.store(out, "User Session");
        } catch (IOException e) {
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
            String userPhotoPath = props.getProperty("userPhotoPath");
            if (userIdStr != null && !userIdStr.isEmpty()) {
                try {
                    session.setUser(Integer.parseInt(userIdStr), username, userPhotoPath);
                } catch (NumberFormatException e) {
                    // Invalid userId format; clear session
                    session.clearSession();
                }
            }
        } catch (IOException e) {
            // File doesn't exist or is invalid; start fresh
        }
        return session;
    }

    static {
        // Load session from file on class initialization
        loadFromFile();
    }

    public void updateSession(int userId, String username, String userPhotoPath) {
        if (this.userId != userId || this.username == null || !this.username.equals(username) || this.userPhotoPath == null || !this.userPhotoPath.equals(userPhotoPath)) {
            setUser(userId, username, userPhotoPath);
        }
    }
}