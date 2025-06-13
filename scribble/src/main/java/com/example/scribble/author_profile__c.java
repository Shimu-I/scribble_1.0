package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class author_profile__c {
    private static final Logger LOGGER = Logger.getLogger(author_profile__c.class.getName());

    @FXML private Button back_button;
    @FXML private ImageView cover_photo;
    @FXML private Label author_name;
    @FXML private Label author_email;
    @FXML private Label joined_at;
    @FXML private Label total_number_of_author_works;
    @FXML private VBox all_button_work;
    @FXML private VBox vbox_container;
    @FXML private HBox book_card_container;
    @FXML private HBox book_card_hbox;
    @FXML private ImageView book_cover;
    @FXML private VBox label_vbox;
    @FXML private Label book_name;
    @FXML private Label post_on_date;

    private int authorId = 0;
    private int bookId = 0;
    private nav_bar__c mainController;
    private Author author;
    private List<Book> books;

    private final db_connect db_connect = new db_connect();

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
        LOGGER.info("setAuthorId called with authorId: " + authorId);
        if (authorId > 0) {
            fetchAuthorData();
            fetchAuthorBooks();
            displayAuthorData();
            displayAuthorBooks();
        } else {
            LOGGER.warning("Invalid authorId: " + authorId + ", skipping data fetch");
        }
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
        AppState.getInstance().setCurrentBookId(bookId);
        LOGGER.info("setBookId called with bookId: " + bookId + ", updated AppState");
    }

    public void setMainController(nav_bar__c mainController) {
        this.mainController = mainController;
        LOGGER.info("setMainController called with mainController: " + (mainController != null ? "non-null" : "null"));
    }

    @FXML
    public void initialize() {
        LOGGER.info("Initializing author_profile__c for authorId: " + authorId + ", bookId: " + bookId);

        logFXMLInjections();

        if (back_button == null || cover_photo == null || author_name == null || author_email == null ||
                joined_at == null || total_number_of_author_works == null || all_button_work == null ||
                vbox_container == null || book_card_container == null || book_card_hbox == null ||
                book_cover == null || label_vbox == null || book_name == null || post_on_date == null) {
            LOGGER.severe("One or more FXML elements are null. Check fx:id mappings in author_profile.fxml");
            showAlert(Alert.AlertType.ERROR, "Initialization Error", "Failed to initialize author profile UI.");
            return;
        }

        if (authorId <= 0) {
            LOGGER.info("Waiting for valid authorId to fetch data");
            author_name.setText("Loading...");
            author_email.setText("");
            joined_at.setText("");
            total_number_of_author_works.setText("(0)");
        }
    }

    private void logFXMLInjections() {
        LOGGER.info("FXML Injection Status:");
        LOGGER.info("back_button: " + (back_button != null ? "injected" : "null"));
        LOGGER.info("cover_photo: " + (cover_photo != null ? "injected" : "null"));
        LOGGER.info("author_name: " + (author_name != null ? "injected" : "null"));
        LOGGER.info("author_email: " + (author_email != null ? "injected" : "null"));
        LOGGER.info("joined_at: " + (joined_at != null ? "injected" : "null"));
        LOGGER.info("total_number_of_author_works: " + (total_number_of_author_works != null ? "injected" : "null"));
        LOGGER.info("all_button_work: " + (all_button_work != null ? "injected" : "null"));
        LOGGER.info("vbox_container: " + (vbox_container != null ? "injected" : "null"));
        LOGGER.info("book_card_container: " + (book_card_container != null ? "injected" : "null"));
        LOGGER.info("book_card_hbox: " + (book_card_hbox != null ? "injected" : "null"));
        LOGGER.info("book_cover: " + (book_cover != null ? "injected" : "null"));
        LOGGER.info("label_vbox: " + (label_vbox != null ? "injected" : "null"));
        LOGGER.info("book_name: " + (book_name != null ? "injected" : "null"));
        LOGGER.info("post_on_date: " + (post_on_date != null ? "injected" : "null"));
    }

    @FXML
    private void handle_back_button(ActionEvent event) {
        LOGGER.info("Back button clicked for authorId: " + authorId + ", bookId: " + bookId);
        try {
            int targetBookId = AppState.getInstance().getCurrentBookId();
            if (targetBookId <= 0) {
                targetBookId = bookId > 0 ? bookId : 1;
                LOGGER.warning("AppState bookId invalid, using instance bookId: " + targetBookId);
            }
            LOGGER.info("Navigating back to read_book.fxml with bookId: " + targetBookId);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/read_book.fxml"));
            Parent content = loader.load();
            read_book__c controller = loader.getController();
            controller.setBookId(targetBookId);
            controller.setMainController(mainController);

            if (mainController != null) {
                mainController.getCenterPane().getChildren().setAll(content);
                LOGGER.info("Navigated back to read_book__c via mainController with bookId: " + targetBookId);
            } else {
                LOGGER.warning("mainController is null, using direct navigation");
                Scene scene = new Scene(content);
                Stage stage = (Stage) back_button.getScene().getWindow();
                stage.setScene(scene);
                LOGGER.info("Navigated back to read_book__c directly with bookId: " + targetBookId);
            }
        } catch (IOException e) {
            LOGGER.severe("Failed to navigate back to read_book.fxml: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate back.");
        }
    }

    private void fetchAuthorData() {
        if (authorId <= 0) {
            LOGGER.warning("Invalid authorId: " + authorId);
            return;
        }

        String sql = "SELECT user_id, username, email, profile_picture, created_at FROM users WHERE user_id = ?"; // Match column name
        try (Connection conn = db_connect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, authorId);
            ResultSet rs = pstmt.executeQuery();
            LOGGER.info("Executing query for authorId: " + authorId);
            if (rs.next()) {
                author = new Author(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("profile_picture"),
                        rs.getTimestamp("created_at")
                );
                LOGGER.info("Found author: " + author.username + " (user_id: " + author.userId + ")");
            } else {
                LOGGER.warning("No author found for authorId: " + authorId);
                author = null; // Explicitly set to null for display
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to fetch author data: " + e.getMessage());
            author = null; // Set to null on error
        }
    }

    private void fetchAuthorBooks() {
        if (authorId <= 0) {
            LOGGER.warning("Invalid authorId: " + authorId);
            return;
        }

        String sql = "SELECT b.book_id, b.title, b.cover_photo, b.created_at " +
                "FROM books b JOIN book_authors ba ON b.book_id = ba.book_id " +
                "WHERE ba.user_id = ? ORDER BY b.created_at DESC";
        books = new ArrayList<>();
        try (Connection conn = db_connect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, authorId);
            ResultSet rs = pstmt.executeQuery();
            LOGGER.info("Executing query for author books with authorId: " + authorId);
            while (rs.next()) {
                Book book = new Book(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("cover_photo"),
                        rs.getTimestamp("created_at")
                );
                books.add(book);
                LOGGER.info("Found book: " + book.title + " (book_id: " + book.bookId + ")");
            }
            LOGGER.info("Total books fetched: " + books.size());
            if (books.isEmpty()) {
                LOGGER.warning("No books found for authorId: " + authorId);
            }
        } catch (SQLException e) {
            LOGGER.severe("Failed to fetch author books: " + e.getMessage());
        }
    }

    private void displayAuthorData() {
        if (author == null) {
            LOGGER.warning("No author data to display for authorId: " + authorId);
            if (author_name != null) author_name.setText("Unknown Author");
            if (author_email != null) author_email.setText("N/A");
            if (joined_at != null) joined_at.setText("N/A");
            return;
        }

        if (author_name != null) author_name.setText(author.username != null ? author.username : "Unknown");
        if (author_email != null) author_email.setText(author.email != null ? author.email : "N/A");
        if (joined_at != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
            joined_at.setText("Member since " + (author.createdAt != null ? sdf.format(author.createdAt) : "N/A"));
        }

        if (cover_photo != null && author.profilePicture != null && !author.profilePicture.isEmpty()) {
            try {
                cover_photo.setImage(new Image(getClass().getResource("/images/profiles/" + author.profilePicture).toExternalForm()));
            } catch (Exception e) {
                LOGGER.warning("Failed to load profile picture: " + author.profilePicture + " - " + e.getMessage());
                cover_photo.setImage(new Image(getClass().getResource("/images/profiles/hollow_circle.png").toExternalForm()));
            }
        } else if (cover_photo != null) {
            cover_photo.setImage(new Image(getClass().getResource("/images/profiles/hollow_circle.png").toExternalForm()));
        }
    }

    private void displayAuthorBooks() {
        if (books == null) {
            books = new ArrayList<>();
            LOGGER.warning("books list was null, initialized to empty for authorId: " + authorId);
        }
        if (total_number_of_author_works != null) {
            total_number_of_author_works.setText("(" + books.size() + ")");
            LOGGER.info("Displayed total works: " + books.size() + " for authorId: " + authorId);
        } else {
            LOGGER.severe("total_number_of_author_works is null, cannot display total works");
        }

        if (book_card_container != null) {
            LOGGER.info("book_card_container found, current children count: " + book_card_container.getChildren().size());
            book_card_container.getChildren().clear();
            LOGGER.info("Cleared book_card_container, preparing to add " + books.size() + " books");

            if (books.isEmpty()) {
                LOGGER.warning("No books to display for authorId: " + authorId);
                Label noBooksLabel = new Label("No works found for this author.");
                noBooksLabel.setTextFill(javafx.scene.paint.Color.WHITE);
                book_card_container.getChildren().add(noBooksLabel);
            } else {
                for (Book book : books) {
                    try {
                        HBox bookHBox = new HBox();
                        bookHBox.setAlignment(javafx.geometry.Pos.CENTER);
                        bookHBox.setPrefSize(306, 154);
                        bookHBox.setMaxSize(306, 154);
                        bookHBox.setMinSize(306, 154);
                        bookHBox.setStyle("-fx-background-color: #F28888; -fx-background-radius: 5; -fx-border-color: #FFFFFF; -fx-border-radius: 5;");

                        ImageView bookCover = new ImageView();
                        bookCover.setFitHeight(118.0);
                        bookCover.setFitWidth(80.0);
                        bookCover.setPickOnBounds(true);
                        bookCover.setPreserveRatio(true);
                        if (book.coverPhoto != null && !book.coverPhoto.isEmpty()) {
                            try {
                                bookCover.setImage(new Image(getClass().getResource("/images/book_covers/" + book.coverPhoto).toExternalForm()));
                            } catch (Exception e) {
                                LOGGER.warning("Failed to load book cover: " + book.coverPhoto + " - " + e.getMessage());
                                bookCover.setImage(new Image(getClass().getResource("/images/book_covers/hollow_rectangle.png").toExternalForm()));
                            }
                        } else {
                            bookCover.setImage(new Image(getClass().getResource("/images/book_covers/hollow_rectangle.png").toExternalForm()));
                        }

                        VBox labelVBox = new VBox();
                        labelVBox.setPrefSize(182, 114);
                        labelVBox.setMaxSize(182, 114);
                        labelVBox.setMinSize(182, 114);
                        labelVBox.setSpacing(5.0);
                        labelVBox.setPadding(new javafx.geometry.Insets(0, 10, 0, 10));

                        Label bookName = new Label(book.title != null ? book.title : "Untitled");
                        bookName.setPrefHeight(57.0);
                        bookName.setPrefWidth(122.0);
                        bookName.setWrapText(true);
                        bookName.setTextFill(javafx.scene.paint.Color.WHITE);
                        bookName.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 18));

                        Label postDate = new Label("Posted on " + (book.createdAt != null ? new SimpleDateFormat("dd/MM/yyyy").format(book.createdAt) : "N/A"));
                        postDate.setPrefHeight(42.0);
                        postDate.setPrefWidth(160.0);
                        postDate.setWrapText(true);
                        postDate.setTextFill(javafx.scene.paint.Color.WHITE);
                        postDate.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

                        labelVBox.getChildren().addAll(bookName, postDate);
                        bookHBox.getChildren().addAll(bookCover, labelVBox);
                        book_card_container.getChildren().add(bookHBox);
                        LOGGER.info("Successfully added book to UI: " + book.title + " (book_id: " + book.bookId + ")");
                    } catch (Exception e) {
                        LOGGER.severe("Failed to add book to UI: " + book.title + " - " + e.getMessage());
                    }
                }
            }
        } else {
            LOGGER.severe("book_card_container is null, cannot display books");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class Author {
        int userId;
        String username;
        String email;
        String profilePicture;
        java.sql.Timestamp createdAt;

        Author(int userId, String username, String email, String profilePicture, java.sql.Timestamp createdAt) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.profilePicture = profilePicture;
            this.createdAt = createdAt;
        }
    }

    private static class Book {
        int bookId;
        String title;
        String coverPhoto;
        java.sql.Timestamp createdAt;

        Book(int bookId, String title, String coverPhoto, java.sql.Timestamp createdAt) {
            this.bookId = bookId;
            this.title = title;
            this.coverPhoto = coverPhoto;
            this.createdAt = createdAt;
        }
    }
}