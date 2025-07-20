package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Insets;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class read_book__c {

    private static final Logger LOGGER = Logger.getLogger(read_book__c.class.getName());
    private Connection conn;
    private int bookId;
    private boolean isAuthorOrCoAuthor; // Updated dynamically
    private int authorId;

    @FXML private Label bookTitleLabel, views, chapters, reads, avg_ratting, status, genre, book_description, user_name, user_comment, total_comments, co_author_names;
    @FXML private ComboBox<Integer> ratingComboBox;
    @FXML private Button read_now_button, support_author_button, request_collab_button, saved_books_button, author_profile, back_button, add_chapter, add_comment, add_draft;
    @FXML private VBox chapterContainer, commentContainer, draftContainer, authorContainer;
    @FXML private HBox authorContainerHBox;
    @FXML private ImageView coverImage;
    @FXML private TextField comment_box;
    @FXML private ComboBox<String> statusComboBox;

    private nav_bar__c mainController;

    public void initialize() {
        connectToDatabase();
        LOGGER.info("Initializing read_book__c controller");

        if (ratingComboBox != null) {
            ratingComboBox.getItems().addAll(0, 1, 2, 3, 4, 5);
            ratingComboBox.setValue(0);
            ratingComboBox.setOnAction(this::handleRating);
        } else {
            LOGGER.severe("ratingComboBox is null");
        }

        if (statusComboBox != null) {
            statusComboBox.getItems().addAll("Ongoing", "Complete", "Hiatus");
            statusComboBox.setOnAction(this::handleStatusUpdate);
            updateStatusComboBoxVisibility();
        } else {
            LOGGER.severe("statusComboBox is null");
        }

        checkNullElements();
        if (bookId > 0) {
            fetchBookDetails();
            loadChapters();
            loadComments();
            loadDrafts();
            updateViewCount();
        } else {
            LOGGER.warning("No valid bookId set, using default behavior.");
            setDefaultLabels();
        }

        checkNullElements();
        updateAddChapterButtonVisibility();
        updateDraftContainerVisibility();
    }

    private void checkNullElements() {
        if (bookTitleLabel == null) LOGGER.severe("bookTitleLabel is null");
        if (views == null) LOGGER.severe("views Label is null");
        if (chapters == null) LOGGER.severe("chapters Label is null");
        if (reads == null) LOGGER.severe("reads Label is null");
        if (avg_ratting == null) LOGGER.severe("avg_ratting Label is null");
        if (status == null) LOGGER.severe("status Label is null");
        if (genre == null) LOGGER.severe("genre Label is null");
        if (book_description == null) LOGGER.severe("book_description Label is null");
        if (user_name == null) LOGGER.severe("user_name Label is null");
        if (user_comment == null) LOGGER.severe("user_comment Label is null");
        if (total_comments == null) LOGGER.severe("total_comments Label is null");
        if (ratingComboBox == null) LOGGER.severe("ratingComboBox is null");
        if (read_now_button == null) LOGGER.severe("read_now_button Button is null");
        if (support_author_button == null) LOGGER.severe("support_author_button Button is null");
        if (request_collab_button == null) LOGGER.severe("request_collab_button Button is null");
        if (saved_books_button == null) LOGGER.severe("saved_books_button Button is null");
        if (author_profile == null) LOGGER.severe("author_profile Button is null");
        if (back_button == null) LOGGER.severe("back_button Button is null");
        if (add_chapter == null) LOGGER.severe("add_chapter Button is null");
        if (add_comment == null) LOGGER.severe("add_comment Button is null");
        if (add_draft == null) LOGGER.severe("add_draft Button is null");
        if (chapterContainer == null) LOGGER.severe("chapterContainer VBox is null");
        if (commentContainer == null) LOGGER.severe("commentContainer VBox is null");
        if (draftContainer == null) LOGGER.severe("draftContainer VBox is null");
        if (authorContainer == null) LOGGER.severe("authorContainer HBox is null");
        if (coverImage == null) LOGGER.severe("coverImage ImageView is null");
        if (comment_box == null) LOGGER.severe("comment_box TextField is null");
    }

    private void connectToDatabase() {
        try {
            conn = db_connect.getConnection();
            if (conn != null && !conn.isClosed()) {
                LOGGER.info("Database connected successfully.");
            } else {
                LOGGER.severe("Failed to establish database connection.");
                conn = null;
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to the database.");
            }
        } catch (SQLException e) {
            LOGGER.severe("DB Connection Error: " + e.getMessage());
            conn = null;
            showAlert(Alert.AlertType.ERROR, "Database Error", "Database connection failed: " + e.getMessage());
        }
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
        LOGGER.info("setBookId called with bookId: " + bookId);
        if (UserSession.getInstance().isLoggedIn()) {
            this.authorId = UserSession.getInstance().getUserId();
            this.isAuthorOrCoAuthor = isAuthorOrCoAuthor(bookId, this.authorId); // Update based on current user
        } else {
            this.isAuthorOrCoAuthor = false; // Default to false if not logged in
        }
        if (bookId <= 0) {
            LOGGER.warning("Invalid bookId: " + bookId + ". Using default behavior.");
            setDefaultLabels();
            return;
        }
        fetchBookDetails();
        loadChapters();
        loadComments();
        loadDrafts();
        updateViewCount(); // Add this to record the book visit
        updateAddChapterButtonVisibility();
        updateDraftContainerVisibility();
        updateStatusComboBoxVisibility(); // Ensure statusComboBox visibility is updated after bookId is set
    }

    // Replace the existing isAuthor method with this for improved debugging
    private boolean isAuthor(int bookId, int userId) {
        if (conn == null) {
            LOGGER.severe("No database connection for owner check for bookId: " + bookId + ", userId: " + userId);
            showAlert(Alert.AlertType.ERROR, "Database Error", "No database connection available.");
            return false;
        }
        String query = "SELECT COUNT(*) FROM book_authors WHERE book_id = ? AND user_id = ? AND role = 'Owner'";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                boolean isOwner = count > 0;
                LOGGER.info("Owner check for bookId: " + bookId + ", userId: " + userId + ", role='Owner' -> count: " + count + ", isOwner: " + isOwner);
                return isOwner;
            } else {
                LOGGER.warning("No result returned from owner check for bookId: " + bookId + ", userId: " + userId);
                return false;
            }
        } catch (SQLException e) {
            LOGGER.severe("Error verifying owner for bookId: " + bookId + ", userId: " + userId + ": " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to verify ownership: " + e.getMessage());
            return false;
        }
    }
    public void setMainController(nav_bar__c mainController) {
        this.mainController = mainController;
        LOGGER.info("Main controller set for navigation.");
    }

    private void fetchBookDetails() {
        if (conn == null) {
            LOGGER.severe("No database connection.");
            setDefaultLabels();
            return;
        }

        String query = """
            SELECT b.book_id, b.title, b.genre, b.status, b.view_count, b.total_reads, b.description, b.cover_photo,
                   COALESCE((SELECT AVG(rating * 1.0) FROM ratings WHERE book_id = b.book_id AND rating IS NOT NULL), 0.0) AS avg_rating,
                   (SELECT COUNT(*) FROM ratings WHERE book_id = b.book_id AND rating IS NOT NULL) AS rater_count
            FROM books b
            WHERE b.book_id = ?
            """;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, bookId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    if (bookTitleLabel != null) bookTitleLabel.setText(rs.getString("title") != null ? rs.getString("title") : "Unknown");
                    if (views != null) views.setText(rs.getInt("view_count") + "");
                    if (reads != null) reads.setText(rs.getInt("total_reads") + "");
                    if (status != null) status.setText(rs.getString("status") != null ? rs.getString("status") : "N/A");
                    if (genre != null) genre.setText(rs.getString("genre") != null ? rs.getString("genre") : "N/A");
                    if (book_description != null) book_description.setText(rs.getString("description") != null ? rs.getString("description") : "No description");
                    // Inside fetchBookDetails method, replace the coverImage block with:
                    if (coverImage != null) {
                        String coverPhoto = rs.getString("cover_photo");
                        if (coverPhoto != null && !coverPhoto.isEmpty()) {
                            File uploadFile = new File("Uploads/book_covers/" + coverPhoto);
                            if (uploadFile.exists()) {
                                try {
                                    Image image = new Image("file:" + uploadFile.getAbsolutePath());
                                    if (!image.isError()) {
                                        coverImage.setImage(image);
                                        LOGGER.info("Loaded book cover from filesystem: file:" + uploadFile.getAbsolutePath());
                                    } else {
                                        LOGGER.warning("Failed to load book cover from filesystem (image error): " + coverPhoto);
                                        coverImage.setImage(new Image(getClass().getResourceAsStream("/images/book_covers/demo_cover.png")));
                                    }
                                } catch (Exception e) {
                                    LOGGER.severe("Error loading cover photo from filesystem: " + coverPhoto + " - " + e.getMessage());
                                    coverImage.setImage(new Image(getClass().getResourceAsStream("/images/book_covers/demo_cover.png")));
                                }
                            } else {
                                try {
                                    Image image = new Image(getClass().getResourceAsStream("/images/book_covers/" + coverPhoto));
                                    if (!image.isError()) {
                                        coverImage.setImage(image);
                                        LOGGER.info("Loaded book cover from classpath: /images/book_covers/" + coverPhoto);
                                    } else {
                                        LOGGER.warning("Failed to load book cover from classpath (image error): " + coverPhoto);
                                        coverImage.setImage(new Image(getClass().getResourceAsStream("/images/book_covers/demo_cover.png")));
                                    }
                                } catch (Exception e) {
                                    LOGGER.severe("Error loading cover photo from classpath: " + coverPhoto + " - " + e.getMessage());
                                    coverImage.setImage(new Image(getClass().getResourceAsStream("/images/book_covers/demo_cover.png")));
                                }
                            }
                        } else {
                            coverImage.setImage(new Image(getClass().getResourceAsStream("/images/book_covers/demo_cover.png")));
                            LOGGER.info("Loaded default book cover: /images/book_covers/demo_cover.png");
                        }
                    }
                    double avgRating = rs.getDouble("avg_rating");
                    int raterCount = rs.getInt("rater_count");
                    if (avg_ratting != null) {
                        avg_ratting.setText(String.format("%.1f / %d", avgRating, raterCount));
                        LOGGER.info("Rating for bookId " + bookId + ": " + String.format("%.1f / %d", avgRating, raterCount));
                    }
                } else {
                    LOGGER.warning("No book found for bookId: " + bookId);
                    setDefaultLabels();
                }
            }
            loadAuthors();
        } catch (SQLException e) {
            LOGGER.severe("Error fetching book details: " + e.getMessage());
            setDefaultLabels();
        }
    }

    private void setDefaultLabels() {
        if (bookTitleLabel != null) bookTitleLabel.setText("Unknown");
        if (views != null) views.setText("0");
        if (reads != null) reads.setText("0");
        if (avg_ratting != null) avg_ratting.setText("0 / 0.0");
        if (status != null) status.setText("N/A");
        if (genre != null) genre.setText("N/A");
        if (book_description != null) book_description.setText("No description");
        if (user_name != null) user_name.setText("Unknown");
        if (user_comment != null) user_comment.setText("No comment");
        if (total_comments != null) total_comments.setText("Comments (0)");
        if (coverImage != null) coverImage.setImage(null);
    }


    private void loadComments() {
        if (conn == null || commentContainer == null) {
            LOGGER.severe("No database connection or commentContainer is null.");
            if (total_comments != null) total_comments.setText("Comments (0)");
            return;
        }

        String query = "SELECT u.username, r.comment, r.user_id, r.rating_id FROM ratings r " +
                "JOIN users u ON r.user_id = u.user_id WHERE r.book_id = ? AND r.comment IS NOT NULL " +
                "ORDER BY r.rating_id DESC";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            commentContainer.getChildren().clear();
            int commentCount = 0;
            int currentUserId = UserSession.getInstance().isLoggedIn() ? UserSession.getInstance().getCurrentUserId() : -1;

            while (rs.next()) {
                commentCount++;
                int commentUserId = rs.getInt("user_id");
                int ratingId = rs.getInt("rating_id");
                String username = rs.getString("username");
                String commentText = rs.getString("comment");

                HBox commentBox = new HBox(10);
                commentBox.setPadding(new Insets(5, 5, 5, 10));

                VBox commentContent = new VBox(5);
                Label usernameLabel = new Label("user: " + username);
                usernameLabel.setTextFill(javafx.scene.paint.Color.WHITE);
                usernameLabel.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 14.0));

                Label commentLabel = new Label(commentText);
                commentLabel.setTextFill(javafx.scene.paint.Color.WHITE);
                commentLabel.setFont(javafx.scene.text.Font.font("System", 14.0));
                commentLabel.setPadding(new Insets(0, 0, 0, 50));

                commentContent.getChildren().addAll(usernameLabel, commentLabel);
                commentBox.getChildren().add(commentContent);

                // Add delete button if the current user is the comment author
                if (currentUserId == commentUserId) {
                    Button deleteButton = new Button("x");
                    deleteButton.setStyle("-fx-background-color: #FF5555; -fx-text-fill: #FFFFFF; -fx-background-radius: 50%; " +
                            "-fx-min-width: 20; -fx-min-height: 20; -fx-max-width: 20; -fx-max-height: 20; " +
                            "-fx-font-size: 14; -fx-alignment: center; -fx-padding: 0; -fx-effect: dropshadow(gaussian, #000000, 2, 0.5, 0, 0);");
                    deleteButton.setOnAction(e -> deleteComment(ratingId));
                    commentBox.getChildren().add(deleteButton);
                }

                commentContainer.getChildren().add(commentBox);
            }
            if (total_comments != null) total_comments.setText("Comments (" + commentCount + ")");

            if (UserSession.getInstance().isLoggedIn()) {
                try (PreparedStatement userCommentStmt = conn.prepareStatement(
                        "SELECT u.username, r.comment FROM ratings r " +
                                "JOIN users u ON r.user_id = u.user_id WHERE r.book_id = ? AND r.user_id = ?")) {
                    userCommentStmt.setInt(1, bookId);
                    userCommentStmt.setInt(2, UserSession.getInstance().getCurrentUserId());
                    ResultSet userRs = userCommentStmt.executeQuery();
                    if (userRs.next()) {
                        if (user_name != null) user_name.setText("user: " + userRs.getString("username"));
                        if (user_comment != null) user_comment.setText(userRs.getString("comment") != null ? userRs.getString("comment") : "No comment");
                    } else {
                        if (user_name != null) user_name.setText("user: " + UserSession.getInstance().getUsername());
                        if (user_comment != null) user_comment.setText("No comment");
                    }
                }
            } else {
                if (user_name != null) user_name.setText("Guest");
                if (user_comment != null) user_comment.setText("Log in to comment");
            }
        } catch (SQLException e) {
            LOGGER.severe("Error loading comments: " + e.getMessage());
            if (total_comments != null) total_comments.setText("Comments (0)");
        }
    }

    private void deleteComment(int ratingId) {
        if (conn == null) {
            LOGGER.severe("No database connection for deleting comment.");
            showAlert(Alert.AlertType.ERROR, "Database Error", "No database connection available.");
            return;
        }

        String deleteQuery = "DELETE FROM ratings WHERE rating_id = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            stmt.setInt(1, ratingId);
            stmt.setInt(2, UserSession.getInstance().getCurrentUserId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.info("Comment deleted for ratingId: " + ratingId + ", userId: " + UserSession.getInstance().getCurrentUserId());
                loadComments(); // Refresh comments
                showAlert(Alert.AlertType.INFORMATION, "Success", "Your comment has been deleted.");
            } else {
                LOGGER.warning("No comment found to delete for ratingId: " + ratingId + ", userId: " + UserSession.getInstance().getCurrentUserId());
                showAlert(Alert.AlertType.WARNING, "No Comment", "You are not authorized to delete this comment.");
            }
        } catch (SQLException e) {
            LOGGER.severe("Error deleting comment for ratingId: " + ratingId + ": " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete comment.");
        }
    }

    private void loadChapters() {
        if (conn == null || chapterContainer == null) {
            LOGGER.severe("No database connection or chapterContainer is null.");
            if (chapters != null) chapters.setText("0");
            return;
        }

        String query = "SELECT chapter_id, chapter_number FROM chapters WHERE book_id = ? ORDER BY chapter_number";
        int count = 0;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, bookId);
            try (ResultSet rs = pstmt.executeQuery()) {
                VBox scrollContent = null;
                for (javafx.scene.Node node : chapterContainer.getChildren()) {
                    if (node instanceof ScrollPane) {
                        ScrollPane scrollPane = (ScrollPane) node;
                        if (scrollPane.getContent() instanceof VBox) {
                            scrollContent = (VBox) scrollPane.getContent();
                        }
                    }
                }

                if (scrollContent == null) {
                    LOGGER.severe("ScrollPane or inner VBox not found in chapterContainer.");
                    if (chapters != null) chapters.setText("0");
                    return;
                }

                scrollContent.getChildren().clear();

                while (rs.next()) {
                    count++;
                    int chapterId = rs.getInt("chapter_id");
                    int chapterNumber = rs.getInt("chapter_number");
                    Button chapterButton = new Button("Chapter " + chapterNumber);
                    chapterButton.setStyle("-fx-background-color: #F5E0CD; -fx-background-radius: 10;");
                    chapterButton.setPrefWidth(120.0);
                    chapterButton.setPrefHeight(30.0);
                    chapterButton.setFont(javafx.scene.text.Font.font("System", 15.0));
                    chapterButton.setOnAction(e -> {
                        if (isAuthorOrCoAuthor) {
                            openChapterForEdit(chapterId);
                        } else {
                            openChapter(chapterId);
                        }
                    });
                    scrollContent.getChildren().add(chapterButton);
                }

                if (chapters != null) chapters.setText(String.valueOf(count));
            }
        } catch (SQLException e) {
            LOGGER.severe("Error loading chapters: " + e.getMessage());
            if (chapters != null) chapters.setText("0");
        }
    }

    private void loadDrafts() {
        if (conn == null || draftContainer == null || !isAuthorOrCoAuthor) {
            LOGGER.info("No drafts loaded: No connection, no container, or user not author/co-author.");
            return;
        }

        String query = "SELECT draft_id, chapter_number FROM draft_chapters WHERE book_id = ? ORDER BY chapter_number";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            VBox scrollContent = null;
            for (javafx.scene.Node node : draftContainer.getChildren()) {
                if (node instanceof ScrollPane) {
                    ScrollPane scrollPane = (ScrollPane) node;
                    if (scrollPane.getContent() instanceof VBox) {
                        scrollContent = (VBox) scrollPane.getContent();
                    }
                }
            }

            if (scrollContent == null) {
                LOGGER.severe("ScrollPane or inner VBox not found in draftContainer.");
                return;
            }

            scrollContent.getChildren().clear();

            while (rs.next()) {
                int draftId = rs.getInt("draft_id");
                int chapterNumber = rs.getInt("chapter_number");
                Button draftButton = new Button("Draft for Chapter " + chapterNumber);
                draftButton.setStyle("-fx-background-color: transparent; -fx-border-color: #fff; -fx-border-radius: 5; -fx-text-fill: #FFFFFF;");
                draftButton.setPrefWidth(181.0);
                draftButton.setPrefHeight(26.0);
                draftButton.setOnAction(e -> openDraft(draftId));
                scrollContent.getChildren().add(draftButton);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error loading drafts: " + e.getMessage());
        }
    }

    private void updateViewCount() {
        if (conn == null || !UserSession.getInstance().isLoggedIn()) {
            LOGGER.warning("Cannot update view count: No database connection or user not logged in.");
            return;
        }
        try {
            int userId = UserSession.getInstance().getCurrentUserId();
            PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT visit_id FROM book_visits WHERE user_id = ? AND book_id = ?");
            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, bookId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE book_visits SET visited_at = ? WHERE user_id = ? AND book_id = ?");
                updateStmt.setTimestamp(1, Timestamp.valueOf(java.time.LocalDateTime.now()));
                updateStmt.setInt(2, userId);
                updateStmt.setInt(3, bookId);
                int rowsAffected = updateStmt.executeUpdate();
                LOGGER.info("Updated visit timestamp for userId: " + userId + ", bookId: " + bookId + ", rows affected: " + rowsAffected);
            } else {
                PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO book_visits (user_id, book_id, visited_at) VALUES (?, ?, ?)");
                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, bookId);
                insertStmt.setTimestamp(3, Timestamp.valueOf(java.time.LocalDateTime.now()));
                int rowsAffected = insertStmt.executeUpdate();
                PreparedStatement updateViewStmt = conn.prepareStatement(
                        "UPDATE books SET view_count = view_count + 1 WHERE book_id = ?");
                updateViewStmt.setInt(1, bookId);
                updateViewStmt.executeUpdate();
                if (views != null) {
                    views.setText(String.valueOf(Integer.parseInt(views.getText()) + 1));
                }
                LOGGER.info("Inserted new visit for userId: " + userId + ", bookId: " + bookId + ", rows affected: " + rowsAffected);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error updating view count: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update visit history.");
        }
    }

    private void deleteRating(int userId, int bookId) {
        if (conn == null) {
            LOGGER.severe("No database connection for deleting rating.");
            showAlert(Alert.AlertType.ERROR, "Database Error", "No database connection available.");
            return;
        }

        String deleteQuery = "DELETE FROM ratings WHERE user_id = ? AND book_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.info("Rating deleted for userId: " + userId + ", bookId: " + bookId);
                fetchBookDetails(); // Refresh book details to update average rating
                showAlert(Alert.AlertType.INFORMATION, "Success", "Your rating has been removed.");
            } else {
                LOGGER.warning("No rating found to delete for userId: " + userId + ", bookId: " + bookId);
                showAlert(Alert.AlertType.WARNING, "No Rating", "You have not rated this book.");
            }
        } catch (SQLException e) {
            LOGGER.severe("Error deleting rating for userId: " + userId + ", bookId: " + bookId + ": " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete rating.");
        }
    }

    // Modified handleRating method to include deletion logic
    private void handleRating(ActionEvent event) {
        if (!UserSession.getInstance().isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please log in to rate this book.");
            LOGGER.warning("Rating attempt failed: User not logged in for bookId: " + bookId);
            return;
        }

        int userId = UserSession.getInstance().getCurrentUserId();
        if (isAuthorOrCoAuthor(bookId, userId)) {
            showAlert(Alert.AlertType.WARNING, "Permission Denied", "Authors cannot rate their own book.");
            LOGGER.warning("Rating attempt failed: User " + userId + " is an author or co-author of bookId: " + bookId);
            return;
        }

        Integer rating = ratingComboBox.getValue();
        if (rating == null) {
            LOGGER.warning("No rating selected for bookId: " + bookId);
            return;
        }

        if (rating == 0) {
            deleteRating(userId, bookId);
            return;
        }

        if (rating < 0 || rating > 5) {
            showAlert(Alert.AlertType.WARNING, "Invalid Rating", "Please select a rating between 1 and 5.");
            LOGGER.warning("Invalid rating (" + rating + ") selected for bookId: " + bookId);
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO ratings (book_id, user_id, rating) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE rating = ?")) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, userId);
            stmt.setInt(3, rating);
            stmt.setInt(4, rating);
            stmt.executeUpdate();
            LOGGER.info("Rating " + rating + " submitted for bookId: " + bookId + " by userId: " + userId);
            fetchBookDetails();
        } catch (SQLException e) {
            LOGGER.severe("Error updating rating for bookId: " + bookId + ": " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit rating.");
        }
    }

    private void openChapter(int chapterId) {
        if (!UserSession.getInstance().isLoggedIn()) {
            LOGGER.info("User not logged in, cannot open chapter with chapterId: " + chapterId + " for bookId: " + bookId);
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please log in to read this chapter.");
            return;
        }

        if (mainController == null) {
            LOGGER.severe("Main controller is null, cannot navigate to read_chapter.fxml for chapterId: " + chapterId);
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to open chapter due to missing main controller.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/read_chapter.fxml"));
            Parent content = loader.load();
            read_chapter__c controller = loader.getController();
            controller.setMainController(mainController);
            controller.setBookId(bookId);
            controller.setChapterId(chapterId);
            AppState.getInstance().setPreviousFXML("/com/example/scribble/read_book.fxml");
            AppState.getInstance().setCurrentBookId(bookId);
            mainController.getCenterPane().getChildren().setAll(content);
            LOGGER.info("Opened chapter with chapterId: " + chapterId + ", bookId: " + bookId + " via mainController");

            // Update chapter read record
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO chapter_reads (user_id, chapter_id) VALUES (?, ?) " +
                            "ON DUPLICATE KEY UPDATE read_at = CURRENT_TIMESTAMP")) {
                stmt.setInt(1, UserSession.getInstance().getCurrentUserId());
                stmt.setInt(2, chapterId);
                stmt.executeUpdate();
                PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE books SET total_reads = total_reads + 1 WHERE book_id = ?");
                updateStmt.setInt(1, bookId);
                updateStmt.executeUpdate();
                if (reads != null) {
                    reads.setText(String.valueOf(Integer.parseInt(reads.getText()) + 1));
                }
                LOGGER.info("Updated chapter read record for chapterId: " + chapterId + ", bookId: " + bookId);
            } catch (SQLException e) {
                LOGGER.severe("Error updating chapter read record for chapterId: " + chapterId + ": " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to record chapter read.");
            }
        } catch (IOException e) {
            LOGGER.severe("Failed to load read_chapter.fxml for chapterId: " + chapterId + ": " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to open chapter: " + e.getMessage());
        }
    }



    private void openChapterForEdit(int chapterId) {
        if (!UserSession.getInstance().isLoggedIn()) {
            LOGGER.info("User not logged in, cannot edit chapter with chapterId: " + chapterId + " for bookId: " + bookId);
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please log in to edit this chapter.");
            return;
        }

        if (status != null && status.getText().equals("Complete")) {
            LOGGER.info("Cannot edit chapter with chapterId: " + chapterId + " for bookId: " + bookId + " as book is Complete");
            showAlert(Alert.AlertType.WARNING, "Permission Denied", "Cannot edit chapters for a completed book.");
            return;
        }

        if (mainController == null) {
            LOGGER.severe("Main controller is null, cannot navigate to write_chapter.fxml for chapterId: " + chapterId);
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to open chapter for edit due to missing main controller.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/write_chapter.fxml"));
            Parent content = loader.load();
            write_chapter__c controller = loader.getController();
            controller.setMainController(mainController);
            controller.setChapterId(chapterId);
            AppState.getInstance().setPreviousFXML("/com/example/scribble/read_book.fxml");
            AppState.getInstance().setCurrentBookId(bookId);
            mainController.getCenterPane().getChildren().setAll(content);
            LOGGER.info("Opened chapter for edit with ID: " + chapterId + ", bookId: " + bookId + " via mainController");
        } catch (IOException e) {
            LOGGER.severe("Error opening chapter for edit with chapterId: " + chapterId + ": " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to open chapter for edit: " + e.getMessage());
        }
    }

    private void openDraft(int draftId) {
        if (!UserSession.getInstance().isLoggedIn()) {
            LOGGER.info("User not logged in, cannot open draft with draftId: " + draftId + " for bookId: " + bookId);
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please log in to view this draft.");
            return;
        }

        if (status != null && status.getText().equals("Complete")) {
            LOGGER.info("Cannot edit draft with draftId: " + draftId + " for bookId: " + bookId + " as book is Complete");
            showAlert(Alert.AlertType.WARNING, "Permission Denied", "Cannot edit drafts for a completed book.");
            return;
        }

        if (mainController == null) {
            LOGGER.severe("Main controller is null, cannot navigate to write_chapter.fxml");
            showAlert(Alert.AlertType.ERROR, "Error", "Navigation failed: main controller not initialized.");
            return;
        }

        try {
            // Retrieve chapter number from draft_chapters
            String draftQuery = "SELECT chapter_number FROM draft_chapters WHERE draft_id = ?";
            int chapterNumber = -1;
            try (Connection conn = db_connect.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(draftQuery)) {
                stmt.setInt(1, draftId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    chapterNumber = rs.getInt("chapter_number");
                } else {
                    LOGGER.severe("No draft found for draft_id: " + draftId);
                    showAlert(Alert.AlertType.ERROR, "Error", "Draft not found.");
                    return;
                }
            } catch (SQLException e) {
                LOGGER.severe("Error querying draft chapter number: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load draft details.");
                return;
            }

            // Retrieve book title from books
            String bookQuery = "SELECT title FROM books WHERE book_id = ?";
            String bookTitle = null;
            try (Connection conn = db_connect.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(bookQuery)) {
                stmt.setInt(1, bookId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    bookTitle = rs.getString("title");
                } else {
                    LOGGER.severe("No book found for book_id: " + bookId);
                    showAlert(Alert.AlertType.ERROR, "Error", "Book not found.");
                    return;
                }
            } catch (SQLException e) {
                LOGGER.severe("Error querying book title: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load book details.");
                return;
            }

            // Load write_chapter.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/write_chapter.fxml"));
            Parent content = loader.load();
            write_chapter__c controller = loader.getController();

            // Set properties on the controller
            controller.setMainController(mainController); // Set mainController first
            controller.setBookId(bookId);
            controller.setDraftId(draftId);
            controller.setChapterNumber(chapterNumber);
            controller.setBookName(bookTitle);
            controller.setUserId(UserSession.getInstance().getUserId());

            // Update the center pane
            AppState.getInstance().setPreviousFXML("/com/example/scribble/read_book.fxml");
            AppState.getInstance().setCurrentBookId(bookId);
            mainController.getCenterPane().getChildren().setAll(content);
            LOGGER.info("Opened draft with ID: " + draftId + ", chapter number: " + chapterNumber + ", book title: " + bookTitle + " via mainController");
        } catch (IOException e) {
            LOGGER.severe("Failed to load write_chapter.fxml: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open draft.");
        }
    }

    @FXML
    private void handle_read(ActionEvent event) {
        if (chapterContainer == null || chapterContainer.getChildren().isEmpty()) {
            LOGGER.warning("No chapters available for bookId: " + bookId);
            showAlert(Alert.AlertType.WARNING, "No Chapters", "This book has no chapters available.");
            return;
        }
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT chapter_id FROM chapters WHERE book_id = ? ORDER BY chapter_number LIMIT 1")) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                openChapter(rs.getInt("chapter_id"));
            } else {
                showAlert(Alert.AlertType.WARNING, "No Chapters", "This book has no chapters available.");
            }
        } catch (SQLException e) {
            LOGGER.severe("Error fetching first chapter: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open first chapter.");
        }
    }

    @FXML
    private void handle_request_collab(ActionEvent event) {
        if (!UserSession.getInstance().isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please log in to request collaboration.");
            return;
        }

        if (isAuthorOrCoAuthor(bookId, UserSession.getInstance().getCurrentUserId())) {
            showAlert(Alert.AlertType.WARNING, "Permission Denied", "You cannot request collaboration on your own book.");
            LOGGER.warning("Collaboration request failed: User " + UserSession.getInstance().getCurrentUserId() + " is an author or co-author of bookId: " + bookId);
            return;
        }

        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Collaboration Request");

        VBox vbox = new VBox(10);
        Label emailLabel = new Label("Owner Email:");
        TextField emailField = new TextField();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT u.email FROM book_authors ba JOIN users u ON ba.user_id = u.user_id " +
                        "WHERE ba.book_id = ? AND ba.role = 'Owner'")) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                emailField.setText(rs.getString("email"));
                emailField.setEditable(false);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error fetching owner email: " + e.getMessage());
        }
        Label messageLabel = new Label("Message:");
        TextArea messageArea = new TextArea();
        Button sendButton = new Button("Send Request");
        sendButton.setOnAction(e -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO collaboration_invites (book_id, inviter_id, invitee_email, invite_code, message) " +
                            "VALUES (?, ?, ?, ?, ?)")) {
                stmt.setInt(1, bookId);
                stmt.setInt(2, UserSession.getInstance().getCurrentUserId());
                stmt.setString(3, emailField.getText());
                stmt.setString(4, UUID.randomUUID().toString().substring(0, 10));
                stmt.setString(5, messageArea.getText());
                stmt.executeUpdate();
                popupStage.close();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Collaboration request sent!");
            } catch (SQLException ex) {
                LOGGER.severe("Error sending collaboration request: " + ex.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to send collaboration request.");
            }
        });
        vbox.getChildren().addAll(emailLabel, emailField, messageLabel, messageArea, sendButton);
        Scene scene = new Scene(vbox, 400, 300);
        popupStage.setScene(scene);
        popupStage.show();
    }

    @FXML
    private void handle_saved_books_button(ActionEvent event) {
        LOGGER.info("Saved books button clicked");
        if (!UserSession.getInstance().isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please log in to save this book.");
            LOGGER.warning("Save attempt failed: User not logged in.");
            return;
        }

        int userId = UserSession.getInstance().getCurrentUserId();
        LOGGER.info("User ID: " + userId + ", Book ID: " + bookId);
        if (userId <= 0) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid user session. Please log in again.");
            LOGGER.severe("Save attempt failed: Invalid user ID " + userId);
            return;
        }

        if (bookId <= 0) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid book selected.");
            LOGGER.severe("Save attempt failed: Invalid book ID " + bookId);
            return;
        }

        ChoiceDialog<String> statusDialog = new ChoiceDialog<>("SavedForLater",
                new ArrayList<>(List.of("Reading", "Completed", "Dropped", "SavedForLater")));
        statusDialog.setTitle("Select Reading Status");
        statusDialog.setHeaderText("Choose a status for the book:");
        statusDialog.setContentText("Status:");
        statusDialog.showAndWait().ifPresentOrElse(selectedStatus -> {
            try {
                if (conn == null || conn.isClosed()) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Database connection lost.");
                    LOGGER.severe("Save attempt failed: Database connection is null or closed.");
                    return;
                }
                LOGGER.info("Database connection state: Connected");

                try (PreparedStatement checkUser = conn.prepareStatement("SELECT user_id FROM users WHERE user_id = ?")) {
                    checkUser.setInt(1, userId);
                    if (!checkUser.executeQuery().next()) {
                        showAlert(Alert.AlertType.ERROR, "Error", "User does not exist.");
                        LOGGER.severe("Save attempt failed: User ID " + userId + " not found in users table.");
                        return;
                    }
                }

                try (PreparedStatement checkBook = conn.prepareStatement("SELECT book_id FROM books WHERE book_id = ?")) {
                    checkBook.setInt(1, bookId);
                    if (!checkBook.executeQuery().next()) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Book does not exist.");
                        LOGGER.severe("Save attempt failed: Book ID " + bookId + " not found in books table.");
                        return;
                    }
                }

                try (PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO reading_list (reader_id, listed_book_id, reading_status) VALUES (?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE reading_status = ?")) {
                    pstmt.setInt(1, userId);
                    pstmt.setInt(2, bookId);
                    pstmt.setString(3, selectedStatus);
                    pstmt.setString(4, selectedStatus);
                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        LOGGER.info("Book saved with status '" + selectedStatus + "': bookId=" + bookId + ", userId=" + userId);
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Book saved with status: " + selectedStatus + "!");
                        recordBookVisit(userId, bookId);
                    } else {
                        LOGGER.warning("No rows affected when saving bookId=" + bookId + " for userId=" + userId + " with status=" + selectedStatus);
                        showAlert(Alert.AlertType.WARNING, "Warning", "Book could not be saved. Please try again.");
                    }
                }
            } catch (SQLException e) {
                String errorMsg = String.format("Error saving book: %s, SQLState: %s, ErrorCode: %d",
                        e.getMessage(), e.getSQLState(), e.getErrorCode());
                LOGGER.severe(errorMsg);
                String userMsg = "Failed to save book: ";
                if (e.getMessage().contains("foreign key constraint")) {
                    userMsg += "Invalid user or book ID.";
                } else if (e.getMessage().contains("Connection")) {
                    userMsg += "Database connection error.";
                } else if (e.getMessage().contains("Access denied")) {
                    userMsg += "Database permission error.";
                } else if (e.getMessage().contains("Incorrect string value") || e.getMessage().contains("ENUM")) {
                    userMsg += "Invalid reading status value.";
                } else {
                    userMsg += "Database error occurred.";
                }
                showAlert(Alert.AlertType.ERROR, "Error", userMsg);
            } catch (Exception e) {
                LOGGER.severe("Unexpected error in handle_saved_books_button: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred. Please try again.");
            }
        }, () -> {
            LOGGER.info("Reading status selection cancelled by user");
        });
    }

    private void recordBookVisit(int userId, int bookId) {
        try {
            PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT visit_id FROM book_visits WHERE user_id = ? AND book_id = ?");
            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, bookId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE book_visits SET visited_at = CURRENT_TIMESTAMP WHERE user_id = ? AND book_id = ?");
                updateStmt.setInt(1, userId);
                updateStmt.setInt(2, bookId);
                updateStmt.executeUpdate();
                LOGGER.info("Updated visit timestamp for userId: " + userId + ", bookId: " + bookId + " (from saving book)");
            } else {
                PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO book_visits (user_id, book_id, visited_at) VALUES (?, ?, CURRENT_TIMESTAMP)");
                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, bookId);
                insertStmt.executeUpdate();
                PreparedStatement updateViewStmt = conn.prepareStatement(
                        "UPDATE books SET view_count = view_count + 1 WHERE book_id = ?");
                updateViewStmt.setInt(1, bookId);
                updateViewStmt.executeUpdate();
                if (views != null) {
                    views.setText(String.valueOf(Integer.parseInt(views.getText()) + 1));
                }
                LOGGER.info("Inserted new visit for userId: " + userId + ", bookId: " + bookId + " (from saving book)");
            }
        } catch (SQLException e) {
            LOGGER.severe("Error recording book visit: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to record book visit.");
        }
    }

    private void handleStatusUpdate(ActionEvent event) {
        if (!UserSession.getInstance().isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please log in to update the book status.");
            LOGGER.warning("Status update attempt failed: User not logged in for bookId: " + bookId);
            return;
        }

        int userId = UserSession.getInstance().getCurrentUserId();
        if (!isAuthor(bookId, userId)) {
            showAlert(Alert.AlertType.WARNING, "Permission Denied", "Only the book owner can update the status.");
            LOGGER.warning("Status update attempt failed: User " + userId + " is not the owner of bookId: " + bookId);
            return;
        }

        String selectedStatus = statusComboBox.getValue();
        if (selectedStatus == null) {
            LOGGER.warning("No status selected for bookId: " + bookId);
            showAlert(Alert.AlertType.WARNING, "Invalid Selection", "Please select a status.");
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE books SET status = ? WHERE book_id = ?")) {
            stmt.setString(1, selectedStatus);
            stmt.setInt(2, bookId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                LOGGER.info("Status updated to " + selectedStatus + " for bookId: " + bookId + " by userId: " + userId);
                if (status != null) {
                    status.setText(selectedStatus);
                }
                updateAddChapterButtonVisibility();
                updateDraftContainerVisibility();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Book status updated to " + selectedStatus + ".");
            } else {
                LOGGER.warning("No rows affected when updating status for bookId: " + bookId);
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update book status.");
            }
        } catch (SQLException e) {
            String errorMsg = String.format("Error updating status: %s, SQLState: %s, ErrorCode: %d",
                    e.getMessage(), e.getSQLState(), e.getErrorCode());
            LOGGER.severe(errorMsg);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update book status: " + e.getMessage());
        }
    }


    private void updateStatusComboBoxVisibility() {
        if (statusComboBox != null) {
            boolean isVisible = UserSession.getInstance().isLoggedIn() && isAuthor(bookId, UserSession.getInstance().getCurrentUserId());
            statusComboBox.setVisible(isVisible);
            statusComboBox.setManaged(isVisible);
            if (isVisible && status != null && !status.getText().isEmpty()) {
                statusComboBox.setValue(status.getText());
            }
            LOGGER.info("statusComboBox visibility: " + isVisible);
        }
    }

    @FXML
    private void handle_add_chapter_button(ActionEvent event) {
        if (!UserSession.getInstance().isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please log in to add a chapter.");
            LOGGER.warning("User not logged in, cannot add chapter for bookId: " + bookId);
            return;
        }

        int userId = UserSession.getInstance().getUserId();
        if (userId <= 0) {
            showAlert(Alert.AlertType.ERROR, "Session Error", "Invalid user session. Please log in again.");
            LOGGER.severe("Invalid userId from session: " + userId);
            return;
        }

        if (!isAuthorOrCoAuthor(bookId, userId)) {
            showAlert(Alert.AlertType.WARNING, "Permission Denied", "Only the author or co-author can add chapters.");
            LOGGER.warning("User " + userId + " not authorized for bookId: " + bookId);
            return;
        }

        TextInputDialog dialog = new TextInputDialog(String.valueOf(getNextChapterNumber(bookId)));
        dialog.setTitle("Add Chapter");
        dialog.setHeaderText("Enter chapter number for the new chapter:");
        dialog.setContentText("Chapter Number:");
        dialog.showAndWait().ifPresent(chapterNumberStr -> {
            try {
                int chapterNumber = Integer.parseInt(chapterNumberStr);
                if (chapterNumber <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Chapter number must be positive.");
                    LOGGER.warning("Invalid chapter number: " + chapterNumberStr);
                    return;
                }

                String bookTitle = null;
                try (Connection conn = db_connect.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("SELECT title FROM books WHERE book_id = ?")) {
                    stmt.setInt(1, bookId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        bookTitle = rs.getString("title");
                        if (bookTitle == null || bookTitle.trim().isEmpty()) {
                            bookTitle = "Untitled Book";
                            LOGGER.warning("No valid title for bookId: " + bookId + ", using default: Untitled Book");
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Invalid Book", "The specified book does not exist.");
                        LOGGER.severe("No book found for bookId: " + bookId);
                        return;
                    }
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to verify book existence.");
                    LOGGER.severe("Error querying book title for bookId: " + bookId + ": " + e.getMessage());
                    return;
                }

                if (chapterNumberExists(bookId, chapterNumber)) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Chapter Number",
                            "Chapter or draft number " + chapterNumber + " already exists for this book.");
                    LOGGER.warning("Chapter number conflict for bookId: " + bookId + ", chapterNumber: " + chapterNumber);
                    return;
                }

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/write_chapter.fxml"));
                    Parent content = loader.load();
                    write_chapter__c controller = loader.getController();
                    controller.setMainController(mainController);
                    controller.setBookId(bookId);
                    controller.setBookName(bookTitle);
                    controller.setUserId(userId);
                    controller.setChapterNumber(chapterNumber);
                    AppState.getInstance().setPreviousFXML("/com/example/scribble/read_book.fxml");
                    AppState.getInstance().setCurrentBookId(bookId);
                    if (mainController != null) {
                        mainController.getCenterPane().getChildren().setAll(content);
                        LOGGER.info("Navigated to add chapter page for bookId: " + bookId +
                                ", chapterNumber: " + chapterNumber + ", bookTitle: " + bookTitle +
                                ", userId: " + userId + " via mainController");
                    } else {
                        LOGGER.severe("Main controller is null, navigation to new scene failed for bookId: " + bookId);
                        showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to open add chapter page due to missing main controller.");
                    }
                } catch (IOException e) {
                    LOGGER.severe("Failed to load write_chapter.fxml: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to open add chapter page.");
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid chapter number.");
                LOGGER.warning("Invalid chapter number format: " + chapterNumberStr);
            }
        });
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
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not determine next chapter number.");
        }
        return 1; // Default to 1 if query fails
    }


    @FXML
    private void handle_support_author(ActionEvent event) {
        if (!UserSession.getInstance().isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please log in to support the author.");
            return;
        }

        if (isAuthorOrCoAuthor(bookId, UserSession.getInstance().getCurrentUserId())) {
            showAlert(Alert.AlertType.WARNING, "Permission Denied", "You cannot support your own book.");
            LOGGER.warning("Support attempt failed: User " + UserSession.getInstance().getCurrentUserId() + " is an author or co-author of bookId: " + bookId);
            return;
        }

        if (mainController == null) {
            LOGGER.severe("Main controller is null, cannot navigate to support author page for bookId: " + bookId);
            showAlert(Alert.AlertType.ERROR, "Error", "Navigation failed: main controller is not initialized.");
            return;
        }

        try {
            AppState.getInstance().setCurrentBookId(bookId);
            AppState.getInstance().setPreviousFXML("/com/example/scribble/read_book.fxml");
            LOGGER.info("Stored in AppState: bookId=" + bookId + ", previousFXML=read_book.fxml");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/support_author.fxml"));
            Parent content = loader.load();
            support_author__c supportAuthorController = loader.getController();
            supportAuthorController.setMainController(mainController);
            supportAuthorController.setBookId(bookId);
            supportAuthorController.setUserId(UserSession.getInstance().getUserId());

            mainController.getCenterPane().getChildren().setAll(content);
            LOGGER.info("Navigated to support author page via mainController with bookId: " + bookId);
        } catch (IOException e) {
            LOGGER.severe("Failed to load support_author.fxml: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open support page: " + e.getMessage());
        }
    }

    @FXML
    private void handle_add_comment(ActionEvent event) {
        if (!UserSession.getInstance().isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please log in to add a comment.");
            return;
        }
        if (comment_box == null) {
            LOGGER.severe("comment_box is null, cannot add comment.");
            showAlert(Alert.AlertType.ERROR, "Error", "Comment input field is not available.");
            return;
        }
        String comment = comment_box.getText().trim();
        if (comment.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please enter a comment.");
            return;
        }
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO ratings (book_id, user_id, comment) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE comment = ?")) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, UserSession.getInstance().getCurrentUserId());
            stmt.setString(3, comment);
            stmt.setString(4, comment);
            stmt.executeUpdate();
            comment_box.clear();
            loadComments();
            LOGGER.info("Comment added for bookId: " + bookId);
        } catch (SQLException e) {
            LOGGER.severe("Error adding comment: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add comment.");
        }
    }

    @FXML
    private void handle_add_draft(ActionEvent event) {
        if (!UserSession.getInstance().isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please log in to add a draft.");
            LOGGER.warning("User not logged in, cannot add draft for bookId: " + bookId);
            return;
        }

        int userId = UserSession.getInstance().getUserId();
        if (userId <= 0) {
            showAlert(Alert.AlertType.ERROR, "Session Error", "Invalid user session. Please log in again.");
            LOGGER.severe("Invalid userId from session: " + userId);
            return;
        }

        if (!isAuthorOrCoAuthor) {
            showAlert(Alert.AlertType.WARNING, "Permission Denied", "Only the author or co-author can add drafts.");
            LOGGER.warning("User " + userId + " not authorized for bookId: " + bookId);
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Draft");
        dialog.setHeaderText("Enter chapter number for the draft:");
        dialog.setContentText("Chapter Number:");
        dialog.showAndWait().ifPresent(chapterNumberStr -> {
            try {
                int chapterNumber = Integer.parseInt(chapterNumberStr);
                if (chapterNumber <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Chapter number must be positive.");
                    LOGGER.warning("Invalid chapter number: " + chapterNumberStr);
                    return;
                }

                String bookTitle = null;
                try (Connection conn = db_connect.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("SELECT title FROM books WHERE book_id = ?")) {
                    stmt.setInt(1, bookId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        bookTitle = rs.getString("title");
                        if (bookTitle == null || bookTitle.trim().isEmpty()) {
                            bookTitle = "Untitled Book";
                            LOGGER.warning("No valid title for bookId: " + bookId + ", using default: Untitled Book");
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Invalid Book", "The specified book does not exist.");
                        LOGGER.severe("No book found for bookId: " + bookId);
                        return;
                    }
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to verify book existence.");
                    LOGGER.severe("Error querying book title for bookId: " + bookId + ": " + e.getMessage());
                    return;
                }

                if (chapterNumberExists(bookId, chapterNumber)) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Chapter Number",
                            "Chapter or draft number " + chapterNumber + " already exists for this book.");
                    LOGGER.warning("Chapter number conflict for bookId: " + bookId + ", chapterNumber: " + chapterNumber);
                    return;
                }

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/write_chapter.fxml"));
                    Parent content = loader.load();
                    write_chapter__c controller = loader.getController();
                    controller.setMainController(mainController);
                    controller.setBookId(bookId);
                    controller.setBookName(bookTitle);
                    controller.setUserId(userId);
                    controller.setChapterNumber(chapterNumber);
                    AppState.getInstance().setPreviousFXML("/com/example/scribble/read_book.fxml");
                    AppState.getInstance().setCurrentBookId(bookId);
                    if (mainController != null) {
                        mainController.getCenterPane().getChildren().setAll(content);
                        LOGGER.info("Navigated to add draft page for bookId: " + bookId +
                                ", chapterNumber: " + chapterNumber + ", bookTitle: " + bookTitle +
                                ", userId: " + userId + " via mainController");
                    } else {
                        LOGGER.severe("Main controller is null, navigation to new scene failed for bookId: " + bookId);
                        showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to open add draft page due to missing main controller.");
                    }
                } catch (IOException e) {
                    LOGGER.severe("Failed to load write_chapter.fxml: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to open add draft page.");
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid chapter number.");
                LOGGER.warning("Invalid chapter number format: " + chapterNumberStr);
            }
        });
    }

    @FXML
    private void handle_chapterContainer(ActionEvent event) {
        Object source = event.getSource();
        if (source instanceof Button chapterButton && chapterContainer.getChildren().contains(chapterButton)) {
            String buttonText = chapterButton.getText();
            if (buttonText.startsWith("Chapter ")) {
                try {
                    int chapterNumber = Integer.parseInt(buttonText.replace("Chapter ", ""));
                    String query = "SELECT chapter_id FROM chapters WHERE book_id = ? AND chapter_number = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setInt(1, bookId);
                        stmt.setInt(2, chapterNumber);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            int chapterId = rs.getInt("chapter_id");
                            if (isAuthorOrCoAuthor) {
                                openChapterForEdit(chapterId);
                            } else {
                                openChapter(chapterId);
                            }
                            LOGGER.info("Chapter opened via container click: chapterId=" + chapterId);
                        } else {
                            LOGGER.warning("No chapter found for chapter_number: " + chapterNumber);
                            showAlert(Alert.AlertType.ERROR, "Error", "Chapter not found.");
                        }
                    }
                } catch (NumberFormatException | SQLException e) {
                    LOGGER.severe("Error processing chapter button click: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to open chapter.");
                }
            }
        } else {
            LOGGER.info("Chapter container clicked outside a chapter button.");
        }
    }

    @FXML
    private void handle_draftContainer(ActionEvent event) {
        Object source = event.getSource();
        if (source instanceof Button draftButton && draftContainer.getChildren().contains(draftButton)) {
            String buttonText = draftButton.getText();
            if (buttonText.startsWith("Draft for Chapter ")) {
                try {
                    int chapterNumber = Integer.parseInt(buttonText.replace("Draft for Chapter ", ""));
                    String query = "SELECT draft_id FROM draft_chapters WHERE book_id = ? AND chapter_number = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setInt(1, bookId);
                        stmt.setInt(2, chapterNumber);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            int draftId = rs.getInt("draft_id");
                            openDraft(draftId);
                            LOGGER.info("Draft opened via container click: draftId=" + draftId);
                        } else {
                            LOGGER.warning("No draft found for chapter_number: " + chapterNumber);
                            showAlert(Alert.AlertType.ERROR, "Error", "Draft not found.");
                        }
                    }
                } catch (NumberFormatException | SQLException e) {
                    LOGGER.severe("Error processing draft button click: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to open draft.");
                }
            }
        } else {
            LOGGER.info("Draft container clicked outside a draft button.");
        }
    }

    private void loadAuthors() {
        if (conn == null || author_profile == null || authorContainer == null) {
            LOGGER.severe("Database connection or UI elements are null.");
            return;
        }
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT u.user_id, u.username, ba.role FROM book_authors ba " +
                        "JOIN users u ON ba.user_id = u.user_id WHERE ba.book_id = ?")) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            String authorName = "Unknown";
            List<String> coAuthors = new ArrayList<>();
            while (rs.next()) {
                String role = rs.getString("role");
                String username = rs.getString("username");
                int userId = rs.getInt("user_id");
                if (role.equals("Owner")) {
                    authorName = username;
                    authorId = userId;
                } else if (role.equals("Co-Author")) {
                    coAuthors.add(username);
                }
            }
            author_profile.setText("Author: " + authorName);
            if (co_author_names != null) {
                if (coAuthors.isEmpty()) {
                    co_author_names.setText("Co-author: None");
                } else {
                    co_author_names.setText("Co-author: " + String.join(", ", coAuthors));
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error loading authors: " + e.getMessage());
            if (author_profile != null) author_profile.setText("Author: Unknown");
            if (co_author_names != null) co_author_names.setText("Co-author: None");
        }
    }

    @FXML
    private void handleAuthorProfile(ActionEvent event) {
        if (!UserSession.getInstance().isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please log in to view the author profile.");
            LOGGER.warning("Access to author profile denied: User not logged in for bookId: " + bookId);
            return;
        }

        if (authorId <= 0) {
            showAlert(Alert.AlertType.ERROR, "Error", "Author not found for this book.");
            LOGGER.severe("Invalid authorId: " + authorId + " for bookId: " + bookId);
            return;
        }

        try {
            AppState.getInstance().setCurrentBookId(bookId);
            LOGGER.info("Stored bookId in AppState: " + bookId);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/author_profile.fxml"));
            Parent content = loader.load();
            author_profile__c controller = loader.getController();
            controller.setAuthorId(authorId);
            controller.setBookId(bookId);
            if (mainController != null) {
                controller.setMainController(mainController);
                mainController.getCenterPane().getChildren().setAll(content);
                shutdown();
                LOGGER.info("Navigated to author profile for authorId: " + authorId + " via mainController");
            } else {
                controller.setMainController(null);
                Scene scene = new Scene(content);
                Stage stage = (Stage) author_profile.getScene().getWindow();
                stage.setScene(scene);
                shutdown();
                LOGGER.info("Navigated to author profile for authorId: " + authorId + " via stage");
            }
        } catch (IOException e) {
            LOGGER.severe("Failed to load author_profile.fxml: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open author profile.");
        }
    }

    @FXML
    private void handle_back_button(ActionEvent event) {
        shutdown();
        if (mainController != null) {
            mainController.loadFXML("reading_list.fxml");
            LOGGER.info("Navigated back to reading list via mainController.");
        } else {
            LOGGER.severe("mainController is null, cannot navigate back to reading list.");
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Main controller not set.");
        }
    }

    private void updateAddChapterButtonVisibility() {
        if (add_chapter != null) {
            boolean isBookComplete = status != null && status.getText().equals("Complete");
            boolean isVisible = isAuthorOrCoAuthor && !isBookComplete;
            add_chapter.setVisible(isVisible);
            add_chapter.setManaged(isVisible);
            LOGGER.info("add_chapter visibility: " + isVisible + ", isBookComplete: " + isBookComplete);
        }
    }

    private void updateDraftContainerVisibility() {
        if (draftContainer != null) {
            boolean isBookComplete = status != null && status.getText().equals("Complete");
            boolean isVisible = isAuthorOrCoAuthor && !isBookComplete;
            draftContainer.setVisible(isVisible);
            draftContainer.setManaged(isVisible);
            LOGGER.info("draftContainer visibility: " + isVisible + ", isBookComplete: " + isBookComplete);
        }
    }

    private boolean isAuthorOrCoAuthor(int bookId, int userId) {
        String query = "SELECT COUNT(*) FROM book_authors WHERE book_id = ? AND user_id = ?";
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                boolean isAuthor = rs.getInt(1) > 0;
                LOGGER.info("Author check for bookId: " + bookId + ", userId: " + userId + " -> " + isAuthor);
                return isAuthor;
            }
        } catch (SQLException e) {
            LOGGER.severe("Error verifying author for bookId: " + bookId + ", userId: " + userId + ": " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to verify authorization.");
        }
        return false;
    }

    private boolean chapterNumberExists(int bookId, int chapterNumber) {
        String query = """
            SELECT (SELECT COUNT(*) FROM chapters WHERE book_id = ? AND chapter_number = ?) +
                   (SELECT COUNT(*) FROM draft_chapters WHERE book_id = ? AND chapter_number = ?) AS count
        """;
        try (Connection conn = db_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, chapterNumber);
            stmt.setInt(3, bookId);
            stmt.setInt(4, chapterNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                boolean exists = rs.getInt("count") > 0;
                LOGGER.info("Chapter number exists check for bookId: " + bookId + ", chapterNumber: " +
                        chapterNumber + " -> " + exists);
                return exists;
            }
        } catch (SQLException e) {
            LOGGER.severe("Error checking chapter number for bookId: " + bookId + ", chapterNumber: " +
                    chapterNumber + ": " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to check chapter number.");
        }
        return false;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void shutdown() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                LOGGER.info("Database connection closed successfully.");
            }
        } catch (SQLException e) {
            LOGGER.severe("Error closing database connection: " + e.getMessage());
        }
    }
}