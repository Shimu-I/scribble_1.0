package com.example.scribble;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Optional;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Optional;

import com.example.scribble.db_connect;
import com.example.scribble.nav_bar__c;
import com.example.scribble.read_book__c;
import com.example.scribble.write__c;
import com.example.scribble.AppState;
import com.example.scribble.UserSession;
import javafx.scene.control.ButtonBar;

public class write_chapter__c {
    private static final Logger LOGGER = Logger.getLogger(write_chapter__c.class.getName());

    @FXML private Label chapter_no;
    @FXML private Label book_title;
    @FXML private Button save_button;
    @FXML private TextArea writing_space;
    @FXML private nav_bar__c mainController;

    private int bookId = -1;
    private String bookName;
    private int authorId = -1;
    private int chapterNumber = -1;

    @FXML

    public void initialize() {
        if (writing_space == null || chapter_no == null || book_title == null || save_button == null) {
            LOGGER.severe("FXML elements not properly bound in write_chapter__c");
            showAlert("Error", "Application error: UI components not initialized.");
            return;
        }
        if (authorId <= 0) {
            authorId = UserSession.getInstance().getUserId(); // Set from session if not provided
            LOGGER.info("Initialized write_chapter__c with authorId from UserSession: " + authorId);
        }
        writing_space.setText("");
        save_button.setOnAction(event -> handleSave());

        // Check if mainController is set
        if (mainController == null) {
            LOGGER.warning("mainController is null in initialize method of write_chapter__c");
        }
    }

    public void setBookDetails(int bookId, String bookName, int authorId) {
        if (bookId <= 0) {
            LOGGER.severe("Invalid bookId provided: " + bookId);
            showAlert("Error", "Cannot load chapter: Invalid book ID.");
            return;
        }
        if (!doesBookExist(bookId)) {
            showAlert("Error", "Book does not exist for book ID: " + bookId);
            LOGGER.severe("Invalid bookId: " + bookId);
            return;
        }
        if (!doesAuthorExist(authorId)) {
            showAlert("Error", "Invalid author ID: " + authorId);
            LOGGER.severe("Invalid authorId: " + authorId);
            return;
        }
        if (!isUserBookAuthor(bookId, authorId)) {
            showAlert("Error", "User is not authorized to write for this book.");
            LOGGER.severe("User " + authorId + " not authorized for bookId: " + bookId);
            return;
        }

        this.bookId = bookId;
        this.bookName = bookName != null ? bookName : "Untitled Book";
        this.authorId = authorId;

        // Update AppState.currentBookId
        AppState.getInstance().setCurrentBookId(bookId);

        book_title.setText(this.bookName);
        this.chapterNumber = getNextChapterNumber(bookId);
        chapter_no.setText("Chapter " + chapterNumber);
        loadDraftFromDatabase();
        LOGGER.info("Set book details: bookId=" + bookId + ", bookName=" + this.bookName +
                ", authorId=" + authorId + ", chapterNumber=" + chapterNumber);
    }

    private int getNextChapterNumber(int bookId) {
        String query = """
            SELECT GREATEST(
                (SELECT IFNULL(MAX(chapter_number), 0) FROM chapters WHERE book_id = ?),
                (SELECT IFNULL(MAX(chapter_number), 0) FROM draft_chapters WHERE book_id = ?)
            ) + 1 AS next_chapter
        """;
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, bookId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int nextChapter = rs.getInt("next_chapter");
                LOGGER.info("Next chapter number for bookId " + bookId + ": " + nextChapter);
                return nextChapter;
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to determine next chapter number for bookId: " + bookId + ", error: " + e.getMessage());
            showAlert("Database Error", "Could not determine next chapter number.");
        }
        return 1;
    }

    private void loadDraftFromDatabase() {
        if (authorId == -1) {
            showAlert("Error", "No valid user logged in. Please log in to load drafts.");
            LOGGER.severe("Attempted to load draft with invalid authorId: " + authorId);
            return;
        }

        String query = "SELECT content FROM draft_chapters WHERE book_id = ? AND chapter_number = ? AND author_id = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, chapterNumber);
            stmt.setInt(3, authorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                writing_space.setText(rs.getString("content"));
                showAlert("Info", "Loaded draft for chapter " + chapterNumber + ".");
                LOGGER.info("Loaded draft for bookId: " + bookId + ", chapterNumber: " + chapterNumber +
                        ", authorId: " + authorId);
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to load draft for bookId: " + bookId + ", chapterNumber: " + chapterNumber +
                    ", authorId: " + authorId + ", error: " + e.getMessage());
            showAlert("Error", "Failed to load draft from database.");
        }
    }

    @FXML
    private void handleSave() {
        String content = writing_space.getText().trim();
        if (content.isEmpty()) {
            showAlert("Error", "Cannot save an empty chapter.");
            LOGGER.warning("Attempted to save empty chapter for bookId: " + bookId + ", chapterNumber: " + chapterNumber);
            return;
        }

        authorId = UserSession.getInstance().getUserId(); // Ensure authorId is current
        if (!UserSession.getInstance().isLoggedIn() || authorId <= 0) {
            LOGGER.severe("No valid user logged in, please log in to save chapter. UserId: " + authorId);
            showAlert("Login Required", "No valid user logged in. Please log in to save the chapter.");            return;
        }

        if (!doesAuthorExist(authorId) || !isUserBookAuthor(bookId, authorId)) {
            showAlert("Error", "Invalid author or unauthorized. Please log in.");
            LOGGER.severe("Invalid or unauthorized authorId: " + authorId + " for bookId: " + bookId);
            return;
        }

        if (doesDraftExistByOtherAuthor(bookId, chapterNumber, authorId)) {
            showAlert("Error", "Another author has a draft for chapter " + chapterNumber + ". Please choose a different chapter number or coordinate with them.");
            LOGGER.warning("Draft conflict for bookId: " + bookId + ", chapterNumber: " + chapterNumber + " by another author");
            return;
        }

        boolean chapterExists = doesChapterExist(bookId, chapterNumber);
        if (chapterExists) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Overwrite");
            alert.setHeaderText(null);
            alert.setContentText("Chapter " + chapterNumber + " already exists. Do you want to overwrite it?");
            ButtonType overwriteButton = new ButtonType("Overwrite", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(overwriteButton, cancelButton);

            if (!alert.showAndWait().filter(response -> response == overwriteButton).isPresent()) {
                showAlert("Info", "Chapter save cancelled.");
                return;
            }
        }

        try (Connection conn = db_connect.getConnection()) {
            conn.setAutoCommit(false);
            try {
                saveChapterToDatabase(conn, bookId, chapterNumber, content);
                deleteDraftFromDatabase(conn);
                conn.commit();
                LOGGER.info("Published chapter for bookId: " + bookId + ", chapterNumber: " + chapterNumber + ", authorId: " + authorId);
                promptForNextChapter();
            } catch (SQLException e) {
                conn.rollback();
                LOGGER.severe("Failed to save chapter for bookId: " + bookId + ", chapterNumber: " + chapterNumber + ", error: " + e.getMessage());
                showAlert("Database Error", "Failed to save chapter: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to establish database connection for saving chapter: " + e.getMessage());
            showAlert("Database Error", "Unable to connect to database: " + e.getMessage());
        }
    }


    private void promptForNextChapter() {
        int nextChapterNumber = getNextChapterNumber(bookId);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Continue Writing");
        alert.setHeaderText("Would you like to start writing the next chapter?");
        alert.setContentText("Next Chapter: " + nextChapterNumber);
        ButtonType continueButton = new ButtonType("Continue", ButtonBar.ButtonData.OK_DONE);
        ButtonType backButton = new ButtonType("Back to Book", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(continueButton, backButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == continueButton) {
                LOGGER.info("Prompted for next chapter: bookId=" + bookId + ", chapterNumber=" + nextChapterNumber);
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/write_chapter.fxml"));
                    Parent content = loader.load();
                    write_chapter__c controller = loader.getController();
                    controller.setMainController(mainController); // Ensure mainController is set
                    controller.setBookId(bookId);
                    controller.setChapterNumber(nextChapterNumber);
                    controller.setBookName(book_title != null ? book_title.getText() : "");
                    controller.setUserId(UserSession.getInstance().getUserId());
                    AppState.getInstance().setPreviousFXML("/com/example/scribble/read_book.fxml");
                    AppState.getInstance().setCurrentBookId(bookId);
                    if (mainController != null) {
                        mainController.getCenterPane().getChildren().setAll(content);
                        LOGGER.info("Navigated to next chapter: bookId=" + bookId + ", chapterNumber=" + nextChapterNumber);
                    } else {
                        LOGGER.severe("Main controller is null, cannot navigate to next chapter");
                        showAlert("Error", "Navigation failed: main controller not initialized.");
                    }
                } catch (IOException e) {
                    LOGGER.severe("Error navigating to next chapter: " + e.getMessage());
                    showAlert("Error", "Failed to open next chapter: " + e.getMessage());
                }
            } else {
                navigateBack();
            }
        });
    }

    private void navigateBack() {
        if (mainController == null) {
            LOGGER.severe("Main controller is null, cannot navigate back from write_chapter__c");
            showAlert("Navigation Error: Navigation failed: main controller is not initialized.", "");            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/read_book.fxml"));
            Parent content = loader.load();
            read_book__c controller = loader.getController();
            controller.setMainController(mainController);
            controller.setBookId(bookId);
            mainController.getCenterPane().getChildren().setAll(content);
            LOGGER.info("Navigated back to /com/example/scribble/read_book.fxml with bookId: " + bookId);
        } catch (IOException e) {
            LOGGER.severe("Error navigating back to read_book.fxml: " + e.getMessage());
            showAlert("Navigation Error: Failed to navigate back: " + e.getMessage(), "");
        }
    }


    private boolean doesChapterExist(int bookId, int chapterNumber) {
        String query = "SELECT COUNT(*) FROM chapters WHERE book_id = ? AND chapter_number = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, chapterNumber);
            ResultSet rs = stmt.executeQuery();
            boolean exists = rs.next() && rs.getInt(1) > 0;
            LOGGER.info("Chapter exists check for bookId: " + bookId + ", chapterNumber: " + chapterNumber + " -> " + exists);
            return exists;
        } catch (SQLException e) {
            LOGGER.severe("Failed to check existing chapter for bookId: " + bookId + ", chapterNumber: " +
                    chapterNumber + ", error: " + e.getMessage());
            showAlert("Database Error", "Failed to check existing chapter.");
            return false;
        }
    }


    @FXML
    public void handle_save_draft(ActionEvent actionEvent) {
        String content = writing_space.getText().trim();
        if (content.isEmpty()) {
            showAlert("Error", "Cannot save an empty draft.");
            LOGGER.warning("Attempted to save empty draft for bookId: " + bookId + ", chapterNumber: " + chapterNumber);
            return;
        }

        if (!doesAuthorExist(authorId) || !isUserBookAuthor(bookId, authorId)) {
            showAlert("Error", "Invalid author or unauthorized. Please log in.");
            LOGGER.severe("Invalid or unauthorized authorId: " + authorId + " for bookId: " + bookId);
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
            LOGGER.info("Saved draft for bookId: " + bookId + ", chapterNumber: " + chapterNumber +
                    ", authorId: " + authorId);
            promptForNextDraft();
        } catch (SQLException e) {
            LOGGER.severe("Failed to save draft for bookId: " + bookId + ", chapterNumber: " + chapterNumber +
                    ", authorId: " + authorId + ", error: " + e.getMessage());
            showAlert("Error", "Failed to save draft to database: " + e.getMessage());
        }
    }

    private void promptForNextDraft() {
        showAlert("Success", "Draft saved successfully.");

        // Check if this is the first draft for the book
        String query = "SELECT COUNT(*) FROM draft_chapters WHERE book_id = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 1) {
                // First draft, navigate back to read_book.fxml
                if (mainController == null) {
                    LOGGER.severe("Main controller is null, cannot navigate to read_book.fxml");
                    showAlert("Error", "Navigation failed: main controller not initialized.");
                    return; // Prevent navigation if mainController is null
                }
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/read_book.fxml"));
                    Parent page = loader.load();
                    read_book__c controller = loader.getController();
                    controller.setMainController(mainController);
                    controller.setBookId(bookId);
                    mainController.getCenterPane().getChildren().setAll(page);
                    LOGGER.info("Navigated back to read_book.fxml for bookId: " + bookId + " after first draft");
                } catch (IOException e) {
                    LOGGER.severe("Failed to navigate to read_book.fxml: " + e.getMessage());
                    showAlert("Error", "Navigation failed: unable to return to reading page.");
                }
                return;
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to check draft count for bookId: " + bookId + ", error: " + e.getMessage());
            showAlert("Database Error", "Failed to verify draft status.");
        }

        // Not the first draft, prompt for next draft
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Continue Writing");
        alert.setHeaderText(null);
        alert.setContentText("Would you like to write the next draft chapter?");
        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton);

        if (alert.showAndWait().filter(response -> response == yesButton).isPresent()) {
            chapterNumber = getNextChapterNumber(bookId);
            chapter_no.setText("Chapter " + chapterNumber);
            writing_space.setText("");
            loadDraftFromDatabase();
            LOGGER.info("Prompted for next draft: bookId=" + bookId + ", chapterNumber=" + chapterNumber);
        } else {
            if (mainController == null) {
                LOGGER.severe("Main controller is null, cannot navigate to read_book.fxml");
                showAlert("Error", "Navigation failed: main controller not initialized.");
                return;
            }
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/read_book.fxml"));
                Parent page = loader.load();
                read_book__c controller = loader.getController();
                controller.setMainController(mainController);
                controller.setBookId(bookId);
                mainController.getCenterPane().getChildren().setAll(page);
                LOGGER.info("Navigated back to read_book.fxml for bookId: " + bookId);
            } catch (IOException e) {
                LOGGER.severe("Failed to navigate to read_book.fxml: " + e.getMessage());
                showAlert("Error", "Navigation failed: unable to return to reading page.");
            }
        }
    }


    private void deleteDraftFromDatabase(Connection conn) throws SQLException {
        String query = "DELETE FROM draft_chapters WHERE book_id = ? AND chapter_number = ? AND author_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, chapterNumber);
            stmt.setInt(3, authorId);
            int rowsAffected = stmt.executeUpdate();
            LOGGER.info("Deleted draft for bookId: " + bookId + ", chapterNumber: " + chapterNumber +
                    ", authorId: " + authorId + ", rowsAffected: " + rowsAffected);
        }
    }

    private void saveChapterToDatabase(Connection conn, int bookId, int chapterNumber, String content) throws SQLException {
        String query = """
            INSERT INTO chapters (book_id, author_id, chapter_number, content)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE content = ?, updated_at = CURRENT_TIMESTAMP
        """;
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, authorId);
            stmt.setInt(3, chapterNumber);
            stmt.setString(4, content);
            stmt.setString(5, content);
            stmt.executeUpdate();
            LOGGER.info("Saved chapter to database: bookId: " + bookId + ", chapterNumber: " + chapterNumber +
                    ", authorId: " + authorId);
        }
    }

    private boolean doesAuthorExist(int authorId) {
        if (authorId == -1) {
            LOGGER.severe("Invalid authorId: " + authorId);
            return false;
        }
        String query = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, authorId);
            ResultSet rs = stmt.executeQuery();
            boolean exists = rs.next() && rs.getInt(1) > 0;
            LOGGER.info("Author exists check for authorId: " + authorId + " -> " + exists);
            return exists;
        } catch (SQLException e) {
            LOGGER.severe("Failed to check author existence for authorId: " + authorId + ", error: " + e.getMessage());
            showAlert("Database Error", "Failed to verify author.");
            return false;
        }
    }

    private boolean isUserBookAuthor(int bookId, int userId) {
        if (userId == -1) {
            LOGGER.severe("Invalid userId: " + userId);
            return false;
        }
        String query = "SELECT COUNT(*) FROM book_authors WHERE book_id = ? AND user_id = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            boolean isAuthor = rs.next() && rs.getInt(1) > 0;
            LOGGER.info("User is author check for bookId: " + bookId + ", userId: " + userId + " -> " + isAuthor);
            return isAuthor;
        } catch (SQLException e) {
            LOGGER.severe("Failed to check author authorization for bookId: " + bookId + ", userId: " + userId +
                    ", error: " + e.getMessage());
            showAlert("Database Error", "Failed to verify authorization.");
            return false;
        }
    }

    private boolean doesDraftExistByOtherAuthor(int bookId, int chapterNumber, int authorId) {
        if (authorId == -1) {
            LOGGER.severe("Invalid authorId: " + authorId);
            return false;
        }
        String query = "SELECT COUNT(*) FROM draft_chapters WHERE book_id = ? AND chapter_number = ? AND author_id != ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, chapterNumber);
            stmt.setInt(3, authorId);
            ResultSet rs = stmt.executeQuery();
            boolean exists = rs.next() && rs.getInt(1) > 0;
            LOGGER.info("Draft by other author exists check for bookId: " + bookId + ", chapterNumber: " + chapterNumber +
                    ", authorId: " + authorId + " -> " + exists);
            return exists;
        } catch (SQLException e) {
            LOGGER.severe("Failed to check draft by other author for bookId: " + bookId + ", chapterNumber: " +
                    chapterNumber + ", authorId: " + authorId + ", error: " + e.getMessage());
            showAlert("Database Error", "Failed to check for existing drafts.");
            return false;
        }
    }

    private boolean doesBookExist(int bookId) {
        if (bookId == -1) {
            LOGGER.severe("Invalid bookId: " + bookId);
            return false;
        }
        String query = "SELECT COUNT(*) FROM books WHERE book_id = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            boolean exists = rs.next() && rs.getInt(1) > 0;
            LOGGER.info("Book exists check for bookId: " + bookId + " -> " + exists);
            return exists;
        } catch (SQLException e) {
            LOGGER.severe("Failed to check book existence for bookId: " + bookId + ", error: " + e.getMessage());
            showAlert("Database Error", "Failed to verify book.");
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
        LOGGER.info("Set mainController in write_chapter__c");
    }

    public void setUserId(int userId) {
        this.authorId = userId;
        if (userId != -1) {
            LOGGER.info("Set authorId: " + userId);
        } else {
            LOGGER.warning("Set invalid authorId: " + userId);
        }
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
        if (bookId != -1) {
            LOGGER.info("Set bookId: " + bookId);
        } else {
            LOGGER.warning("Set invalid bookId: " + bookId);
        }
    }

    public void setBookName(String title) {
        this.bookName = title != null ? title : "Untitled Book";
        if (book_title != null) {
            book_title.setText(this.bookName);
        }
        LOGGER.info("Set bookName: " + this.bookName);
    }


    @FXML
    public void handle_back_button(ActionEvent actionEvent) {
        if (mainController == null) {
            LOGGER.severe("Main controller is null, cannot navigate back from write_chapter__c");
            showAlert("Error", "Navigation failed: main controller is not initialized.");
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

            // Set mainController and bookId based on the target controller type
            if (controller instanceof read_book__c readBookController) {
                readBookController.setMainController(mainController);
                readBookController.setBookId(bookId);
                LOGGER.info("Navigating back to read_book.fxml with bookId: " + bookId);
            } else if (controller instanceof write__c writeController) {
                writeController.setMainController(mainController);
                writeController.setBookId(bookId);
                LOGGER.info("Navigating back to write.fxml with bookId: " + bookId);
            } else {
                LOGGER.warning("Unknown controller type for FXML: " + previousFXML);
            }

            // Update AppState
            AppState.getInstance().setPreviousFXML("/com/example/scribble/write_chapter.fxml");
            AppState.getInstance().setCurrentBookId(bookId);

            mainController.getCenterPane().getChildren().setAll(page);
            LOGGER.info("Navigated back to " + previousFXML + " with bookId: " + bookId);
        } catch (IOException e) {
            LOGGER.severe("Failed to navigate back to " + previousFXML + ": " + e.getMessage());
            showAlert("Error", "Failed to navigate back: " + e.getMessage());
        }
    }

    private boolean doesChapterOrDraftExist(int bookId, int chapterNumber) {
        if (authorId == -1) {
            LOGGER.severe("Invalid authorId: " + authorId);
            return false;
        }
        String query = """
            SELECT (SELECT COUNT(*) FROM chapters WHERE book_id = ? AND chapter_number = ?) +
                   (SELECT COUNT(*) FROM draft_chapters WHERE book_id = ? AND chapter_number = ? AND author_id = ?) AS count
        """;
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, chapterNumber);
            stmt.setInt(3, bookId);
            stmt.setInt(4, chapterNumber);
            stmt.setInt(5, authorId);
            ResultSet rs = stmt.executeQuery();
            boolean exists = rs.next() && rs.getInt("count") > 0;
            LOGGER.info("Chapter or draft exists check for bookId: " + bookId + ", chapterNumber: " + chapterNumber +
                    ", authorId: " + authorId + " -> " + exists);
            return exists;
        } catch (SQLException e) {
            LOGGER.severe("Failed to check chapter/draft existence for bookId: " + bookId + ", chapterNumber: " +
                    chapterNumber + ", authorId: " + authorId + ", error: " + e.getMessage());
            showAlert("Database Error", "Failed to check chapter/draft existence.");
            return false;
        }
    }

    private void loadChapterOrDraftContent() {
        if (authorId == -1) {
            showAlert("Error", "No valid user logged in. Please log in to load content.");
            LOGGER.severe("Attempted to load content with invalid authorId: " + authorId);
            return;
        }

        String chapterQuery = "SELECT content FROM chapters WHERE book_id = ? AND chapter_number = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(chapterQuery)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, chapterNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                writing_space.setText(rs.getString("content"));
                showAlert("Info", "Loaded chapter " + chapterNumber + " from published chapters.");
                LOGGER.info("Loaded chapter content for bookId: " + bookId + ", chapterNumber: " + chapterNumber);
                return;
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to load chapter content for bookId: " + bookId + ", chapterNumber: " +
                    chapterNumber + ", error: " + e.getMessage());
        }

        String draftQuery = "SELECT content FROM draft_chapters WHERE book_id = ? AND chapter_number = ? AND author_id = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(draftQuery)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, chapterNumber);
            stmt.setInt(3, authorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                writing_space.setText(rs.getString("content"));
                showAlert("Info", "Loaded draft for chapter " + chapterNumber + ".");
                LOGGER.info("Loaded draft content for bookId: " + bookId + ", chapterNumber: " + chapterNumber +
                        ", authorId: " + authorId);
                return;
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to load draft content for bookId: " + bookId + ", chapterNumber: " + chapterNumber +
                    ", authorId: " + authorId + ", error: " + e.getMessage());
        }

        writing_space.setText("");
        showAlert("Info", "No content found for chapter " + chapterNumber + ". Start writing!");
        LOGGER.info("No content found for bookId: " + bookId + ", chapterNumber: " + chapterNumber + ", authorId: " + authorId);
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

                if (!doesBookExist(bookId)) {
                    showAlert("Error", "Book not found for book_id: " + bookId);
                    LOGGER.severe("Book not found for bookId: " + bookId);
                    return;
                }
                if (!isUserBookAuthor(bookId, authorId)) {
                    showAlert("Error", "User not authorized to edit this draft.");
                    LOGGER.severe("User " + authorId + " not authorized for bookId: " + bookId);
                    return;
                }

                String bookQuery = "SELECT title FROM books WHERE book_id = ?";
                try (PreparedStatement bookStmt = conn.prepareStatement(bookQuery)) {
                    bookStmt.setInt(1, bookId);
                    ResultSet bookRs = bookStmt.executeQuery();
                    if (bookRs.next()) {
                        this.bookName = bookRs.getString("title");
                        book_title.setText(this.bookName != null ? this.bookName : "Untitled Book");
                        LOGGER.info("Loaded book title: " + this.bookName + " for bookId: " + bookId);
                    }
                }

                chapter_no.setText("Chapter " + chapterNumber);
                writing_space.setText(content != null ? content : "");
                LOGGER.info("Loaded draft for draftId: " + draftId + ", bookId: " + bookId +
                        ", chapterNumber: " + chapterNumber + ", authorId: " + authorId);
            } else {
                showAlert("Error", "Draft not found for draft_id: " + draftId);
                LOGGER.severe("Draft not found for draftId: " + draftId);
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to load draft for draftId: " + draftId + ", error: " + e.getMessage());
            showAlert("Error", "Failed to load draft.");
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

                if (!doesBookExist(bookId)) {
                    showAlert("Error", "Book not found for book_id: " + bookId);
                    LOGGER.severe("Book not found for bookId: " + bookId);
                    return;
                }
                if (!isUserBookAuthor(bookId, authorId)) {
                    showAlert("Error", "User not authorized to edit this chapter.");
                    LOGGER.severe("User " + authorId + " not authorized for bookId: " + bookId);
                    return;
                }

                String bookQuery = "SELECT title FROM books WHERE book_id = ?";
                try (PreparedStatement bookStmt = conn.prepareStatement(bookQuery)) {
                    bookStmt.setInt(1, bookId);
                    ResultSet bookRs = bookStmt.executeQuery();
                    if (bookRs.next()) {
                        this.bookName = bookRs.getString("title");
                        book_title.setText(this.bookName != null ? this.bookName : "Untitled Book");
                    }
                }

                chapter_no.setText("Chapter " + chapterNumber);
                writing_space.setText(content != null ? content : "");
                LOGGER.info("Loaded chapter for chapterId: " + chapterId + ", bookId: " + bookId +
                        ", chapterNumber: " + chapterNumber + ", authorId: " + authorId);
            } else {
                showAlert("Error", "Chapter not found for chapter_id: " + chapterId);
                LOGGER.severe("Chapter not found for chapterId: " + chapterId);
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to load chapter for chapterId: " + chapterId + ", error: " + e.getMessage());
            showAlert("Error", "Failed to load chapter.");
        }
    }

    public void setChapterNumber(int chapterNumber) {
        this.chapterNumber = chapterNumber;
        chapter_no.setText("Chapter " + chapterNumber);
        loadDraftFromDatabase();
        LOGGER.info("Set chapter number: bookId: " + bookId + ", chapterNumber: " + chapterNumber);
    }
}