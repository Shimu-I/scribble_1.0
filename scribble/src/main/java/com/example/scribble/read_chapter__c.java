package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class read_chapter__c {

    private static final Logger LOGGER = Logger.getLogger(read_chapter__c.class.getName());

    @FXML private Label bookNameLabel; // fx:id="bookNameLabel"
    @FXML private Label chapterNumberLabel; // fx:id="chapterNumberLabel"
    @FXML private TextArea chapterContentArea; // fx:id="chapterContentArea"
    @FXML private Button nextButton; // fx:id="nextButton"
    @FXML private Button back_button; // fx:id="back_button"

    private nav_bar__c mainController;
    private int bookId;
    private int chapterId;
    private int chapterNumber;
    private final boolean readOnly = true;

    @FXML
    private void initialize() {
        checkUIElements();
        if (chapterContentArea != null) {
            chapterContentArea.setEditable(!readOnly);
            chapterContentArea.setWrapText(true);
            LOGGER.info("Initialized chapterContentArea as read-only with wrap text enabled");
        }
    }

    public void setMainController(nav_bar__c mainController) {
        this.mainController = mainController;
        LOGGER.info("Main controller set for read_chapter__c");
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
        LOGGER.info("setBookId called with bookId: " + bookId);
    }

    public void setChapterId(int chapterId) {
        this.chapterId = chapterId;
        loadChapterData();
    }

    private void loadChapterData() {
        if (chapterId <= 0) {
            LOGGER.warning("Invalid chapterId: " + chapterId);
            showAlert(Alert.AlertType.ERROR, "Invalid Chapter", "No valid chapter selected.");
            setDefaultUI();
            return;
        }

        String query = """
                SELECT c.book_id, c.chapter_number, c.content, b.title
                FROM chapters c
                JOIN books b ON c.book_id = b.book_id
                WHERE c.chapter_id = ?
                """;
        try (Connection conn = db_connect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, chapterId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                this.bookId = rs.getInt("book_id");
                this.chapterNumber = rs.getInt("chapter_number");
                String title = rs.getString("title");
                String content = rs.getString("content");

                if (bookNameLabel != null) {
                    bookNameLabel.setText( (title != null ? title : "Unknown"));
                }
                if (chapterNumberLabel != null) {
                    chapterNumberLabel.setText("Chapter " + chapterNumber);
                }
                if (chapterContentArea != null) {
                    chapterContentArea.setText(content != null ? content : "No content available.");
                }
                LOGGER.info("Loaded chapter data for chapterId: " + chapterId + ", bookId: " + bookId);
            } else {
                LOGGER.warning("No chapter found for chapterId: " + chapterId);
                showAlert(Alert.AlertType.ERROR, "Chapter Not Found", "The specified chapter does not exist.");
                setDefaultUI();
            }
        } catch (SQLException e) {
            LOGGER.severe("Error loading chapter data for chapterId: " + chapterId + ": " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load chapter: " + e.getMessage());
            setDefaultUI();
        }
    }

    @FXML
    private void handleNextButton(ActionEvent event) {
        if (bookId <= 0 || chapterNumber <= 0) {
            LOGGER.warning("Invalid bookId: " + bookId + " or chapterNumber: " + chapterNumber);
            showAlert(Alert.AlertType.ERROR, "Invalid State", "Cannot load next chapter.");
            return;
        }

        String query = """
            SELECT chapter_id, chapter_number, content
            FROM chapters
            WHERE book_id = ? AND chapter_number = ?
            """;
        try (Connection conn = db_connect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, bookId);
            pstmt.setInt(2, chapterNumber + 1);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                this.chapterId = rs.getInt("chapter_id");
                this.chapterNumber = rs.getInt("chapter_number");
                if (chapterNumberLabel != null) {
                    chapterNumberLabel.setText("Chapter " + chapterNumber);
                }
                if (chapterContentArea != null) {
                    chapterContentArea.setText(rs.getString("content") != null ? rs.getString("content") : "No content available.");
                }
                LOGGER.info("Loaded next chapter: chapterId=" + chapterId + ", chapterNumber=" + chapterNumber + ", bookId=" + bookId);
                recordChapterRead();
            } else {
                LOGGER.info("No more chapters available for bookId: " + bookId + ", chapterNumber: " + chapterNumber);
                showAlert(Alert.AlertType.INFORMATION, "End of Book", "You have completed all the chapters!");
                navigateToReadBook();
            }
        } catch (SQLException e) {
            LOGGER.severe("Error loading next chapter for bookId: " + bookId + ", chapterNumber: " + (chapterNumber + 1) + ": " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load next chapter: " + e.getMessage());
        }
    }


    @FXML
    private void handleBackButton(ActionEvent event) {
        if (mainController == null) {
            LOGGER.severe("Main controller is null, cannot navigate back from read_chapter__c");
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Navigation failed: main controller is not initialized.");
            return;
        }

        String previousFXML = AppState.getInstance().getPreviousFXML();
        if (previousFXML == null || previousFXML.isEmpty()) {
            LOGGER.warning("No previous FXML set in AppState, defaulting to read_book.fxml");
            previousFXML = "/com/example/scribble/read_book.fxml";
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(previousFXML));
            Parent page = loader.load();
            Object controller = loader.getController();

            if (controller instanceof read_book__c readBookController) {
                readBookController.setMainController(mainController);
                readBookController.setBookId(bookId);
                LOGGER.info("Navigating back to read_book.fxml with bookId: " + bookId);
            } else {
                LOGGER.warning("Unknown controller type for FXML: " + previousFXML);
            }

            AppState.getInstance().setPreviousFXML("/com/example/scribble/read_chapter.fxml");
            AppState.getInstance().setCurrentBookId(bookId);
            mainController.getCenterPane().getChildren().setAll(page);
            LOGGER.info("Navigated back to " + previousFXML + " with bookId: " + bookId);
        } catch (IOException e) {
            LOGGER.severe("Failed to navigate back to " + previousFXML + ": " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate back: " + e.getMessage());
        }
    }

    private void navigateToReadBook() {
        if (mainController == null) {
            LOGGER.severe("Main controller is null, cannot navigate to read_book.fxml");
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Navigation failed: main controller is not initialized.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/read_book.fxml"));
            Parent page = loader.load();
            read_book__c controller = loader.getController();
            controller.setMainController(mainController);
            controller.setBookId(bookId);
            AppState.getInstance().setPreviousFXML("/com/example/scribble/read_chapter.fxml");
            AppState.getInstance().setCurrentBookId(bookId);
            mainController.getCenterPane().getChildren().setAll(page);
            LOGGER.info("No more chapters, navigated to read_book.fxml with bookId: " + bookId);
        } catch (IOException e) {
            LOGGER.severe("Failed to navigate to read_book.fxml: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to book page: " + e.getMessage());
        }
    }

    private void recordChapterRead() {
        if (!UserSession.getInstance().isLoggedIn()) {
            LOGGER.info("User not logged in, skipping chapter read record for chapterId: " + chapterId);
            return;
        }

        try (Connection conn = db_connect.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO chapter_reads (user_id, chapter_id) VALUES (?, ?) " +
                            "ON DUPLICATE KEY UPDATE read_at = CURRENT_TIMESTAMP")) {
                stmt.setInt(1, UserSession.getInstance().getCurrentUserId());
                stmt.setInt(2, chapterId);
                stmt.executeUpdate();
                LOGGER.info("Recorded chapter read for chapterId: " + chapterId + ", userId: " + UserSession.getInstance().getCurrentUserId());
            }
            try (PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE books SET total_reads = total_reads + 1 WHERE book_id = ?")) {
                updateStmt.setInt(1, bookId);
                updateStmt.executeUpdate();
                LOGGER.info("Incremented total_reads for bookId: " + bookId);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error recording chapter read for chapterId: " + chapterId + ": " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to record chapter read.");
        }
    }

    private void checkUIElements() {
        if (bookNameLabel == null) LOGGER.severe("bookNameLabel is null");
        if (chapterNumberLabel == null) LOGGER.severe("chapterNumberLabel is null");
        if (chapterContentArea == null) LOGGER.severe("chapterContentArea is null");
        if (nextButton == null) LOGGER.severe("nextButton is null");
        if (back_button == null) LOGGER.severe("back_button is null");
        if (bookNameLabel == null || chapterNumberLabel == null || chapterContentArea == null ||
                nextButton == null || back_button == null) {
            showAlert(Alert.AlertType.ERROR, "UI Error", "Application error: UI components not initialized.");
        }
    }

    private void setDefaultUI() {
        if (bookNameLabel != null) bookNameLabel.setText("Book: Unknown");
        if (chapterNumberLabel != null) chapterNumberLabel.setText("Chapter Not Found");
        if (chapterContentArea != null) chapterContentArea.setText("Unable to load chapter content.");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}