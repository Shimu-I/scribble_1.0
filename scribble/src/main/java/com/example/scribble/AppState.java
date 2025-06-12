package com.example.scribble;

public class AppState {
    private static AppState instance = new AppState();
    private int currentBookId;
    private String previousFXML;

    private AppState() {}

    public static AppState getInstance() {
        return instance;
    }

    public void setCurrentBookId(int bookId) {
        this.currentBookId = bookId;
    }

    public int getCurrentBookId() {
        return currentBookId;
    }

    public void setPreviousFXML(String fxml) {
        this.previousFXML = fxml;
    }

    public String getPreviousFXML() {
        return previousFXML;
    }
}