package com.example.scribble;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;



public class write__c {

    @FXML
    public BorderPane rootPane;
    public Button back_to_books;

    @FXML
    private Button book_image_button;

    @FXML
    private ImageView bookCoverImageView;
    private String coverPhotoPath;

    @FXML
    private TextField book_title;

    @FXML
    private TextArea book_description;



    @FXML
    private ComboBox<String> genreComboBox;

    @FXML
    private ComboBox<String> statusComboBox;


    @FXML
    private Button write_button;

    @FXML
    private nav_bar__c mainController;

    public void setMainController(nav_bar__c mainController) {
        this.mainController = mainController;
    }


    public void handle_back_to_books(ActionEvent actionEvent) {
        if (mainController != null) {
            mainController.loadFXML("reading_list.fxml"); // Navigate back to Books
        }else {
            System.err.println("Main controller is null in write__c.");
        }
    }


    @FXML
    private void initialize() {
        genreComboBox.getItems().addAll(
                "Fantasy", "Thriller", "Mystery", "Thriller Mystery", "Youth Fiction",
                "Crime", "Horror", "Romance", "Science Fiction", "Adventure", "Historical"
        );
        genreComboBox.getSelectionModel().selectFirst();

        statusComboBox.getItems().addAll(
                "Ongoing", "Complete", "Hiatus"
        );
        statusComboBox.getSelectionModel().selectFirst();
    }


    @FXML
    private void handle_book_cover(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Book Cover Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                // Define the target directory
                String targetDir = "src/main/resources/images/book_covers/";
                Path targetPath = Paths.get(targetDir);
                if (!Files.exists(targetPath)) {
                    Files.createDirectories(targetPath); // Create directory if it doesn't exist
                }

                // Get the file extension
                String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));

                // Find the next available numeric name (e.g., cover_1.jpg)
                int nextNumber = 1;
                File targetFile;
                do {
                    targetFile = new File(targetDir + "cover_" + nextNumber + extension);
                    nextNumber++;
                } while (targetFile.exists());

                // Copy the file to the target directory
                Files.copy(selectedFile.toPath(), targetFile.toPath());

                // Store the relative path for database
                coverPhotoPath = "cover_" + (nextNumber - 1) + extension;

                // Display the image
                Image bookCover = new Image(targetFile.toURI().toString());
                bookCoverImageView.setImage(bookCover);
            } catch (Exception e) {
                showAlert("Error", "Failed to save or load image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleWriteButton(ActionEvent event) {
        System.out.println("Write button clicked");
        String title = book_title.getText();
        String description = book_description.getText();
        String genre = genreComboBox.getSelectionModel().getSelectedItem();
        String status = statusComboBox.getSelectionModel().getSelectedItem();
        String coverPhoto = coverPhotoPath;

        // Validate inputs
        if (title.isEmpty() || description.isEmpty()) {
            showAlert("Error", "Please fill in title and description.");
            return;
        }
        if (genre == null || status == null) {
            showAlert("Error", "Please select a genre and status.");
            return;
        }

        // Get user_id from UserSession
        UserSession session = UserSession.getInstance();
        if (!session.isLoggedIn()) {
            showAlert("Error", "You must be logged in to create a book.");
            navigateToSignIn();
            return;
        }
        int userId = session.getUserId(); // Dynamic user_id (int)

        // Save to database
        Connection conn = null;
        try {
            conn = db_connect.getConnection();
            if (conn == null) {
                showAlert("Error", "Failed to connect to the database.");
                return;
            }
            conn.setAutoCommit(false); // Start transaction

            // Insert into books table and get generated book_id
            String bookSql = "INSERT INTO books (title, description, genre, status, cover_photo, view_count) " +
                    "VALUES (?, ?, ?, ?, ?, 0)";
            PreparedStatement bookPstmt = conn.prepareStatement(bookSql, PreparedStatement.RETURN_GENERATED_KEYS);
            bookPstmt.setString(1, title);
            bookPstmt.setString(2, description);
            bookPstmt.setString(3, genre);
            bookPstmt.setString(4, status);
            bookPstmt.setString(5, coverPhoto); // May be null if no image was selected
            bookPstmt.executeUpdate();

            // Retrieve generated book_id
            ResultSet generatedKeys = bookPstmt.getGeneratedKeys();
            int bookId;
            if (generatedKeys.next()) {
                bookId = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Failed to retrieve book_id.");
            }

            // Insert into book_authors table
            String authorSql = "INSERT INTO book_authors (book_id, user_id, role) " +
                    "VALUES (?, ?, 'Owner')";
            PreparedStatement authorPstmt = conn.prepareStatement(authorSql);
            authorPstmt.setInt(1, bookId);
            authorPstmt.setInt(2, userId); // Use dynamic user_id
            authorPstmt.executeUpdate();

            conn.commit(); // Commit transaction
            showAlert("Success", "Book created successfully!");

            // Navigate to chapter page
            navigateToChapterPage(bookId, title, userId);

            clearForm();
        } catch (SQLException e) {
            try {
                conn.rollback(); // Roll back on error
            } catch (SQLException rollbackEx) {
                showAlert("Error", "Rollback failed: " + rollbackEx.getMessage());
            }
            showAlert("Error", "Failed to save book: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    showAlert("Error", "Failed to close connection: " + closeEx.getMessage());
                }
            }
        }
    }



    private void navigateToSignIn() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("sign_in.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) write_button.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load sign-in page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToChapterPage(int bookId, String bookName, int authorId) {
        if (mainController != null) {
            System.out.println("Loading write_chapter.fxml via mainController");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("write_chapter.fxml"));
                mainController.getCenterPane().getChildren().setAll((Node) loader.load());
                write_chapter__c chapterController = loader.getController();
                chapterController.setMainController(mainController);
                chapterController.setBookDetails(bookId, bookName, authorId);
            } catch (IOException e) {
                showAlert("Error", "Failed to load chapter page: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Main controller is null in write__c.");
            showAlert("Error", "Cannot navigate to chapter page: Main controller is null.");
        }
    }

    private void clearForm() {
        book_title.clear();
        book_description.clear();
        genreComboBox.getSelectionModel().clearSelection();
        statusComboBox.getSelectionModel().clearSelection();
        bookCoverImageView.setImage(null);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}