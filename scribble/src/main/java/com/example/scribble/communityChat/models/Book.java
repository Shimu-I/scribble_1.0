package com.example.scribble.communityChat.models;

public class Book {
    private int bookId;
    private String title;

    public Book(int bookId, String title) {
        this.bookId = bookId;
        this.title = title;
    }

    public int getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return title;  // important for ComboBox display
    }
}

