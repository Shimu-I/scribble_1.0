package com.example.scribble;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class reading_list__c {
    private static final Logger LOGGER = Logger.getLogger(reading_list__c.class.getName());

    @FXML private Button fantasy_button, thriller_button, mystery_button, horror_button, fiction_button;
    @FXML private Button add_book_button, inside_book_button;
    @FXML private Button newBookButton;
    @FXML private TextField search_bar;
    @FXML private VBox books_container;

    private Connection conn;
    private List<Book> allBooks = new ArrayList<>();
    private nav_bar__c mainController;

    public void setMainController(nav_bar__c mainController) {
        this.mainController = mainController;
    }

    // Inner class for storing book data
    private static class Book {
        int id, viewCount;
        String title, coverPhoto, genre, status, author;
        double avgRating;

        Book(int id, String title, String coverPhoto, String genre, String status,
             int viewCount, String author, double avgRating) {
            this.id = id;
            this.title = title;
            this.coverPhoto = coverPhoto;
            this.genre = genre;
            this.status = status;
            this.viewCount = viewCount;
            this.author = (author != null) ? author : "Unknown";
            this.avgRating = avgRating;
        }
    }

    @FXML
    public void initialize() {
        connectToDatabase();
        if (books_container == null) {
            System.err.println("books_container not initialized.");
            return;
        }

        allBooks = fetchBooks("");
        displayBooks(allBooks);

        if (search_bar != null) {
            search_bar.textProperty().addListener((obs, oldVal, newVal) -> searchBooks(newVal.trim()));
        }

        if (newBookButton != null) {
            newBookButton.setOnAction(this::handleNewBook);
            LOGGER.info("New book button initialized in reading_list__c");
        } else {
            LOGGER.warning("newBookButton not found in FXML.");
        }
    }

    private void connectToDatabase() {
        try {
            conn = db_connect.getConnection();
            System.out.println((conn != null && !conn.isClosed()) ? "Database connected." : "Failed to connect.");
        } catch (Exception e) {
            System.err.println("DB Connection Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Book> fetchBooks(String searchTerm) {
        List<Book> books = new ArrayList<>();
        if (conn == null) return books;

        String query = searchTerm.isEmpty()
                ? "SELECT b.book_id, b.title, b.cover_photo, b.genre, b.status, b.view_count, u.username, " +
                "COALESCE((SELECT AVG(rating) FROM ratings WHERE book_id = b.book_id), 0.0) AS avg_rating " +
                "FROM books b " +
                "LEFT JOIN book_authors ba ON b.book_id = ba.book_id AND ba.role = 'Owner' " +
                "LEFT JOIN users u ON ba.user_id = u.user_id " +
                "GROUP BY b.book_id, u.username"
                : "SELECT b.book_id, b.title, b.cover_photo, b.genre, b.status, b.view_count, u.username, " +
                "COALESCE((SELECT AVG(rating) FROM ratings WHERE book_id = b.book_id), 0.0) AS avg_rating " +
                "FROM books b " +
                "LEFT JOIN book_authors ba ON b.book_id = ba.book_id AND ba.role = 'Owner' " +
                "LEFT JOIN users u ON ba.user_id = u.user_id " +
                "WHERE LOWER(b.title) LIKE ? OR LOWER(b.genre) LIKE ? OR LOWER(b.description) LIKE ? OR LOWER(u.username) LIKE ? " +
                "GROUP BY b.book_id, u.username";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            if (!searchTerm.isEmpty()) {
                String pattern = "%" + searchTerm.toLowerCase() + "%";
                pstmt.setString(1, pattern);
                pstmt.setString(2, pattern);
                pstmt.setString(3, pattern);
                pstmt.setString(4, pattern);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    books.add(new Book(
                            rs.getInt("book_id"),
                            rs.getString("title"),
                            rs.getString("cover_photo"),
                            rs.getString("genre"),
                            rs.getString("status"),
                            rs.getInt("view_count"),
                            rs.getString("username"),
                            rs.getDouble("avg_rating")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching books: " + e.getMessage());
        }
        return books;
    }

    private void displayBooks(List<Book> books) {
        books_container.getChildren().clear();
        int booksPerRow = 4;
        HBox currentRow = null;

        for (int i = 0; i < books.size(); i++) {
            if (i % booksPerRow == 0) {
                currentRow = new HBox(25);
                currentRow.setMinHeight(330);
                currentRow.setPrefWidth(1400);
                currentRow.setAlignment(Pos.TOP_CENTER);
                currentRow.setPadding(new Insets(5, 0, 0, 0));
                books_container.getChildren().add(currentRow);
            }

            try {
                VBox card = createBookCard(books.get(i));
                currentRow.getChildren().add(card);
            } catch (Exception e) {
                System.err.println("Error creating book card: " + e.getMessage());
            }
        }
    }

    private VBox createBookCard(Book book) {
        // Create the book card VBox
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefHeight(330);
        card.setPrefWidth(214);
        card.setStyle("-fx-background-color: #F5E0CD; -fx-background-radius: 5;");

        // Book cover ImageView
        ImageView bookCover = new ImageView();
        bookCover.setFitHeight(148);
        bookCover.setFitWidth(100);
        bookCover.setPickOnBounds(true);
        bookCover.setPreserveRatio(true);
        loadBookImage(book.coverPhoto, bookCover);

        // Book name Label
        Label bookName = new Label(book.title);
        bookName.setFont(Font.font("System", 18));

        // Details VBox
        VBox detailsVBox = new VBox(0);
        detailsVBox.setAlignment(Pos.TOP_CENTER);
        detailsVBox.setPrefHeight(55);
        detailsVBox.setPrefWidth(159);
        detailsVBox.setPadding(new Insets(0, 0, 0, 10));

        // Author HBox
        HBox authorHBox = new HBox();
        authorHBox.setPrefHeight(16);
        authorHBox.setPrefWidth(120);
        authorHBox.setPadding(new Insets(0, 0, 0, 20));
        Label authorLabel = new Label("Author: ");
        authorLabel.setFont(Font.font("System", 12));
        Label authorName = new Label(book.author);
        authorHBox.getChildren().addAll(authorLabel, authorName);

        // Genre HBox
        HBox genreHBox = new HBox();
        genreHBox.setPrefHeight(16);
        genreHBox.setPrefWidth(120);
        genreHBox.setPadding(new Insets(0, 0, 0, 20));
        Label genreLabel = new Label("Genre: ");
        genreLabel.setFont(Font.font("System", 12));
        Label genreName = new Label(book.genre);
        genreHBox.getChildren().addAll(genreLabel, genreName);

        // Rating HBox
        HBox ratingHBox = new HBox();
        ratingHBox.setPrefHeight(16);
        ratingHBox.setPrefWidth(120);
        ratingHBox.setPadding(new Insets(0, 0, 0, 20));
        Label rateStar = new Label("Rating: " + getRatingStars(book.avgRating) +
                " (" + String.format("%.1f", book.avgRating) + "/5)");
        rateStar.setFont(Font.font("System", 12));
        ratingHBox.getChildren().add(rateStar);

        detailsVBox.getChildren().addAll(authorHBox, genreHBox, ratingHBox);

        // Open Button
        Button openButton = new Button("Open");
        openButton.setPrefHeight(30);
        openButton.setPrefWidth(120);
        openButton.setStyle("-fx-background-color: #F4908A; -fx-background-radius: 10;");
        openButton.setFont(Font.font("System", 14));
        // Store bookId in the button's user data
        openButton.setUserData(book.id);
        // Set onAction programmatically to ensure consistency (optional, since FXML binds it)
        openButton.setOnAction(event -> handle_inside_book_button(event));

        // Add hover effect
        openButton.setOnMouseEntered(event -> {
            openButton.setScaleX(1.1); // Scale up by 10%
            openButton.setScaleY(1.1);
            openButton.setStyle("-fx-background-color: #E07A75; -fx-background-radius: 10;"); // Darker shade
        });
        openButton.setOnMouseExited(event -> {
            openButton.setScaleX(1.0); // Revert to original size
            openButton.setScaleY(1.0);
            openButton.setStyle("-fx-background-color: #F4908A; -fx-background-radius: 10;"); // Original color
        });

        // Add all components to the card
        card.getChildren().addAll(bookCover, bookName, detailsVBox, openButton);

        return card;
    }

    private void loadBookImage(String coverPhoto, ImageView imageView) {
        try {
            if (coverPhoto != null && !coverPhoto.isEmpty()) {
                File uploadFile = new File("Uploads/book_covers/" + coverPhoto);
                if (uploadFile.exists()) {
                    Image image = new Image("file:" + uploadFile.getAbsolutePath());
                    if (!image.isError()) {
                        imageView.setImage(image);
                        LOGGER.info("Loaded book cover from filesystem: file:" + uploadFile.getAbsolutePath());
                        return;
                    } else {
                        LOGGER.warning("Failed to load book cover from filesystem (image error): " + coverPhoto);
                    }
                }
                URL resource = getClass().getResource("/images/book_covers/" + coverPhoto);
                if (resource != null) {
                    Image image = new Image(resource.toExternalForm());
                    if (!image.isError()) {
                        imageView.setImage(image);
                        LOGGER.info("Loaded book cover from classpath: " + resource.toExternalForm());
                        return;
                    } else {
                        LOGGER.warning("Failed to load book cover from classpath (image error): " + coverPhoto);
                    }
                }
                LOGGER.warning("Book cover not found: " + coverPhoto);
            }
            URL defaultUrl = getClass().getResource("/images/book_covers/demo_cover.png");
            if (defaultUrl != null) {
                imageView.setImage(new Image(defaultUrl.toExternalForm()));
                LOGGER.info("Loaded default book cover: /images/book_covers/demo_cover.png");
            } else {
                LOGGER.severe("Default book cover not found: /images/book_covers/demo_cover.png");
                imageView.setImage(null);
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to load book cover: " + coverPhoto + " - " + e.getMessage());
            imageView.setImage(null);
        }
    }

    private String getRatingStars(double rating) {
        int full = (int) rating;
        boolean half = (rating - full) >= 0.5;
        StringBuilder stars = new StringBuilder("★".repeat(full));
        if (half) stars.append("☆");
        while (stars.length() < 5) stars.append("☆");
        return stars.toString();
    }

    private void searchBooks(String text) {
        displayBooks(fetchBooks(text));
    }

    @FXML
    private void handleGenreClick(ActionEvent event) {
        Button btn = (Button) event.getSource();
        search_bar.setText(btn.getText());
    }


    @FXML
    private void handleAddBook(ActionEvent event) {
        if (!UserSession.getInstance().isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please log in to write new book.");
            return;
        }
        if (mainController != null) {
            try {
                AppState.getInstance().setCurrentBookId(-1); // Clear currentBookId for new book
                LOGGER.info("Cleared AppState.currentBookId for new book creation");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/write.fxml"));
                Parent content = loader.load();
                write__c controller = loader.getController();
                controller.setMainController(mainController);
                AppState.getInstance().setPreviousFXML("/com/example/scribble/reading_list.fxml");
                mainController.getCenterPane().getChildren().setAll(content);
                LOGGER.info("Loading write.fxml via mainController for new book");
            } catch (IOException e) {
                LOGGER.severe("Failed to load write.fxml: " + e.getMessage());

                showAlert(Alert.AlertType.ERROR, "Error", "Failed to open write page.");
            }
        } else {
            LOGGER.severe("Main controller is null. Cannot load write.fxml.");

            showAlert(Alert.AlertType.ERROR, "Error", "Navigation failed: main controller is not initialized.");        }
    }

    // FXML-bound event handler for the "Open" button

    @FXML
    private void handle_inside_book_button(ActionEvent event) {
        Button source = (Button) event.getSource();
        Integer bookId = (Integer) source.getUserData();
        if (bookId != null) {
            // Update view count
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE books SET view_count = view_count + 1 WHERE book_id = ?")) {
                pstmt.setInt(1, bookId);
                pstmt.executeUpdate();
                System.out.println("Book opened. ID: " + bookId);
            } catch (SQLException e) {
                System.err.println("View count update failed: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update view count: " + e.getMessage());
            }

            if (mainController != null) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/read_book.fxml"));
                    Parent root = loader.load();
                    read_book__c controller = loader.getController();
                    controller.setBookId(bookId);
                    controller.setMainController(mainController); // Ensure mainController is passed
                    mainController.getCenterPane().getChildren().setAll(root);
                    AnchorPane.setTopAnchor(root, 0.0);
                    AnchorPane.setBottomAnchor(root, 0.0);
                    AnchorPane.setLeftAnchor(root, 0.0);
                    AnchorPane.setRightAnchor(root, 0.0);
                    System.out.println("Loaded read_book.fxml into mainController's center pane");
                } catch (IOException e) {
                    System.err.println("Failed to load read_book.fxml: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to load the book page: " + e.getMessage());
                }
            } else {
                System.err.println("Main controller is null. Cannot load read_book.fxml.");
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Navigation bar not available. Please restart the application.");
            }
        } else {
            System.err.println("No bookId found for button.");
            showAlert(Alert.AlertType.ERROR, "Error", "No book ID associated with this button.");
        }
    }

    @FXML
    private void handleNewBook(ActionEvent event) {
        AppState.getInstance().clearCurrentBookId();
        AppState.getInstance().setPreviousFXML("/com/example/scribble/reading_list.fxml");
        if (mainController != null) {
            mainController.loadFXML("write.fxml");
            LOGGER.info("Navigated to write.fxml for new book creation from reading_list__c");
        } else {
            LOGGER.severe("Main controller is null in reading_list__c, cannot navigate to write.fxml");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/write.fxml"));
                Parent page = loader.load();
                write__c writeController = loader.getController();
                writeController.setMainController(null);
                Stage stage = (Stage) newBookButton.getScene().getWindow();
                stage.getScene().setRoot(page);
                LOGGER.info("Navigated to write.fxml with fallback due to null mainController");
            } catch (IOException e) {
                LOGGER.severe("Failed to load write.fxml: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

    public void addBook(Book book) {
        allBooks.add(book);
        displayBooks(allBooks);
    }

    public void shutdown() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("DB connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing DB: " + e.getMessage());
        }
    }
}