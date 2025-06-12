package com.example.scribble;

public class AppState {
    private static AppState instance = new AppState();
    private int currentBookId;

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
}