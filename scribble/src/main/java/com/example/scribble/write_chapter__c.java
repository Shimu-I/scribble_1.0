package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
import java.util.logging.Logger;

public class write_chapter__c {
    private static final Logger LOGGER = Logger.getLogger(write_chapter__c.class.getName());

    @FXML
    public Label chapter_no;

    @FXML
    private Label book_title;

    @FXML
    private Button save_button;

    @FXML
    private TextArea writing_space;

    @FXML
    private nav_bar__c mainController;

    private int bookId = -1;
    private String bookName;
    private int authorId = -1;
    private int chapterNumber = -1;

    @FXML
    public void initialize() {
        writing_space.setText("");
        save_button.setOnAction(event -> handleSave());
    }

    public void setBookDetails(int bookId, String bookName, int authorId) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.authorId = authorId;

        if (book_title != null) {
            book_title.setText(bookName);
        }

        this.chapterNumber = getNextChapterNumber(bookId);
        if (chapter_no != null) {
            chapter_no.setText("Chapter " + chapterNumber);
        }

        loadDraftFromDatabase();
    }

    private int getNextChapterNumber(int bookId) {
        String query = "SELECT IFNULL(MAX(chapter_number), 0) + 1 AS next_chapter FROM chapters WHERE book_id = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("next_chapter");
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Could not determine next chapter number: " + e.getMessage());
        }
        return 1;
    }

    private void loadDraftFromDatabase() {
        String query = "SELECT content FROM draft_chapters WHERE book_id = ? AND chapter_number = ? AND author_id = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, chapterNumber);
            stmt.setInt(3, authorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                writing_space.setText(rs.getString("content"));
                showAlert("Info", "Loaded draft from database.");
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load draft from database: " + e.getMessage());
        }
    }

    private void handleSave() {
        String content = writing_space.getText().trim();
        if (content.isEmpty()) {
            showAlert("Error", "Cannot save an empty chapter.");
            return;
        }

        if (!doesAuthorExist(authorId) || !isUserBookAuthor(bookId, authorId)) {
            showAlert("Error", "Invalid author or unauthorized.");
            return;
        }

        // Check if chapter already exists in chapters table
        if (doesChapterExist(bookId, chapterNumber)) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Overwrite");
            alert.setHeaderText(null);
            alert.setContentText("Chapter " + chapterNumber + " for this book already exists. Do you want to overwrite it?");
            ButtonType overwriteButton = new ButtonType("Overwrite", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(overwriteButton, cancelButton);

            if (alert.showAndWait().filter(response -> response == overwriteButton).isPresent()) {
                saveChapterToDatabase(bookId, chapterNumber, content);
                deleteDraftFromDatabase();
                promptForNextChapter();
            } else {
                showAlert("Info", "Chapter save cancelled.");
            }
        } else {
            saveChapterToDatabase(bookId, chapterNumber, content);
            deleteDraftFromDatabase();
            promptForNextChapter();
        }
    }

    private void promptForNextChapter() {
        showAlert("Success", "Chapter published successfully.");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Continue Writing");
        alert.setHeaderText(null);
        alert.setContentText("Would you like to write the next chapter?");
        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton);

        if (alert.showAndWait().filter(response -> response == yesButton).isPresent()) {
            // Increment chapter number and reset UI for next chapter
            chapterNumber = getNextChapterNumber(bookId);
            if (chapter_no != null) {
                chapter_no.setText("Chapter " + chapterNumber);
            }
            writing_space.setText("");
            loadDraftFromDatabase(); // Check for any existing draft for the new chapter
        } else {
            // Navigate back to main writing screen
            if (mainController != null) {
                mainController.loadFXML("reading_list.fxml");
            }
        }
    }

    private boolean doesChapterExist(int bookId, int chapterNumber) {
        String query = "SELECT COUNT(*) FROM chapters WHERE book_id = ? AND chapter_number = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, chapterNumber);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to check existing chapter: " + e.getMessage());
            return false;
        }
    }

    public void handle_save_draft(ActionEvent actionEvent) {
        String content = writing_space.getText();
        if (content.isEmpty()) {
            showAlert("Error", "Cannot save an empty draft.");
            return;
        }

        String query = """
            INSERT INTO draft_chapters (book_id, author_id, chapter_number, content)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE content = ?, updated_at = CURRENT_TIMESTAMP
            """;
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, authorId);
            stmt.setInt(3, chapterNumber);
            stmt.setString(4, content);
            stmt.setString(5, content);
            stmt.executeUpdate();
            showAlert("Success", "Draft saved to database.");
        } catch (SQLException e) {
            showAlert("Error", "Failed to save draft to database: " + e.getMessage());
        }
    }

    private void deleteDraftFromDatabase() {
        String query = "DELETE FROM draft_chapters WHERE book_id = ? AND chapter_number = ? AND author_id = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, chapterNumber);
            stmt.setInt(3, authorId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            showAlert("Warning", "Could not delete draft from database: " + e.getMessage());
        }
    }

    private void saveChapterToDatabase(int bookId, int chapterNumber, String content) {
        String query = """
        INSERT INTO chapters (book_id, author_id, chapter_number, content)
        VALUES (?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE content = ?, updated_at = CURRENT_TIMESTAMP
        """;
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, authorId);
            stmt.setInt(3, chapterNumber);
            stmt.setString(4, content);
            stmt.setString(5, content);
            stmt.executeUpdate();
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to save to database: " + e.getMessage());
        }
    }

    private boolean doesAuthorExist(int authorId) {
        String query = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, authorId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean isUserBookAuthor(int bookId, int userId) {
        String query = "SELECT COUNT(*) FROM book_authors WHERE book_id = ? AND user_id = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private void showAlert(String title, String message) {
        Alert.AlertType type = title.equalsIgnoreCase("error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION;
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setMainController(nav_bar__c mainController) {
        this.mainController = mainController;
    }

    public void setUserId(int userId) {
        this.authorId = userId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public void setBookName(String title) {
        this.bookName = title;
    }

    public void handle_back_button(ActionEvent actionEvent) {
        if (mainController != null) {
            mainController.loadFXML("write.fxml");
        }
    }

    public void setDraftId(int draftId) {
        String query = "SELECT book_id, chapter_number, author_id, content FROM draft_chapters WHERE draft_id = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, draftId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                this.bookId = rs.getInt("book_id");
                this.chapterNumber = rs.getInt("chapter_number");
                this.authorId = rs.getInt("author_id");
                String content = rs.getString("content");

                // Fetch book title
                String bookQuery = "SELECT title FROM books WHERE book_id = ?";
                try (PreparedStatement bookStmt = conn.prepareStatement(bookQuery)) {
                    bookStmt.setInt(1, bookId);
                    ResultSet bookRs = bookStmt.executeQuery();
                    if (bookRs.next()) {
                        this.bookName = bookRs.getString("title");
                        LOGGER.info("Loaded book title: " + (bookName != null ? bookName : "null") + " for book_id: " + bookId);
                        if (book_title != null) {
                            book_title.setText(bookName != null ? bookName : "");
                        }
                    } else {
                        showAlert("Error", "Book not found for book_id: " + bookId);
                    }
                }

                // Update UI
                if (chapter_no != null) {
                    chapter_no.setText("Chapter " + chapterNumber);
                }
                if (writing_space != null) {
                    writing_space.setText(content != null ? content : "");
                }
            } else {
                showAlert("Error", "Draft not found for draft_id: " + draftId);
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load draft: " + e.getMessage());
            LOGGER.severe("SQL Exception in setDraftId: " + e.getMessage());
        }
    }

    public void setChapterId(int chapterId) {
        String query = "SELECT book_id, chapter_number, author_id, content FROM chapters WHERE chapter_id = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, chapterId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                this.bookId = rs.getInt("book_id");
                this.chapterNumber = rs.getInt("chapter_number");
                this.authorId = rs.getInt("author_id");
                String content = rs.getString("content");

                // Fetch book title
                String bookQuery = "SELECT title FROM books WHERE book_id = ?";
                try (PreparedStatement bookStmt = conn.prepareStatement(bookQuery)) {
                    bookStmt.setInt(1, bookId);
                    ResultSet bookRs = bookStmt.executeQuery();
                    if (bookRs.next()) {
                        this.bookName = bookRs.getString("title");
                        if (book_title != null) {
                            book_title.setText(bookName);
                        }
                    } else {
                        showAlert("Error", "Book not found for book_id: " + bookId);
                    }
                }

                // Update UI
                if (chapter_no != null) {
                    chapter_no.setText("Chapter " + chapterNumber);
                }
                if (writing_space != null) {
                    writing_space.setText(content != null ? content : "");
                }
            } else {
                showAlert("Error", "Chapter not found for chapter_id: " + chapterId);
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load chapter: " + e.getMessage());
        }
    }

    public void setChapterNumber(int chapterNumber) {
        this.chapterNumber = chapterNumber;
        if (chapter_no != null) {
            chapter_no.setText("Chapter " + chapterNumber);
        }
    }
}