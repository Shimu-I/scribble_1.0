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
    private boolean isAuthorOrCoAuthor;
    private int authorId;

    @FXML private Label bookTitleLabel, views, chapters, reads, avg_ratting, status, genre, book_description, user_name, user_comment, total_comments;
    @FXML private ComboBox<Integer> ratingComboBox;
    @FXML private Button read_now_button, support_author_button, request_collab_button, saved_books_button, author_profile, back_button, add_chapter, add_comment, add_draft;
    @FXML private VBox chapterContainer, commentContainer, draftContainer;
    @FXML private HBox authorContainer;
    @FXML private ImageView coverImage;

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
        isAuthorOrCoAuthor = isUserAuthorOrCoAuthor();
        fetchBookDetails();
        loadChapters();
        loadComments();
        loadDrafts();
        updateAddChapterButtonVisibility();
        updateDraftContainerVisibility();
        updateViewCount();
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
                       COALESCE((SELECT AVG(rating) FROM ratings WHERE book_id = b.book_id), 0.0) AS avg_rating,
                       (SELECT rating FROM ratings WHERE book_id = b.book_id AND user_id = ?) AS user_rating
                FROM books b
                WHERE b.book_id = ?
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, UserSession.getInstance().getCurrentUserId());
            pstmt.setInt(2, bookId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    if (bookTitleLabel != null) bookTitleLabel.setText(rs.getString("title") != null ? rs.getString("title") : "Unknown");
                    if (views != null) views.setText(rs.getInt("view_count") + "");
                    if (reads != null) reads.setText(rs.getInt("total_reads") + "");
                    if (status != null) status.setText(rs.getString("status") != null ? rs.getString("status") : "N/A");
                    if (genre != null) genre.setText(rs.getString("genre") != null ? rs.getString("genre") : "N/A");
                    if (book_description != null) book_description.setText(rs.getString("description") != null ? rs.getString("description") : "No description");
                    if (coverImage != null) {
                        String coverPhoto = rs.getString("cover_photo");
                        if (coverPhoto != null && !coverPhoto.isEmpty()) {
                            try {
                                Image image = new Image(getClass().getResourceAsStream("/images/book_covers/" + coverPhoto));
                                coverImage.setImage(image);
                            } catch (Exception e) {
                                LOGGER.severe("Error loading cover photo: " + e.getMessage());
                                coverImage.setImage(new Image(getClass().getResourceAsStream("/images/book_covers/demo_cover.png")));
                            }
                        } else {
                            coverImage.setImage(new Image(getClass().getResourceAsStream("/images/book_covers/demo_cover.png")));
                        }
                    }
                    double avgRating = rs.getDouble("avg_rating");
                    int userRating = rs.wasNull() ? 0 : rs.getInt("user_rating");
                    if (avg_ratting != null) avg_ratting.setText(userRating + " / " + String.format("%.1f", avgRating));
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

    private void loadAuthors() {
        if (conn == null || author_profile == null || authorContainer == null) return;
        authorContainer.getChildren().clear();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT u.user_id, u.username, ba.role FROM book_authors ba " +
                        "JOIN users u ON ba.user_id = u.user_id WHERE ba.book_id = ?")) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            List<String> coAuthors = new ArrayList<>();
            int ownerId = 0;
            String ownerName = "Unknown";
            while (rs.next()) {
                String role = rs.getString("role");
                String username = rs.getString("username");
                if (role.equals("Owner")) {
                    ownerId = rs.getInt("user_id");
                    ownerName = username;
                } else {
                    coAuthors.add(username + " (co-author)");
                }
            }
            author_profile.setText(ownerName + " (author)");
            final int finalOwnerId = ownerId;
            author_profile.setOnAction(e -> handleAuthorProfile(finalOwnerId));
            for (String coAuthor : coAuthors) {
                Label coAuthorLabel = new Label(coAuthor);
                authorContainer.getChildren().add(coAuthorLabel);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error loading authors: " + e.getMessage());
            author_profile.setText("Unknown (author)");
        }
    }

    private void loadComments() {
        if (conn == null || commentContainer == null) {
            LOGGER.severe("No database connection or commentContainer is null.");
            if (total_comments != null) total_comments.setText("Comments (0)");
            return;
        }

        String query = "SELECT u.username, r.comment FROM ratings r " +
                "JOIN users u ON r.user_id = u.user_id WHERE r.book_id = ? AND r.comment IS NOT NULL";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            commentContainer.getChildren().clear();
            int commentCount = 0;
            while (rs.next()) {
                commentCount++;
                VBox commentBox = new VBox(5);
                commentBox.setPadding(new Insets(5, 5, 5, 10)); // Match FXML padding

                Label usernameLabel = new Label("user: " + rs.getString("username"));
                usernameLabel.setTextFill(javafx.scene.paint.Color.WHITE);
                usernameLabel.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 14.0));

                Label commentLabel = new Label(rs.getString("comment"));
                commentLabel.setTextFill(javafx.scene.paint.Color.WHITE);
                commentLabel.setFont(javafx.scene.text.Font.font("System", 14.0));
                commentLabel.setPadding(new Insets(0, 0, 0, 50)); // Match FXML user_comment padding

                commentBox.getChildren().addAll(usernameLabel, commentLabel);
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
                // Find the ScrollPane's inner VBox (scrollable content)
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

                // Clear only the scrollable content
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

                if (isAuthorOrCoAuthor && add_chapter != null) {
                    scrollContent.getChildren().add(add_chapter);
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
            draftContainer.getChildren().clear();
            while (rs.next()) {
                int draftId = rs.getInt("draft_id");
                int chapterNumber = rs.getInt("chapter_number");
                Button draftButton = new Button("Draft for Chapter " + chapterNumber);
                draftButton.setOnAction(e -> openDraft(draftId));
                draftContainer.getChildren().add(draftButton);
            }
            if (add_draft != null) {
                draftContainer.getChildren().add(add_draft);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error loading drafts: " + e.getMessage());
        }
    }

    private void updateViewCount() {
        if (conn == null || !UserSession.getInstance().isLoggedIn()) return;
        try {
            PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT visit_id FROM book_visits WHERE user_id = ? AND book_id = ?");
            checkStmt.setInt(1, UserSession.getInstance().getCurrentUserId());
            checkStmt.setInt(2, bookId);
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO book_visits (user_id, book_id) VALUES (?, ?)");
                insertStmt.setInt(1, UserSession.getInstance().getCurrentUserId());
                insertStmt.setInt(2, bookId);
                insertStmt.executeUpdate();
                PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE books SET view_count = view_count + 1 WHERE book_id = ?");
                updateStmt.setInt(1, bookId);
                updateStmt.executeUpdate();
                if (views != null) views.setText(String.valueOf(Integer.parseInt(views.getText()) + 1));
            }
        } catch (SQLException e) {
            LOGGER.severe("Error updating view count: " + e.getMessage());
        }
    }

    private void handleRating(ActionEvent event) {
        if (!UserSession.getInstance().isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please log in to rate this book.");
            return;
        }
        Integer rating = ratingComboBox.getValue();
        if (rating == null) return;
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO ratings (book_id, user_id, rating) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE rating = ?")) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, UserSession.getInstance().getCurrentUserId());
            stmt.setInt(3, rating);
            stmt.setInt(4, rating);
            stmt.executeUpdate();
            fetchBookDetails();
        } catch (SQLException e) {
            LOGGER.severe("Error updating rating: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit rating.");
        }
    }

    private void openChapter(int chapterId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/read_chapter.fxml"));
            Parent content = loader.load();
            read_chapter__c controller = loader.getController();
            controller.setChapterId(chapterId);
            if (mainController != null) {
                mainController.getCenterPane().getChildren().setAll(content);
                LOGGER.info("Opened chapter with ID: " + chapterId + " via mainController");
            } else {
                Scene scene = new Scene(content);
                Stage stage = (Stage) read_now_button.getScene().getWindow();
                stage.setScene(scene);
                LOGGER.info("Opened chapter with ID: " + chapterId);
            }
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
                if (reads != null) reads.setText(String.valueOf(Integer.parseInt(reads.getText()) + 1));
            }
        } catch (SQLException | IOException e) {
            LOGGER.severe("Error opening chapter: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open chapter.");
        }
    }

    private void openChapterForEdit(int chapterId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/write_chapter.fxml"));
            Parent content = loader.load();
            write_chapter__c controller = loader.getController();
            controller.setBookId(bookId);
            controller.setChapterId(chapterId);
            if (mainController != null) {
                mainController.getCenterPane().getChildren().setAll(content);
                LOGGER.info("Opened chapter for edit with ID: " + chapterId + " via mainController");
            } else {
                Scene scene = new Scene(content);
                Stage stage = (Stage) chapterContainer.getScene().getWindow();
                stage.setScene(scene);
                LOGGER.info("Opened chapter for edit with ID: " + chapterId);
            }
        } catch (IOException e) {
            LOGGER.severe("Failed to load write_chapter.fxml: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open chapter for editing.");
        }
    }

    private void openDraft(int draftId) {
        try {
            // Query draft_chapters to get chapter_number
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

            // Query books to get title
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

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/write_chapter.fxml"));
            Parent content = loader.load();
            write_chapter__c controller = loader.getController();
            controller.setBookId(bookId);
            controller.setDraftId(draftId);
            controller.setChapterNumber(chapterNumber);
            controller.setBookName(bookTitle); // Explicitly set book title
            if (mainController != null) {
                mainController.getCenterPane().getChildren().setAll(content);
                LOGGER.info("Opened draft with ID: " + draftId + ", chapter number: " + chapterNumber + ", book title: " + bookTitle + " via mainController");
            } else {
                Scene scene = new Scene(content);
                Stage stage = (Stage) draftContainer.getScene().getWindow();
                stage.setScene(scene);
                LOGGER.info("Opened draft with ID: " + draftId + ", chapter number: " + chapterNumber + ", book title: " + bookTitle);
            }
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

        try {
            if (conn == null || conn.isClosed()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Database connection lost.");
                LOGGER.severe("Save attempt failed: Database connection is null or closed.");
                return;
            }
            LOGGER.info("Database connection state: Connected");

            // Validate userId and bookId
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
                    "INSERT INTO reading_list (reader_id, listed_book_id, reading_status) VALUES (?, ?, 'SavedForLater') " +
                            "ON DUPLICATE KEY UPDATE reading_status = 'SavedForLater'")) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, bookId);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    LOGGER.info("Book saved for later: bookId=" + bookId + ", userId=" + userId);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Book saved for later!");
                } else {
                    LOGGER.warning("No rows affected when saving bookId=" + bookId + " for userId=" + userId);
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
    }
    @FXML
    private void handle_add_chapter_button(ActionEvent event) {
        if (!UserSession.getInstance().isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please log in to add a chapter.");
            return;
        }
        if (!isAuthorOrCoAuthor) {
            showAlert(Alert.AlertType.WARNING, "Permission Denied", "Only the author or co-author can add chapters.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Chapter");
        dialog.setHeaderText("Enter chapter number:");
        dialog.setContentText("Chapter Number:");
        dialog.showAndWait().ifPresent(chapterNumberStr -> {
            try {
                int chapterNumber = Integer.parseInt(chapterNumberStr);
                if (chapterNumber <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Chapter number must be positive.");
                    return;
                }
                // Check if chapter number already exists
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT COUNT(*) FROM chapters WHERE book_id = ? AND chapter_number = ?")) {
                    stmt.setInt(1, bookId);
                    stmt.setInt(2, chapterNumber);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        showAlert(Alert.AlertType.ERROR, "Invalid Input", "Chapter number already exists.");
                        return;
                    }
                }
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/write_chapter.fxml"));
                    Parent content = loader.load();
                    write_chapter__c controller = loader.getController();
                    controller.setBookId(bookId);
                    controller.setChapterNumber(chapterNumber);
                    if (mainController != null) {
                        mainController.getCenterPane().getChildren().setAll(content);
                        LOGGER.info("Navigated to add chapter page for bookId: " + bookId + ", chapterNumber: " + chapterNumber + " via mainController");
                    } else {
                        Scene scene = new Scene(content);
                        Stage stage = (Stage) add_chapter.getScene().getWindow();
                        stage.setScene(scene);
                        LOGGER.info("Navigated to add chapter page for bookId: " + bookId + ", chapterNumber: " + chapterNumber);
                    }
                } catch (IOException e) {
                    LOGGER.severe("Failed to load write_chapter.fxml: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to open add chapter page.");
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid chapter number.");
            } catch (SQLException e) {
                LOGGER.severe("Error checking chapter number: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to validate chapter number.");
            }
        });
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

    @FXML
    private void handle_support_author(ActionEvent event) {
        if (!UserSession.getInstance().isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please log in to support the author.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/support_author.fxml"));
            Parent content = loader.load();
            if (mainController != null) {
                mainController.getCenterPane().getChildren().setAll(content);
                LOGGER.info("Navigated to support author page via mainController.");
            } else {
                Scene scene = new Scene(content);
                Stage stage = (Stage) support_author_button.getScene().getWindow();
                stage.setScene(scene);
                LOGGER.info("Navigated to support author page.");
            }
        } catch (IOException e) {
            LOGGER.severe("Failed to load support_author.fxml: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open support page.");
        }
    }

    private void handleAuthorProfile(int authorId) {
        if (authorId == 0) {
            showAlert(Alert.AlertType.ERROR, "Error", "Author not found for this book.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/profile.fxml"));
            Parent content = loader.load();
            profile__c controller = loader.getController();
            controller.setAuthorId(authorId);
            if (mainController != null) {
                mainController.getCenterPane().getChildren().setAll(content);
                LOGGER.info("Navigated to author profile for authorId: " + authorId + " via mainController");
            } else {
                Scene scene = new Scene(content);
                Stage stage = (Stage) author_profile.getScene().getWindow();
                stage.setScene(scene);
                LOGGER.info("Navigated to author profile for authorId: " + authorId);
            }
        } catch (IOException e) {
            LOGGER.severe("Failed to load profile.fxml: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open author profile.");
        }
    }

    @FXML
    private void handle_add_comment(ActionEvent event) {
        if (!UserSession.getInstance().isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please log in to add a comment.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Comment");
        dialog.setHeaderText("Enter your comment:");
        dialog.setContentText("Comment:");
        dialog.showAndWait().ifPresent(comment -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO ratings (book_id, user_id, comment) VALUES (?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE comment = ?")) {
                stmt.setInt(1, bookId);
                stmt.setInt(2, UserSession.getInstance().getCurrentUserId());
                stmt.setString(3, comment);
                stmt.setString(4, comment);
                stmt.executeUpdate();
                loadComments();
                LOGGER.info("Comment added for bookId: " + bookId);
            } catch (SQLException e) {
                LOGGER.severe("Error adding comment: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add comment.");
            }
        });
    }

    @FXML
    private void handle_add_draft(ActionEvent event) {
        if (!UserSession.getInstance().isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please log in to add a draft.");
            return;
        }
        if (!isAuthorOrCoAuthor) {
            showAlert(Alert.AlertType.WARNING, "Permission Denied", "Only the author or co-author can add drafts.");
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
                    return;
                }
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/write_chapter.fxml"));
                    Parent content = loader.load();
                    write_chapter__c controller = loader.getController();
                    controller.setBookId(bookId);
                    controller.setChapterNumber(chapterNumber);
                    if (mainController != null) {
                        mainController.getCenterPane().getChildren().setAll(content);
                        LOGGER.info("Navigated to add draft page for bookId: " + bookId + ", chapterNumber: " + chapterNumber + " via mainController");
                    } else {
                        Scene scene = new Scene(content);
                        Stage stage = (Stage) draftContainer.getScene().getWindow();
                        stage.setScene(scene);
                        LOGGER.info("Navigated to add draft page for bookId: " + bookId + ", chapterNumber: " + chapterNumber);
                    }
                } catch (IOException e) {
                    LOGGER.severe("Failed to load write_chapter.fxml: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to open add draft page.");
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid chapter number.");
            }
        });
    }

    @FXML
    private void handle_chapterContainer(ActionEvent event) {
        // Get the source of the event
        Object source = event.getSource();
        if (source instanceof Button chapterButton && chapterContainer.getChildren().contains(chapterButton)) {
            // Extract chapter ID from button text (e.g., "Chapter 5")
            String buttonText = chapterButton.getText();
            if (buttonText.startsWith("Chapter ")) {
                try {
                    int chapterNumber = Integer.parseInt(buttonText.replace("Chapter ", ""));
                    // Query chapter_id for the chapter_number
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
            // Optional: Show alert or ignore
            // showAlert(Alert.AlertType.INFORMATION, "Info", "Please click a chapter button to open a chapter.");
        }
    }

    @FXML
    private void handle_draftContainer(ActionEvent event) {
        // Get the source of the event
        Object source = event.getSource();
        if (source instanceof Button draftButton && draftContainer.getChildren().contains(draftButton)) {
            // Extract chapter number from button text (e.g., "Draft for Chapter 5")
            String buttonText = draftButton.getText();
            if (buttonText.startsWith("Draft for Chapter ")) {
                try {
                    int chapterNumber = Integer.parseInt(buttonText.replace("Draft for Chapter ", ""));
                    // Query draft_id for the chapter_number
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
            // Optional: Show alert or ignore
            // showAlert(Alert.AlertType.INFORMATION, "Info", "Please click a draft button to open a draft.");
        }
    }

    private void updateAddChapterButtonVisibility() {
        if (add_chapter != null) {
            add_chapter.setVisible(isAuthorOrCoAuthor);
            add_chapter.setManaged(isAuthorOrCoAuthor);
            LOGGER.info("add_chapter visibility: " + isAuthorOrCoAuthor);
        }
    }

    private void updateDraftContainerVisibility() {
        if (draftContainer != null) {
            draftContainer.setVisible(isAuthorOrCoAuthor);
            draftContainer.setManaged(isAuthorOrCoAuthor);
            LOGGER.info("draftContainer visibility: " + isAuthorOrCoAuthor);
        }
    }

    private boolean isUserAuthorOrCoAuthor() {
        if (conn == null || !UserSession.getInstance().isLoggedIn()) return false;
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM book_authors WHERE book_id = ? AND user_id = ? AND role IN ('Owner', 'Co-Author')")) {
            pstmt.setInt(1, bookId);
            pstmt.setInt(2, UserSession.getInstance().getCurrentUserId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            LOGGER.severe("Error checking author/co-author status: " + e.getMessage());
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

    @FXML
    private void handle_author_profile(ActionEvent actionEvent) {
        handleAuthorProfile(authorId);
    }
}