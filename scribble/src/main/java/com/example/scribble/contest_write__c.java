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
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.nio.file.*;
import java.io.IOException;


public class contest_write__c {

    @FXML
    private Button back_button;

    @FXML
    private Button upload_button;

    @FXML
    private Button cover_photo_button;

    @FXML
    private TextField book_tittle;

    @FXML
    private Label genre_name;

    @FXML
    private ImageView cover_photo;

    @FXML
    private TextArea writing_area;

    private int contestId;
    private String genre;
    private int userId;
    private String username;
    private String userPhotoPath;
    private String selectedCoverPhotoPath;

    public void initData(int contestId, String genre, int userId, String username, String userPhotoPath) {
        this.contestId = contestId;
        this.genre = genre;
        this.userId = userId;
        this.username = username;
        this.userPhotoPath = userPhotoPath;

        book_tittle.setText("");
        book_tittle.setPromptText("write the title of this book");

        book_tittle.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        genre_name.setText("(genre: " + genre.toLowerCase() + ")");
        selectedCoverPhotoPath = null;
        try {
            String defaultImagePath = "/images/contest_book_cover/demo_cover_photo.png";
            java.net.URL resource = getClass().getResource(defaultImagePath);
            if (resource != null) {
                cover_photo.setImage(new Image(resource.toExternalForm()));
            } else {
                showErrorAlert("Resource Error", "Default cover photo not found in classpath.");
                cover_photo.setImage(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Resource Error", "Failed to load default cover photo.");
            cover_photo.setImage(null);
        }
        writing_area.setText("");
    }

    @FXML
    private void handle_back_button(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scribble/contest.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) back_button.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Pen Wars Contests");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Failed to return to the contest page.");
        }
    }

    @FXML
    private void handle_upload_button(ActionEvent event) {
        System.out.println("Upload button clicked at " + new java.util.Date());
        String entryTitle = book_tittle.getText().trim();
        String content = writing_area.getText().trim();

        if (entryTitle.isEmpty() || content.isEmpty()) {
            showErrorAlert("Input Error", "Please provide a title and content for your entry.");
            System.out.println("Validation failed: Empty title or content");
            return;
        }

        if (!UserSession.getInstance().isLoggedIn()) {
            showErrorAlert("Session Error", "You must be logged in to submit an entry.");
            System.out.println("Validation failed: User not logged in");
            return;
        }

        // Validate that userId matches the logged-in user's ID
        if (userId != UserSession.getInstance().getUserId()) {
            showErrorAlert("Session Error", "User ID mismatch. Please log in with the correct account.");
            System.out.println("Validation failed: userId " + userId + " does not match session userId " + UserSession.getInstance().getUserId());
            return;
        }

        try (Connection conn = db_connect.getConnection()) {
            System.out.println("Database connection established: " + (conn != null));
            conn.setAutoCommit(false);

            // Validate contestId exists and matches genre
            String validateContestSQL = "SELECT contest_id FROM contests WHERE contest_id = ? AND genre = ? COLLATE utf8mb4_bin";
            try (PreparedStatement validateStmt = conn.prepareStatement(validateContestSQL)) {
                validateStmt.setInt(1, contestId);
                validateStmt.setString(2, genre);
                ResultSet rs = validateStmt.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    showErrorAlert("Contest Error", "Invalid contest ID or genre mismatch.");
                    System.out.println("Validation failed: Contest ID " + contestId + " not found or genre mismatch for genre " + genre);
                    return;
                }
            }

            // Check for duplicate entry title for this contest
            String checkDuplicateSQL = "SELECT entry_id FROM contest_entries WHERE contest_id = ? AND entry_title = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkDuplicateSQL)) {
                checkStmt.setInt(1, contestId);
                checkStmt.setString(2, entryTitle);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    conn.rollback();
                    showErrorAlert("Input Error", "An entry with this title already exists for this contest.");
                    System.out.println("Validation failed: Duplicate entry title '" + entryTitle + "' for contestId " + contestId);
                    return;
                }
            }

            // Insert entry
            String insertEntrySQL = "INSERT INTO contest_entries (contest_id, user_id, entry_title, content, cover_photo) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertEntrySQL)) {
                stmt.setInt(1, contestId);
                stmt.setInt(2, userId);
                stmt.setString(3, entryTitle);
                stmt.setString(4, content);
                stmt.setString(5, selectedCoverPhotoPath);
                System.out.println("Executing entry insert with contestId: " + contestId + ", userId: " + userId);
                int rowsAffected = stmt.executeUpdate();
                System.out.println("Rows affected for entry: " + rowsAffected);
            }

            conn.commit();
            System.out.println("Transaction committed successfully");
            showInfoAlert("Success", "Your entry has been submitted successfully!");
            clearForm();

            // Navigate to contest_entries page with initialized data
            try {
                java.net.URL resource = getClass().getResource("/com/example/scribble/contest_entries.fxml");
                if (resource == null) {
                    System.err.println("Resource not found: /com/example/scribble/contest_entries.fxml");
                    showErrorAlert("Resource Error", "Contest entries FXML file not found. Check the file path in src/main/resources.");
                    return;
                }
                System.out.println("Resource found at: " + resource);
                FXMLLoader loader = new FXMLLoader(resource);
                Parent root = loader.load();
                contest_entries__c controller = loader.getController();
                controller.initData(contestId, genre, userId, username); // Pass data to new controller
                Stage stage = (Stage) upload_button.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Contest Entries");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showErrorAlert("Navigation Error", "Failed to load the contest entries page: " + e.getMessage());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Database Error", "Failed to submit your entry. Error: " + e.getMessage());
            System.out.println("SQLException caught: " + e.getMessage());
        }
    }



    @FXML
    private void handle_cover_photo_button(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Cover Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(cover_photo_button.getScene().getWindow());
        if (selectedFile != null) {
            Path directoryPath = Path.of("src/main/resources/images/contest_book_cover");
            int nextNumber = 1;

            // Find the highest existing number in filenames starting with "ccp_"
            try {
                Files.createDirectories(directoryPath);
                DirectoryStream<Path> stream = Files.newDirectoryStream(directoryPath, "ccp_[0-9]*.{png,jpg,jpeg}");
                for (Path file : stream) {
                    String filename = file.getFileName().toString();
                    String numberPart = filename.replace("ccp_", "").replaceAll("\\..*", "");
                    try {
                        int num = Integer.parseInt(numberPart);
                        if (num >= nextNumber) nextNumber = num + 1;
                    } catch (NumberFormatException ignored) {}
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            String extension = getFileExtension(selectedFile.getName());
            String newFilename = "ccp_" + nextNumber + "." + extension;
            Path destinationPath = directoryPath.resolve(newFilename);

            try {
                Files.copy(selectedFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                selectedCoverPhotoPath = newFilename; // Store new filename for database
                cover_photo.setImage(new Image("file:" + destinationPath.toString())); // Load full path for display
            } catch (IOException e) {
                e.printStackTrace();
                showErrorAlert("Image Error", "Failed to copy the selected cover photo.");
                selectedCoverPhotoPath = null;
            }
        }
    }

    // Helper method to get file extension
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex == -1) ? "png" : filename.substring(lastDotIndex + 1).toLowerCase();
    }


    private void clearForm() {
        book_tittle.setText("");
        writing_area.setText("");
        selectedCoverPhotoPath = null;
        try {
            String defaultImagePath = "/images/contest_book_cover/demo_cover_photo.png";
            java.net.URL resource = getClass().getResource(defaultImagePath);
            if (resource != null) {
                cover_photo.setImage(new Image(resource.toExternalForm()));
            } else {
                showErrorAlert("Resource Error", "Default cover photo not found in classpath.");
                cover_photo.setImage(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Resource Error", "Failed to load default cover photo.");
            cover_photo.setImage(null);
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}