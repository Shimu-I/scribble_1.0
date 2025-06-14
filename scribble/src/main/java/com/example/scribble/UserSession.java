package com.example.scribble;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class UserSession {
    private static UserSession instance;
    private int userId;
    private String username;
    private String userPhotoPath;
    private static final String SESSION_FILE = System.getProperty("user.dir") + "/session.properties";

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
        saveToFile();
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
        saveToFile();
        System.out.println("UserSession cleared: loggedIn=" + isLoggedIn());
    }

    public boolean isLoggedIn() {
        return userId != 0;
    }

    public void saveToFile() {
        Properties props = new Properties();
        props.setProperty("userId", String.valueOf(userId));
        props.setProperty("username", username != null ? username : "");
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
            String userPhotoPath = props.getProperty("userPhotoPath");
            System.out.println("Loaded session from " + SESSION_FILE + ": userId=" + userIdStr + ", username=" + username);
            if (userIdStr != null && !userIdStr.isEmpty()) {
                try {
                    session.setUser(Integer.parseInt(userIdStr), username, userPhotoPath);
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

    public void updateSession(int userId, String username, String userPhotoPath) {
        if (this.userId != userId || this.username == null || !this.username.equals(username) || this.userPhotoPath == null || !this.userPhotoPath.equals(userPhotoPath)) {
            setUser(userId, username, userPhotoPath);
        }
    }
}