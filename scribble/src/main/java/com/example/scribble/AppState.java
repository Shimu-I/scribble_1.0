package com.example.scribble;

import java.util.logging.Logger;
import java.util.logging.Level;

public class AppState {
    private static final Logger LOGGER = Logger.getLogger(AppState.class.getName());
    private static AppState instance = new AppState();
    private int currentBookId = -1;
    private String previousFXML;

    private AppState() {}

    public static AppState getInstance() {
        return instance;
    }

    public void setCurrentBookId(int bookId) {
        this.currentBookId = bookId;
        LOGGER.info("Set currentBookId: " + bookId);
    }

    public int getCurrentBookId() {
        LOGGER.info("Retrieved currentBookId: " + currentBookId);
        return currentBookId;
    }

    public void clearCurrentBookId() {
        this.currentBookId = -1;
        LOGGER.info("Cleared currentBookId");
    }

    public void setPreviousFXML(String fxml) {
        this.previousFXML = fxml;
        LOGGER.info("Set previousFXML: " + fxml);
    }

    public String getPreviousFXML() {
        LOGGER.info("Retrieved previousFXML: " + previousFXML);
        return previousFXML;
    }
}