package com.example.scribble;

import java.util.logging.Logger;

public class AppState_c {
    private static final Logger LOGGER = Logger.getLogger(AppState_c.class.getName());
    private static volatile AppState_c instance;
    private String previousFXML;

    // Private constructor to prevent instantiation
    private AppState_c() {
        // Prevent instantiation through reflection
        if (instance != null) {
            throw new RuntimeException("Use getInstance() to get the singleton instance of AppState_c.");
        }
    }

    // Thread-safe singleton instantiation
    public static AppState_c getInstance() {
        if (instance == null) {
            synchronized (AppState_c.class) {
                if (instance == null) {
                    instance = new AppState_c();
                    LOGGER.info("AppState_c singleton instance created.");
                }
            }
        }
        return instance;
    }

    // Get the previous FXML file path
    public String getPreviousFXML() {
        LOGGER.fine("Retrieving previous FXML: " + previousFXML);
        return previousFXML;
    }

    // Set the previous FXML file path
    public void setPreviousFXML(String previousFXML) {
        this.previousFXML = previousFXML;
        LOGGER.info("Set previous FXML to: " + previousFXML);
    }
}