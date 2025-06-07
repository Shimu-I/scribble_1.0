package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class read_chapter__c {

    @FXML
    private Label bookNameLabel; // fx:id="bookNameLabel"
    @FXML
    private Label chapterNumberLabel; // fx:id="chapterNumberLabel"
    @FXML
    private TextArea chapterContentArea; // fx:id="chapterContentArea"
    @FXML
    private Button nextButton; // fx:id="nextButton"
    @FXML
    private Button back_button; // fx:id="back_button"

    @FXML
    private nav_bar__c mainController;

    private int bookId;
    private int chapterId;
    private int chapterNumber;
    private boolean readOnly = true;
    private static final Logger LOGGER = Logger.getLogger(read_chapter__c.class.getName());

    @FXML
    private void initialize() {
        if (chapterContentArea != null) {
            chapterContentArea.setEditable(!readOnly);
            chapterContentArea.setWrapText(true);
        }
    }

    public void setMainController(nav_bar__c mainController) {
        this.mainController = mainController;
    }

    public void setChapterData(int bookId, int chapterId, String title, String content) {
        this.bookId = bookId;
        this.chapterId = chapterId;
        fetchBookTitle();
        fetchChapterDetails();
    }

    private void fetchBookTitle() {
        String sql = "SELECT title FROM books WHERE book_id = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                bookNameLabel.setText("Book: " + rs.getString("title"));
            } else {
                bookNameLabel.setText("Book: Unknown");
                LOGGER.warning("No book found for book_id: " + bookId);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error fetching book title: " + e.getMessage());
            showAlert("Error", "Failed to load book title: " + e.getMessage());
        }
    }

    private void fetchChapterDetails() {
        String sql = "SELECT chapter_number, content FROM chapters WHERE chapter_id = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, chapterId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                chapterNumber = rs.getInt("chapter_number");
                chapterNumberLabel.setText("Chapter " + chapterNumber);
                chapterContentArea.setText(rs.getString("content"));
            } else {
                chapterNumberLabel.setText("Chapter Not Found");
                chapterContentArea.setText("Unable to load chapter content.");
                LOGGER.warning("No chapter found for chapter_id: " + chapterId);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error fetching chapter details: " + e.getMessage());
            showAlert("Error", "Failed to load chapter: " + e.getMessage());
        }
    }

    @FXML
    private void handleNextButton(ActionEvent event) {
        String sql = "SELECT chapter_id, chapter_number, content FROM chapters " +
                "WHERE book_id = ? AND chapter_number = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            pstmt.setInt(2, chapterNumber + 1);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                chapterId = rs.getInt("chapter_id");
                chapterNumber = rs.getInt("chapter_number");
                chapterNumberLabel.setText("Chapter " + chapterNumber);
                chapterContentArea.setText(rs.getString("content"));
            } else {
                showAlert("Info", "No more chapters available.");
            }
        } catch (SQLException e) {
            LOGGER.severe("Error loading next chapter: " + e.getMessage());
            showAlert("Error", "Failed to load next chapter: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackButton(ActionEvent event) {
        if (mainController != null) {
            mainController.loadFXML("read_book.fxml"); // Navigate back to Books
        }else {
            System.err.println("read_chapter is not having the nav bar.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setChapterId(int chapterId) {
        this.chapterId = chapterId;
        // Fetch the bookId for this chapter
        String sql = "SELECT book_id FROM chapters WHERE chapter_id = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, chapterId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                this.bookId = rs.getInt("book_id");
                fetchBookTitle();
                fetchChapterDetails();
            } else {
                LOGGER.warning("No book found for chapter_id: " + chapterId);
                showAlert("Error", "Chapter not found.");
            }
        } catch (SQLException e) {
            LOGGER.severe("Error fetching book_id for chapter: " + e.getMessage());
            showAlert("Error", "Failed to load chapter: " + e.getMessage());
        }
    }
}